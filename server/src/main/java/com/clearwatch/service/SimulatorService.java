package com.clearwatch.service;

import com.clearwatch.config.ClearWatchProperties;
import com.clearwatch.domain.*;
import com.clearwatch.repository.*;
import com.clearwatch.simulator.ActiveFailure;
import com.clearwatch.simulator.FailureScenario;
import com.clearwatch.simulator.LogMessages;
import com.clearwatch.simulator.TrafficCurve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Keeps ClearWatch "alive" with zero real infrastructure: emits synthetic metrics/logs on a
 * daily traffic curve, backfills 24h of history on first boot, and periodically injects
 * failure scenarios that breach alert thresholds so the alerting engine and incident
 * timeline are never empty.
 */
@Service
@Order(2)
public class SimulatorService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulatorService.class);
    private static final Map<String, Double> SERVICE_FACTOR = Map.of(
            "auth-service", 1.0,
            "orders-api", 1.3,
            "payments", 0.9,
            "inventory", 0.7,
            "notifications", 0.6,
            "gateway", 1.6
    );
    private static final MetricType[] ALL_METRICS = MetricType.values();

    private final ServiceRepository serviceRepository;
    private final MetricSampleRepository metricSampleRepository;
    private final LogEntryRepository logEntryRepository;
    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final IncidentRepository incidentRepository;
    private final ClearWatchProperties properties;

    private final List<ActiveFailure> activeFailures = new CopyOnWriteArrayList<>();
    private final AtomicReference<Instant> lastFailureTriggeredAt = new AtomicReference<>(Instant.now());

    public SimulatorService(ServiceRepository serviceRepository,
                             MetricSampleRepository metricSampleRepository,
                             LogEntryRepository logEntryRepository,
                             AlertRepository alertRepository,
                             AlertRuleRepository alertRuleRepository,
                             IncidentRepository incidentRepository,
                             ClearWatchProperties properties) {
        this.serviceRepository = serviceRepository;
        this.metricSampleRepository = metricSampleRepository;
        this.logEntryRepository = logEntryRepository;
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.incidentRepository = incidentRepository;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (!properties.getSimulator().isEnabled()) {
            log.info("Simulator disabled via SIMULATOR_ENABLED=false");
            return;
        }
        if (metricSampleRepository.count() == 0) {
            log.info("No existing telemetry found - backfilling 24h of history");
            backfill();
            log.info("Backfill complete");
        }
    }

    // ===================== Live ticking =====================

    @Scheduled(fixedDelay = 5000, initialDelay = 3000)
    @Transactional
    public void tick() {
        if (!properties.getSimulator().isEnabled()) {
            return;
        }
        Instant now = Instant.now();
        activeFailures.removeIf(f -> !f.isActiveAt(now));

        double speed = properties.getSimulator().getSpeed();
        double load = TrafficCurve.loadFactor(now, speed);

        List<ServiceEntity> services = serviceRepository.findAll();
        List<MetricSample> samples = new ArrayList<>();
        List<LogEntry> logs = new ArrayList<>();

        for (ServiceEntity svc : services) {
            ActiveFailure failure = activeFailures.stream()
                    .filter(f -> f.serviceId.equals(svc.getId()) && f.isActiveAt(now))
                    .findFirst().orElse(null);
            generateSamplesAndLogs(svc, now, load, failure, samples, logs);
        }

        metricSampleRepository.saveAll(samples);
        logEntryRepository.saveAll(logs);
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 20_000)
    public void manageFailureScenarios() {
        if (!properties.getSimulator().isEnabled()) {
            return;
        }
        double speed = Math.max(properties.getSimulator().getSpeed(), 0.01);
        long intervalSeconds = (long) ((properties.getSimulator().getFailureIntervalMinutes() * 60L) / speed);
        Instant now = Instant.now();

        if (!activeFailures.isEmpty()) {
            return;
        }
        if (secondsBetween(lastFailureTriggeredAt.get(), now) < intervalSeconds) {
            return;
        }

        List<ServiceEntity> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }
        ServiceEntity target = services.get(ThreadLocalRandom.current().nextInt(services.size()));
        FailureScenario scenario = FailureScenario.values()[ThreadLocalRandom.current().nextInt(FailureScenario.values().length)];
        long durationSeconds = (long) (ThreadLocalRandom.current().nextInt(90, 180) / speed);

        ActiveFailure failure = new ActiveFailure(target.getId(), scenario, now, now.plusSeconds(durationSeconds));
        activeFailures.add(failure);
        lastFailureTriggeredAt.set(now);
        log.info("Injecting failure scenario {} on service '{}' for {}s", scenario, target.getName(), durationSeconds);
    }

    private static long secondsBetween(Instant a, Instant b) {
        return ChronoUnit.SECONDS.between(a, b);
    }

    // ===================== Sample / log generation =====================

    private void generateSamplesAndLogs(ServiceEntity svc, Instant t, double load, ActiveFailure failure,
                                         List<MetricSample> samples, List<LogEntry> logs) {
        double serviceFactor = SERVICE_FACTOR.getOrDefault(svc.getName(), 1.0);
        double intensity = 0;
        if (failure != null) {
            long total = failure.endsAt.getEpochSecond() - failure.startedAt.getEpochSecond();
            long elapsed = t.getEpochSecond() - failure.startedAt.getEpochSecond();
            double frac = total <= 0 ? 0 : Math.min(1.0, Math.max(0.0, (double) elapsed / total));
            intensity = envelope(frac, failure.scenario);
        }

        for (MetricType type : ALL_METRICS) {
            double base = TrafficCurve.baseline(type, load, serviceFactor);
            double distorted = failure != null ? distort(type, base, failure.scenario, intensity) : base;
            double value = TrafficCurve.withNoise(distorted, 0.12);
            samples.add(new MetricSample(svc, t, type, round2(value)));
        }

        emitLogs(svc, t, failure, logs);
    }

    private void emitLogs(ServiceEntity svc, Instant t, ActiveFailure failure, List<LogEntry> logs) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (failure != null) {
            int burst = r.nextInt(2, 6);
            for (int i = 0; i < burst; i++) {
                LogLevel level = r.nextDouble() < 0.65 ? LogLevel.ERROR : LogLevel.WARN;
                String message = level == LogLevel.ERROR ? LogMessages.randomError() : LogMessages.randomWarn();
                logs.add(new LogEntry(svc, t, level, message, LogMessages.traceId()));
            }
        } else if (r.nextDouble() < 0.7) {
            logs.add(new LogEntry(svc, t, LogLevel.INFO, LogMessages.randomInfo(), LogMessages.traceId()));
            if (r.nextDouble() < 0.05) {
                logs.add(new LogEntry(svc, t, LogLevel.WARN, LogMessages.randomWarn(), LogMessages.traceId()));
            }
        }
    }

    private double envelope(double frac, FailureScenario scenario) {
        return switch (scenario) {
            case LATENCY_DEGRADATION -> Math.sin(Math.PI * frac);
            case ERROR_SPIKE, HARD_OUTAGE -> {
                if (frac < 0.1) yield frac / 0.1;
                if (frac > 0.85) yield Math.max(0, (1.0 - frac) / 0.15);
                yield 1.0;
            }
        };
    }

    private double distort(MetricType type, double base, FailureScenario scenario, double intensity) {
        return switch (scenario) {
            case LATENCY_DEGRADATION -> switch (type) {
                case LATENCY_P50, LATENCY_P95, LATENCY_P99 -> base * (1 + 3.0 * intensity);
                case ERROR_RATE -> base * (1 + 1.5 * intensity);
                default -> base;
            };
            case ERROR_SPIKE -> switch (type) {
                case ERROR_RATE -> base + 25.0 * intensity;
                case LATENCY_P50, LATENCY_P95, LATENCY_P99 -> base * (1 + 0.8 * intensity);
                case REQUEST_RATE -> base * (1 - 0.2 * intensity);
                default -> base;
            };
            case HARD_OUTAGE -> switch (type) {
                case REQUEST_RATE -> base * (1 - 0.9 * intensity);
                case ERROR_RATE -> base + 75.0 * intensity;
                case LATENCY_P50, LATENCY_P95, LATENCY_P99 -> base * (1 + 5.0 * intensity);
                case CPU -> Math.min(99, base * (1 + 0.6 * intensity));
                case MEMORY -> Math.min(97, base * (1 + 0.4 * intensity));
            };
        };
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ===================== Backfill =====================

    private void backfill() {
        List<ServiceEntity> services = serviceRepository.findAll();
        Instant now = Instant.now();
        Instant start = now.minus(24, ChronoUnit.HOURS);

        List<SyntheticIncidentWindow> windows = planSyntheticIncidents(services, start, now);

        for (ServiceEntity svc : services) {
            List<MetricSample> samples = new ArrayList<>();
            List<LogEntry> logs = new ArrayList<>();
            double serviceFactor = SERVICE_FACTOR.getOrDefault(svc.getName(), 1.0);
            Instant lastInfoLog = start;

            for (Instant t = start; t.isBefore(now); t = t.plusSeconds(60)) {
                double load = TrafficCurve.loadFactor(t, 1.0);
                SyntheticIncidentWindow window = findWindow(windows, svc.getId(), t);
                double intensity = 0;
                if (window != null) {
                    long total = window.end.getEpochSecond() - window.start.getEpochSecond();
                    long elapsed = t.getEpochSecond() - window.start.getEpochSecond();
                    double frac = total <= 0 ? 0 : Math.min(1.0, Math.max(0.0, (double) elapsed / total));
                    intensity = envelope(frac, window.scenario);
                }

                for (MetricType type : ALL_METRICS) {
                    double base = TrafficCurve.baseline(type, load, serviceFactor);
                    double distorted = window != null ? distort(type, base, window.scenario, intensity) : base;
                    samples.add(new MetricSample(svc, t, type, round2(TrafficCurve.withNoise(distorted, 0.12))));
                }

                if (window != null) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                        LogLevel level = ThreadLocalRandom.current().nextDouble() < 0.65 ? LogLevel.ERROR : LogLevel.WARN;
                        String msg = level == LogLevel.ERROR ? LogMessages.randomError() : LogMessages.randomWarn();
                        logs.add(new LogEntry(svc, t, level, msg, LogMessages.traceId()));
                    }
                } else if (ChronoUnit.SECONDS.between(lastInfoLog, t) >= 25) {
                    logs.add(new LogEntry(svc, t, LogLevel.INFO, LogMessages.randomInfo(), LogMessages.traceId()));
                    lastInfoLog = t;
                }
            }

            metricSampleRepository.saveAll(samples);
            logEntryRepository.saveAll(logs);
        }

        for (SyntheticIncidentWindow window : windows) {
            createHistoricalIncident(window);
        }
    }

    private SyntheticIncidentWindow findWindow(List<SyntheticIncidentWindow> windows, Long serviceId, Instant t) {
        for (SyntheticIncidentWindow w : windows) {
            if (w.serviceId.equals(serviceId) && !t.isBefore(w.start) && t.isBefore(w.end)) {
                return w;
            }
        }
        return null;
    }

    private List<SyntheticIncidentWindow> planSyntheticIncidents(List<ServiceEntity> services, Instant start, Instant now) {
        List<SyntheticIncidentWindow> windows = new ArrayList<>();
        if (services.isEmpty()) {
            return windows;
        }
        long totalSeconds = now.getEpochSecond() - start.getEpochSecond();
        int bucketCount = 3;
        long bucketSeconds = totalSeconds / bucketCount;
        ThreadLocalRandom r = ThreadLocalRandom.current();

        for (int i = 0; i < bucketCount; i++) {
            Instant bucketStart = start.plusSeconds(bucketSeconds * i + r.nextInt(60, (int) Math.max(61, bucketSeconds / 3)));
            long durationSeconds = r.nextInt(3, 8) * 60L;
            ServiceEntity svc = services.get(r.nextInt(services.size()));
            FailureScenario scenario = FailureScenario.values()[r.nextInt(FailureScenario.values().length)];
            windows.add(new SyntheticIncidentWindow(svc.getId(), svc, scenario, bucketStart, bucketStart.plusSeconds(durationSeconds)));
        }
        return windows;
    }

    private void createHistoricalIncident(SyntheticIncidentWindow window) {
        AlertRule primaryRule = ruleFor(window.scenario, true);
        if (primaryRule == null) {
            return;
        }
        Severity severity = primaryRule.getSeverity();
        Incident incident = new Incident(primaryRule.getName() + " on " + window.service.getName(),
                window.service, severity, window.start);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(window.end);
        incident = incidentRepository.save(incident);

        Alert primaryAlert = new Alert(primaryRule, window.service, AlertState.RESOLVED, severity,
                window.start, peakValue(window.scenario, primaryRule));
        primaryAlert.setResolvedAt(window.end);
        primaryAlert.setIncident(incident);
        alertRepository.save(primaryAlert);

        if (window.scenario == FailureScenario.HARD_OUTAGE) {
            AlertRule secondaryRule = ruleFor(window.scenario, false);
            if (secondaryRule != null) {
                Alert secondaryAlert = new Alert(secondaryRule, window.service, AlertState.RESOLVED, secondaryRule.getSeverity(),
                        window.start.plusSeconds(10), peakValue(window.scenario, secondaryRule));
                secondaryAlert.setResolvedAt(window.end.minusSeconds(5));
                secondaryAlert.setIncident(incident);
                alertRepository.save(secondaryAlert);
            }
        }
    }

    private AlertRule ruleFor(FailureScenario scenario, boolean primary) {
        List<AlertRule> rules = alertRuleRepository.findAll();
        return switch (scenario) {
            case LATENCY_DEGRADATION -> findRuleByName(rules, "High p95 latency");
            case ERROR_SPIKE -> findRuleByName(rules, "High error rate");
            case HARD_OUTAGE -> primary
                    ? findRuleByName(rules, "Critical error rate")
                    : findRuleByName(rules, "Request rate collapse");
        };
    }

    private AlertRule findRuleByName(List<AlertRule> rules, String name) {
        return rules.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
    }

    private double peakValue(FailureScenario scenario, AlertRule rule) {
        return switch (scenario) {
            case LATENCY_DEGRADATION -> rule.getThreshold() * 1.4;
            case ERROR_SPIKE -> rule.getThreshold() * 1.6;
            case HARD_OUTAGE -> rule.getComparator() == Comparator.LESS_THAN ? 0.5 : rule.getThreshold() * 2.0;
        };
    }

    private static class SyntheticIncidentWindow {
        final Long serviceId;
        final ServiceEntity service;
        final FailureScenario scenario;
        final Instant start;
        final Instant end;

        SyntheticIncidentWindow(Long serviceId, ServiceEntity service, FailureScenario scenario, Instant start, Instant end) {
            this.serviceId = serviceId;
            this.service = service;
            this.scenario = scenario;
            this.start = start;
            this.end = end;
        }
    }
}
