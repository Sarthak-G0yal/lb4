package org.lb4.loadbalancer.config;

public class LoadBalancerConfig {

    private String algorithm;
    private LoadBalancingAlgorithm algorithmEnum;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void validate() {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("algorithm is required");
        }
        algorithmEnum = LoadBalancingAlgorithm.fromString(algorithm);
    }

    @Override
    public String toString() {
        return "LoadBalancerConfig{"
                + "algorithm='" + algorithm + '\''
                + '}';
    }

    public LoadBalancingAlgorithm getAlgorithmEnum() {
        return algorithmEnum;
    }
}
