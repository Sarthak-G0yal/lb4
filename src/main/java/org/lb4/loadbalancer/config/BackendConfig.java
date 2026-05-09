package org.lb4.loadbalancer.config;

public class BackendConfig {

    private String id;
    private String host;
    private int port;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("backend id is required");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("backend host is required");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("backend port must be 1..65535");
        }
    }

    @Override
    public String toString() {
        return "BackendConfig{"
                + "id='" + id + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + '}';
    }
}
