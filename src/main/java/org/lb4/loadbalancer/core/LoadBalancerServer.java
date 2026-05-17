package org.lb4.loadbalancer.core;

import org.lb4.loadbalancer.config.AppConfig;
import org.lb4.loadbalancer.metrics.MetricsPrinter;
import org.lb4.loadbalancer.metrics.MetricsRegistry;

public class LoadBalancerServer {

    private final AppConfig config;
    private final EventLoop eventLoop;

    public LoadBalancerServer(AppConfig config) {
        this.config = config;
        SessionManager sessionManager = new SessionManager();
        MetricsRegistry metrics = new MetricsRegistry();
        BackendRegistry backendRegistry = new BackendRegistry(config.getBackends(), config.getLoadBalancing().getAlgorithmEnum(), metrics);
        MetricsPrinter printer = new MetricsPrinter(metrics);
        printer.start(10);
        this.eventLoop = new EventLoop(config.getServer(), sessionManager, backendRegistry, metrics);
    }

    public void start() {
        eventLoop.run();
    }

}
