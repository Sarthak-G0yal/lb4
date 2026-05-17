package org.lb4.loadbalancer.core;

import java.util.ArrayList;
import java.util.List;

import org.lb4.loadbalancer.config.BackendConfig;

public class BackendRegistry {

    private static final long FAILURE_COOLDOWN_MILLIS = 5000L;

    private final List<Backend> backends = new ArrayList<>();
    private int cursor = 0;

    public BackendRegistry(List<BackendConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            throw new IllegalArgumentException("backends must not be empty");
        }
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
        backend.markSuccess();;
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

    public Backend selectBackendForClient(String clientIp) {
        String key = (clientIp == null || clientIp.isBlank()) ? "unknown" : clientIp;
        int start = Math.floorMod(key.hashCode(), backends.size());
        for (int i = 0; i < backends.size(); i++) {
            Backend backend = backends.get((start + i) % backends.size());
            if (isBackendHealthy(backend)) {
                return backend;
            }
        }
        return backends.get(start);
    }
}
