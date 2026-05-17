package org.lb4.loadbalancer.metrics;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsPrinter {

    private final MetricsRegistry metrics;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public MetricsPrinter(MetricsRegistry metrics) {
        this.metrics = metrics;
    }

    public void start(long periodSeconds) {
        scheduler.scheduleAtFixedRate(this::print, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void print() {
        System.out.println("metrics ts=" + Instant.now()
                + " totalSessions=" + metrics.getTotalSessions()
                + " activeSessions=" + metrics.getActiveSessions()
                + " failedSessions=" + metrics.getFailedSessions()
                + " bytesForwarded=" + metrics.getBytesForwarded()
                + " backendFailures=" + metrics.getBackendFailures());
    }
}
