package com.clearwatch.simulator;

import com.clearwatch.domain.MetricType;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Produces a realistic daily traffic curve (busy daytime, quiet night, plus noise)
 * and derives correlated baselines for latency/error/cpu/memory from the load factor.
 */
public final class TrafficCurve {

    private TrafficCurve() {}

    /** 0..1 load factor for a given instant, compressed by a speed multiplier so demos can run fast. */
    public static double loadFactor(Instant at, double speedMultiplier) {
        long epochSeconds = at.getEpochSecond();
        double compressedSeconds = epochSeconds * speedMultiplier;
        double hourOfCycle = (compressedSeconds / 3600.0) % 24.0;
        // Peak around "2pm", trough around "3am" -> shift cosine
        double radians = 2 * Math.PI * (hourOfCycle - 14) / 24.0;
        double base = 0.55 + 0.4 * Math.cos(radians);
        return clamp(base, 0.08, 1.0);
    }

    public static double baseline(MetricType type, double load, double serviceFactor) {
        return switch (type) {
            case REQUEST_RATE -> 20 + 180 * load * serviceFactor;
            case LATENCY_P50 -> 40 + 60 * load;
            case LATENCY_P95 -> 90 + 160 * load;
            case LATENCY_P99 -> 150 + 260 * load;
            case ERROR_RATE -> 0.2 + 0.6 * load;
            case CPU -> 15 + 55 * load * serviceFactor;
            case MEMORY -> 30 + 35 * load * serviceFactor;
        };
    }

    public static double withNoise(double baseline, double noiseFraction) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double noise = 1.0 + (r.nextDouble() * 2 - 1) * noiseFraction;
        return Math.max(0, baseline * noise);
    }

    private static double clamp(double v, double min, double max) {
        return Math.min(max, Math.max(min, v));
    }

    public static ZonedDateTime utc(Instant instant) {
        return instant.atZone(ZoneOffset.UTC);
    }
}
