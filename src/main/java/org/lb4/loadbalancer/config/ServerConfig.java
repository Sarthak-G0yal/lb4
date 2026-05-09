package org.lb4.loadbalancer.config;

public class ServerConfig {

    private String listenIp;
    private int listenPort;

    public String getListenIp() {
        return listenIp;
    }

    public void setListenIp(String listenIp) {
        this.listenIp = listenIp;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public void validate() {
        if (listenIp == null || listenIp.isBlank()) {
            throw new IllegalArgumentException("listenIp is required");
        }
        if (listenPort < 1 || listenPort > 65535) {
            throw new IllegalArgumentException("listenPort must be 1..65535");
        }
    }

    @Override
    public String toString() {
        return "ServerConfig{"
                + "listenIp='" + listenIp + '\''
                + ", listenPort=" + listenPort
                + '}';
    }
}
