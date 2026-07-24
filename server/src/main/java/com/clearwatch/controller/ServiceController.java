package com.clearwatch.controller;

import com.clearwatch.domain.MetricSample;
import com.clearwatch.domain.MetricType;
import com.clearwatch.domain.ServiceEntity;
import com.clearwatch.dto.MetricPointDto;
import com.clearwatch.dto.ServiceSummaryDto;
import com.clearwatch.repository.MetricSampleRepository;
import com.clearwatch.repository.ServiceRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final MetricSampleRepository metricSampleRepository;

    public ServiceController(ServiceRepository serviceRepository, MetricSampleRepository metricSampleRepository) {
        this.serviceRepository = serviceRepository;
        this.metricSampleRepository = metricSampleRepository;
    }

    @GetMapping
    public List<ServiceSummaryDto> listServices() {
        Instant now = Instant.now();
        Instant sparklineStart = now.minusSeconds(15 * 60);

        return serviceRepository.findAll().stream().map(svc -> {
            double errorRate = latestOrZero(svc, MetricType.ERROR_RATE);
            double p95 = latestOrZero(svc, MetricType.LATENCY_P95);
            double requestRate = latestOrZero(svc, MetricType.REQUEST_RATE);
            List<Double> sparkline = metricSampleRepository
                    .findByService_IdAndMetricTypeAndTimestampBetweenOrderByTimestampAsc(
                            svc.getId(), MetricType.REQUEST_RATE, sparklineStart, now)
                    .stream().map(MetricSample::getValue).toList();

            return new ServiceSummaryDto(svc.getId(), svc.getName(), svc.getDescription(),
                    svc.getCurrentStatus(), errorRate, p95, requestRate, sparkline);
        }).toList();
    }

    @GetMapping("/{id}")
    public ServiceSummaryDto getService(@PathVariable Long id) {
        ServiceEntity svc = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such service: " + id));
        Instant now = Instant.now();
        List<Double> sparkline = metricSampleRepository
                .findByService_IdAndMetricTypeAndTimestampBetweenOrderByTimestampAsc(
                        svc.getId(), MetricType.REQUEST_RATE, now.minusSeconds(900), now)
                .stream().map(MetricSample::getValue).toList();

        return new ServiceSummaryDto(svc.getId(), svc.getName(), svc.getDescription(), svc.getCurrentStatus(),
                latestOrZero(svc, MetricType.ERROR_RATE), latestOrZero(svc, MetricType.LATENCY_P95),
                latestOrZero(svc, MetricType.REQUEST_RATE), sparkline);
    }

    @GetMapping("/{id}/metrics")
    public List<MetricPointDto> getMetrics(@PathVariable Long id,
                                            @RequestParam MetricType type,
                                            @RequestParam(defaultValue = "1h") String window) {
        Instant now = Instant.now();
        Instant from = now.minus(parseWindow(window));
        return metricSampleRepository
                .findByService_IdAndMetricTypeAndTimestampBetweenOrderByTimestampAsc(id, type, from, now)
                .stream().map(m -> new MetricPointDto(m.getTimestamp(), m.getValue())).toList();
    }

    private double latestOrZero(ServiceEntity svc, MetricType type) {
        return metricSampleRepository.findLatest(svc.getId(), type).map(MetricSample::getValue).orElse(0.0);
    }

    private Duration parseWindow(String window) {
        return switch (window) {
            case "15m" -> Duration.ofMinutes(15);
            case "1h" -> Duration.ofHours(1);
            case "24h" -> Duration.ofHours(24);
            default -> Duration.ofHours(1);
        };
    }
}
