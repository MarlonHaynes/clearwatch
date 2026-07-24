package com.clearwatch.service;

import com.clearwatch.config.ClearWatchProperties;
import com.clearwatch.repository.LogEntryRepository;
import com.clearwatch.repository.MetricSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Trims old metric/log rows so a free-tier Postgres instance doesn't fill up. */
@Service
public class RetentionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RetentionCleanupService.class);

    private final MetricSampleRepository metricSampleRepository;
    private final LogEntryRepository logEntryRepository;
    private final ClearWatchProperties properties;

    public RetentionCleanupService(MetricSampleRepository metricSampleRepository,
                                    LogEntryRepository logEntryRepository,
                                    ClearWatchProperties properties) {
        this.metricSampleRepository = metricSampleRepository;
        this.logEntryRepository = logEntryRepository;
        this.properties = properties;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    @Transactional
    public void cleanup() {
        Instant cutoff = Instant.now().minus(properties.getRetention().getDays(), ChronoUnit.DAYS);
        int metrics = metricSampleRepository.deleteOlderThan(cutoff);
        int logs = logEntryRepository.deleteOlderThan(cutoff);
        if (metrics > 0 || logs > 0) {
            log.info("Retention cleanup removed {} metric samples and {} log entries older than {}",
                    metrics, logs, cutoff);
        }
    }
}
