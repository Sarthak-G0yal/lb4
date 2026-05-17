package org.lb4.loadbalancer.core;

import java.util.ArrayList;
import java.util.List;

import org.lb4.loadbalancer.config.BackendConfig;
import org.lb4.loadbalancer.config.LoadBalancingAlgorithm;

public class BackendRegistry {

    private static final long FAILURE_COOLDOWN_MILLIS = 5000L;

    private final List<Backend> backends = new ArrayList<>();
    private int cursor = 0;

    private final LoadBalancingAlgorithm algorithm;

    public BackendRegistry(List<BackendConfig> configs, LoadBalancingAlgorithm algorithm) {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("backends must not be empty");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm is required");
        }
        this.algorithm = algorithm;
        for (BackendConfig cfg : configs) {
            backends.add(new Backend(cfg.getId(), cfg.getHost(), cfg.getPort()));
        }
    }

    public Backend selectBackend() {
        int index = Math.floorMod(cursor++, backends.size());
        return backends.get(index);
    }

    public List<Backend> getBackends() {
        return List.copyOf(backends);
    }

    public void markBackendFailure(Backend backend, String reason) {
        if (backend == null) {
            return;
        }
        backend.markFailure();
        System.out.println("Marked backend unhealthy: " + backend + " reason=" + reason);
    }

    public void markBackendSuccess(Backend backend) {
        if (backend == null) {
            return;
        }
        backend.markSuccess();
    }

    private boolean isBackendHealthy(Backend backend) {
        if (backend.getState() == BackendState.HEALTHY) {
            return true;
        }

        long elapsed = System.currentTimeMillis() - backend.getLastFailedAtMillis();
        if (elapsed >= FAILURE_COOLDOWN_MILLIS) {
            backend.setState(BackendState.HEALTHY);
            return true;
        }
        return false;
    }

    public Backend selectBackend(String clientIp) {
        if (algorithm == LoadBalancingAlgorithm.ROUND_ROBIN) {
            return selectRoundRobin();
        }
        return selectIpHash(clientIp);
    }

    private Backend selectRoundRobin() {
        for (int i = 0; i < backends.size(); i++) {
            Backend backend = backends.get(Math.floorMod(cursor++, backends.size()));
            if (isBackendHealthy(backend)) {
                return backend;
            }
        }
        return null;
    }

    private Backend selectIpHash(String clientIp) {
        String key = (clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp;
        int start = Math.floorMod(key.hashCode(), backends.size());
        for (int i = 0; i < backends.size(); i++) {
            Backend backend = backends.get((start + i) % backends.size());
            if (isBackendHealthy(backend)) {
                return backend;
            }
        }
        return null;
    }
}
