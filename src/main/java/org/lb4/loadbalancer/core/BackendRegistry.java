package org.lb4.loadbalancer.core;

import java.util.ArrayList;
import java.util.List;

import org.lb4.loadbalancer.config.BackendConfig;

public class BackendRegistry {

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
}
