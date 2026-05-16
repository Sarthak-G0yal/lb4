package org.lb4.loadbalancer.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpEchoServer {

    private static final int DEFAULT_BACKLOG = 128;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: <port> [name]");
            System.exit(2);
        }
        int port = Integer.parseInt(args[0]);
        String name = args.length > 1 ? args[1] : "backend";

        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(port, DEFAULT_BACKLOG)) {
            System.out.println(name + " listening on port " + port);
            while (true) {
                Socket socket = server.accept();
                pool.execute(() -> handle(socket, name));
            }
        }
    }

    private static void handle(Socket socket, String name) {
        String remote = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println(name + " accepted " + remote);
        try (socket;
                InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                out.flush();
                String payload = new String(buf, 0, read, StandardCharsets.UTF_8)
                        .replace("\r", "\\r")
                        .replace("\n", "\\n");
                System.out.println(name + " received " + read + " bytes: " + payload);
            }
        } catch (IOException e) {
            System.out.println(name + "connection error: " + e.getMessage());
        }

    }
}
