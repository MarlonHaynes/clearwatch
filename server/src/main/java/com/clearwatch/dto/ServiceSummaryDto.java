package com.clearwatch.dto;

import com.clearwatch.domain.ServiceStatus;

import java.util.List;

public record ServiceSummaryDto(
        Long id,
        String name,
        String description,
        ServiceStatus status,
        double errorRatePercent,
        double p95LatencyMs,
        double requestRatePerSec,
        List<Double> requestRateSparkline
) {}
