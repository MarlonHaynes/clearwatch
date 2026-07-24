package com.clearwatch.simulator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LogMessages {

    private LogMessages() {}

    private static final List<String> INFO = List.of(
            "Request completed successfully",
            "Handled GET /health check",
            "Processed incoming request in normal bounds",
            "Cache hit for lookup key",
            "Background job completed",
            "Connection pool at healthy utilization",
            "Scheduled task executed",
            "Request routed to upstream dependency"
    );

    private static final List<String> WARN = List.of(
            "Response time exceeded soft threshold",
            "Retrying request after transient failure",
            "Connection pool utilization elevated",
            "Upstream dependency responded slowly",
            "Cache miss rate above normal",
            "Queue depth growing beyond baseline"
    );

    private static final List<String> ERROR = List.of(
            "Request failed with 500 Internal Server Error",
            "Upstream dependency timed out",
            "Database connection pool exhausted",
            "Unhandled exception while processing request",
            "Circuit breaker opened for downstream call",
            "Request failed: connection refused",
            "Health check failed - service unresponsive"
    );

    public static String random(List<String> pool) {
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    public static String randomInfo() { return random(INFO); }
    public static String randomWarn() { return random(WARN); }
    public static String randomError() { return random(ERROR); }

    public static String traceId() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }
}
