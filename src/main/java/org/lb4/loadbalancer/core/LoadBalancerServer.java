package org.lb4.loadbalancer.core;

import org.lb4.loadbalancer.config.AppConfig;

public class LoadBalancerServer {

    private final AppConfig config;
    private final EventLoop eventLoop;

    public LoadBalancerServer(AppConfig config) {
        this.config = config;
        SessionManager sessionManager = new SessionManager();
        this.eventLoop = new EventLoop(config.getServer(),sessionManager);
    }

    public void start() {
        eventLoop.run();
    }
}
