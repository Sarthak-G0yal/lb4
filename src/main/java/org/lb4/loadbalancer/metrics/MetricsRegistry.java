package org.lb4.loadbalancer.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class MetricsRegistry {

    private final AtomicLong totalSessions = new AtomicLong();
    private final AtomicLong activeSessions = new AtomicLong();
    private final AtomicLong failedSessions = new AtomicLong();
    private final AtomicLong bytesForwarded = new AtomicLong();
    private final AtomicLong backendFailures = new AtomicLong();

    public void incrementTotalSessions() {
        totalSessions.incrementAndGet();
    }

    public void incrementActiveSessions() {
        activeSessions.incrementAndGet();
    }

    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }

    public void incrementFailedSessions() {
        failedSessions.incrementAndGet();
    }

    public void addBytesForwarded(long bytes) {
        bytesForwarded.addAndGet(bytes);
    }

    public void incrementBackendFailures() {
        backendFailures.incrementAndGet();
    }

    public long getTotalSessions() {
        return totalSessions.get();
    }

    public long getActiveSessions() {
        return activeSessions.get();
    }

    public long getFailedSessions() {
        return failedSessions.get();
    }

    public long getBytesForwarded() {
        return bytesForwarded.get();
    }

    public long getBackendFailures() {
        return backendFailures.get();
    }
}
