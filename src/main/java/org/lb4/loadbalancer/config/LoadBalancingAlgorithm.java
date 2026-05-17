package org.lb4.loadbalancer.config;

public enum LoadBalancingAlgorithm {
    IP_HASH,
    ROUND_ROBIN;

    public static LoadBalancingAlgorithm fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("algorithm is required");
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "ip_hash":
                return IP_HASH;
            case "round_robin":
                return ROUND_ROBIN;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + value);
        }
    }

}
