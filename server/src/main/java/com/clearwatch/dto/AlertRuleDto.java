package com.clearwatch.dto;

import com.clearwatch.domain.Comparator;
import com.clearwatch.domain.MetricType;
import com.clearwatch.domain.Severity;

public record AlertRuleDto(
        Long id,
        String name,
        Long serviceId,
        String serviceName,
        MetricType metricType,
        Comparator comparator,
        double threshold,
        int durationSeconds,
        Severity severity,
        boolean enabled
) {}
