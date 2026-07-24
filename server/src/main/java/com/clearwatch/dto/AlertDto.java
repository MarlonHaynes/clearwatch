package com.clearwatch.dto;

import com.clearwatch.domain.AlertState;
import com.clearwatch.domain.MetricType;
import com.clearwatch.domain.Severity;

import java.time.Instant;

public record AlertDto(
        Long id,
        Long ruleId,
        String ruleName,
        Long serviceId,
        String serviceName,
        MetricType metricType,
        AlertState state,
        Severity severity,
        Instant startedAt,
        Instant resolvedAt,
        double triggeringValue,
        Long incidentId
) {}
