package org.lb4.loadbalancer.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.lb4.loadbalancer.config.ServerConfig;
import org.lb4.loadbalancer.logging.Log;
import org.lb4.loadbalancer.metrics.MetricsRegistry;

public class EventLoop {

    private static final int BUFFER_SIZE = 8192;

    private final ServerConfig serverConfig;
    private final SessionManager sessionManager;
    private final BackendRegistry backendRegistry;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final MetricsRegistry metrics;

    public EventLoop(ServerConfig serverConfig, SessionManager sessionManager, BackendRegistry backendRegistry, MetricsRegistry metrics) {
        this.serverConfig = serverConfig;
        this.sessionManager = sessionManager;
        this.backendRegistry = backendRegistry;
        this.metrics = metrics;
    }

    public void run() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(serverConfig.getListenIp(), serverConfig.getListenPort()));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            Log.info("listen",
                    "ip", serverConfig.getListenIp(),
                    "port", String.valueOf(serverConfig.getListenPort()));
            while (true) {
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isConnectable()) {
                            handleConnect(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        Session session = (Session) key.attachment();
                        if (session != null) {
                            if (key.channel() == session.getBackendChannel()) {
                                backendRegistry.markBackendFailure(session.getBackend(), "io exception");
                            }
                            closeSession(session);
                        } else {
                            key.cancel();
                            key.channel().close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Event loop failure", e);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client;
        while ((client = server.accept()) != null) {
            client.configureBlocking(false);

            Session session = sessionManager.createSession();
            session.setClientChannel(client);
            session.setState(SessionState.ACTIVE);
            metrics.incrementTotalSessions();
            metrics.incrementActiveSessions();

            InetSocketAddress remote = (InetSocketAddress) client.getRemoteAddress();
            String clientIp = remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.getHostString();

            Backend backend = backendRegistry.selectBackend(clientIp);

            if (backend == null) {
                Log.info("no_backend",
                        "clientIp", clientIp,
                        "sessionId", String.valueOf(session.getSessionId()));
                client.close();
                return;
            }
            session.setBackend(backend);

            Log.info("backend_selected",
                    "sessionId", String.valueOf(session.getSessionId()),
                    "clientIp", clientIp,
                    "backend", backend.toString());

            SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ, session);
            session.setClientKey(clientKey);

            SocketChannel backendChannel = SocketChannel.open();
            backendChannel.configureBlocking(false);
            session.setBackendChannel(backendChannel);

            InetSocketAddress backendAddr = new InetSocketAddress(backend.getHost(), backend.getPort());
            boolean connected = backendChannel.connect(backendAddr);

            SelectionKey backendKey = backendChannel.register(selector, connected ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT, session);
            session.setBackendKey(backendKey);

            sessionManager.register(session);
            Log.info("session_accept",
                    "sessionId", String.valueOf(session.getSessionId()),
                    "client", String.valueOf(client.getRemoteAddress()));
            if (connected) {
                Log.info("backend_connected",
                        "sessionId", String.valueOf(session.getSessionId()),
                        "backend", backend.toString());
            } else {
                Log.info("backend_connecting",
                        "sessionId", String.valueOf(session.getSessionId()),
                        "backend", backend.toString());
            }
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {
        Session session = (Session) key.attachment();
        if (session == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        SocketChannel backend = (SocketChannel) key.channel();
        try {
            if (backend.finishConnect()) {
                key.interestOps(SelectionKey.OP_READ);
                backendRegistry.markBackendSuccess(session.getBackend());
                Log.info("backend_connected",
                        "sessionId", String.valueOf(session.getSessionId()),
                        "backend", session.getBackend().toString());
            }
        } catch (IOException e) {
            backendRegistry.markBackendFailure(session.getBackend(), "connect failed");
            Log.info("backend_connect_failed",
                    "sessionId", String.valueOf(session.getSessionId()),
                    "error", e.getMessage());
            closeSession(session);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        Session session = (Session) key.attachment();
        if (session == null) {
            key.cancel();
            key.channel().close();
            return;
        }
        SocketChannel from = (SocketChannel) key.channel();
        SocketChannel to = peerChannel(session, from);
        if (to == null || !to.isConnected()) {
            closeSession(session);
            return;
        }

        readBuffer.clear();
        int read = from.read(readBuffer);

        if (read == -1) {
            if (from == session.getBackendChannel()) {
                backendRegistry.markBackendFailure(session.getBackend(), "backend EOF");
            }
            Log.info("session_closed",
                    "sessionId", String.valueOf(session.getSessionId()),
                    "reason", "eof");
            closeSession(session);
            return;
        }
        if (read == 0) {
            return;
        }
        readBuffer.flip();
        int totalWritten = 0;
        while (readBuffer.hasRemaining()) {
            int written = to.write(readBuffer);
            if (written == 0) {
                if (to == session.getBackendChannel()) {
                    backendRegistry.markBackendFailure(session.getBackend(), "partial write");
                }
                Log.info("partial_write",
                        "sessionId", String.valueOf(session.getSessionId()));
                closeSession(session);
                return;
            }
            totalWritten += written;
        }
        metrics.addBytesForwarded(totalWritten);
        String side = from == session.getClientChannel() ? "client" : "backend";
        Log.info("forward",
                "sessionId", String.valueOf(session.getSessionId()),
                "from", side,
                "bytes", String.valueOf(totalWritten));
    }

    private SocketChannel peerChannel(Session session, SocketChannel channel) {
        if (channel == session.getClientChannel()) {
            return session.getBackendChannel();
        }
        if (channel == session.getBackendChannel()) {
            return session.getClientChannel();
        }
        return null;
    }

    private void closeSession(Session session) throws IOException {
        if (session.getState() == SessionState.CLOSED) {
            return;
        }
        session.setState(SessionState.CLOSED);
        metrics.decrementActiveSessions();
        metrics.incrementFailedSessions();

        SelectionKey clientKey = session.getClientKey();
        if (clientKey != null) {
            clientKey.cancel();
        }
        SelectionKey backendKey = session.getBackendKey();
        if (backendKey != null) {
            backendKey.cancel();
        }
        closeChannel(session.getClientChannel());
        closeChannel(session.getBackendChannel());
        sessionManager.remove(session);
    }

    private void closeChannel(SocketChannel channel) {
        if (channel == null || !channel.isOpen()) {
            return;
        }
        try {
            channel.close();

        } catch (IOException ignored) {
        }
    }

}
