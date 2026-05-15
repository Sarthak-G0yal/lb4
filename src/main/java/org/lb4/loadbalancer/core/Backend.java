package org.lb4.loadbalancer.core;

public class Backend {

    private final String id;
    private final String host;
    private final int port;

    public Backend(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Backend{"
                + "id='" + id + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + '}';
    }
}
