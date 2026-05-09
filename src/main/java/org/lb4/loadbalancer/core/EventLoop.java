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

    private final ServerConfig serverConfig;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    private Selector selector;
    private ServerSocketChannel serverChannel;

    public EventLoop(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
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
                    if (key.isAcceptable()) {
                        handleAccept(key);

                    } else if (key.isReadable()) {
                        handleRead(key);
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
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("Accepted " + client.getRemoteAddress());
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        readBuffer.clear();
        int read = client.read(readBuffer);

        if (read == -1) {
            System.out.println("Closed by peer" + client.getRemoteAddress());
            key.cancel();
            client.close();
            return;
        }
        if (read > 0) {
            System.out.println("Read " + read + "byte from " + client.getRemoteAddress());
        }
    }

}
