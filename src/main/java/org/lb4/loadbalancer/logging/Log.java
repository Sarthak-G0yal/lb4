package org.lb4.loadbalancer.logging;

import java.time.Instant;

public final class Log {

    private Log() {
    }

    public static void info(String event, String... kv) {
        StringBuilder sb = new StringBuilder();
        sb.append("ts=").append(Instant.now()).append(" event=").append(event);
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append(' ').append(kv[i]).append('=').append(kv[i + 1]);
        }
        System.out.println(sb);
    }
}