package org.lb4.loadbalancer;

import java.nio.file.Path;

import org.lb4.loadbalancer.config.AppConfig;
import org.lb4.loadbalancer.config.ConfigLoader;
import org.lb4.loadbalancer.core.LoadBalancerServer;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <path-to-config-yaml>");
            System.exit(2);
        }
        AppConfig config = ConfigLoader.loadFromPath(Path.of(args[0]));
        System.out.println("Loaded Config: " + config);

        LoadBalancerServer server = new LoadBalancerServer(config);
        server.start();
    }
}
