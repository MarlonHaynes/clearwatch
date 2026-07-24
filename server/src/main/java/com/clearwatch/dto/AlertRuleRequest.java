package com.clearwatch.dto;

import com.clearwatch.domain.Comparator;
import com.clearwatch.domain.MetricType;
import com.clearwatch.domain.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertRuleRequest(
        @NotBlank String name,
        Long serviceId,
        @NotNull MetricType metricType,
        @NotNull Comparator comparator,
        double threshold,
        @Positive int durationSeconds,
        @NotNull Severity severity,
        boolean enabled
) {}
