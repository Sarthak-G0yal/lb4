package org.lb4.loadbalancer.core;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Session {

    private final long sessionId;
    private SocketChannel clientChannel;
    private SocketChannel backendChannel;
    private SelectionKey clientKey;
    private SelectionKey backendKey;
    private SessionState state;
    private Backend backend;

    public Session(long sessionId) {
        this.sessionId = sessionId;
        this.state = SessionState.NEW;
    }

    public long getSessionId() {
        return sessionId;
    }

    public SocketChannel getClientChannel() {
        return clientChannel;
    }

    public void setClientChannel(SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    public SocketChannel getBackendChannel() {
        return backendChannel;
    }

    public void setBackendChannel(SocketChannel backendChannel) {
        this.backendChannel = backendChannel;
    }

    public SelectionKey getClientKey() {
        return clientKey;
    }

    public void setClientKey(SelectionKey clientKey) {
        this.clientKey = clientKey;
    }

    public SelectionKey getBackendKey() {
        return backendKey;
    }

    public void setBackendKey(SelectionKey backendKey) {
        this.backendKey = backendKey;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }
}
