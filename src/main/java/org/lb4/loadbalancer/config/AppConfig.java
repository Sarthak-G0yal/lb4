package org.lb4.loadbalancer.config;

import java.util.List;

public class AppConfig {

    private ServerConfig server;
    private LoadBalancerConfig loadBalancing;
    private List<BackendConfig> backends;

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public LoadBalancerConfig getLoadBalancing() {
        return loadBalancing;
    }

    public void setLoadBalancing(LoadBalancerConfig loadBalancing) {
        this.loadBalancing = loadBalancing;
    }

    public List<BackendConfig> getBackends() {
        return backends;
    }

    public void setBackends(List<BackendConfig> backends) {
        this.backends = backends;
    }

    public void validate() {
        if (server == null) {
            throw new IllegalArgumentException("server is required");
        }
        server.validate();

        if (loadBalancing == null) {
            throw new IllegalArgumentException("loadBalancing is required");
        }
        loadBalancing.validate();

        if (backends == null || backends.isEmpty()) {
            throw new IllegalArgumentException("backends must not be empty");
        }
        for (BackendConfig backend : backends) {
            backend.validate();
        }
    }

    @Override
    public String toString() {
        return "AppConfig{"
                + "server=" + server
                + ", loadBalancing=" + loadBalancing
                + ", backends=" + backends
                + '}';
    }
}
