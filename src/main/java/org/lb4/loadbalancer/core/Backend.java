package org.lb4.loadbalancer.core;

public class Backend {

    private final String id;
    private final String host;
    private final int port;

    private BackendState state = BackendState.HEALTHY;
    private long failedConnections;
    private long successfulConnections;
    private long lastFailedAtMillis;

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

    public BackendState getState() {
        return state;
    }

    public void setState(BackendState state) {
        this.state = state;
    }

    public long getFailedConnections() {
        return failedConnections;
    }

    public long getSuccessfulConnections() {
        return successfulConnections;
    }

    public long getLastFailedAtMillis() {
        return lastFailedAtMillis;
    }

    public void markFailure() {
        failedConnections++;
        lastFailedAtMillis = System.currentTimeMillis();
        state = BackendState.UNHEALTHY;
    }

    public void markSuccess() {
        successfulConnections++;
        state = BackendState.HEALTHY;
    }

    @Override
    public String toString() {
        return "Backend{"
                + "id='" + id + '\''
                + ", host='" + host + '\''
                + ", port=" + port
                + ", state=" + state
                + '}';
    }
}
