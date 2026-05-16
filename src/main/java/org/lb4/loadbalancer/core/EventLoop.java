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

public class EventLoop {

    private static final int BUFFER_SIZE = 8192;

    private final ServerConfig serverConfig;
    private final SessionManager sessionManager;
    private final BackendRegistry backendRegistry;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private Selector selector;
    private ServerSocketChannel serverChannel;

    public EventLoop(ServerConfig serverConfig, SessionManager sessionManager, BackendRegistry backendRegistry) {
        this.serverConfig = serverConfig;
        this.sessionManager = sessionManager;
        this.backendRegistry = backendRegistry;
    }

    public void run() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(serverConfig.getListenIp(), serverConfig.getListenPort()));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Listening on " + serverConfig.getListenIp() + ":" + serverConfig.getListenPort());
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

            InetSocketAddress remote = (InetSocketAddress) client.getRemoteAddress();
            String clientIp = remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.getHostString();

            Backend backend = backendRegistry.selectBackendForClient(clientIp);
            session.setBackend(backend);

            System.out.println("Selected backend " + backend + " for client " + clientIp);

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
            System.out.println("Accepted session " + session.getSessionId() + " from " + client.getRemoteAddress());
            if (connected) {
                System.out.println("Session " + session.getSessionId() + " connected to backend " + backend);
            } else {
                System.out.println("Session " + session.getSessionId() + " connecting to backend " + backend);
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
                System.out.println("Backend connected for session " + session.getSessionId());
            }
        } catch (IOException e) {
            System.out.println("Backend connect failed for session " + session.getSessionId() + ": " + e.getMessage());
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
            System.out.println("Closed by peer session " + session.getSessionId());
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
                System.out.println("Partial write for session " + session.getSessionId());
                closeSession(session);
                return;
            }
            totalWritten += written;
        }
        String side = from == session.getClientChannel() ? "client" : "backend";
        System.out.println("Forwarded " + totalWritten + "bytes from " + side + " for session " + session.getSessionId());
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
        session.setState(SessionState.CLOSED);

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
