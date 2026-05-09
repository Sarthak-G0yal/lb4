package org.lb4.loadbalancer.core;

import org.lb4.loadbalancer.config.AppConfig;

public class LoadBalancerServer {

    private final AppConfig config;
    private final EventLoop eventLoop;

    public LoadBalancerServer(AppConfig config) {
        this.config = config;
        this.eventLoop = new EventLoop(config.getServer());
    }

    public void start() {
        eventLoop.run();
    }
}
