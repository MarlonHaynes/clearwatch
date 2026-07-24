package com.clearwatch.dto;

import com.clearwatch.domain.IncidentStatus;
import com.clearwatch.domain.Severity;

import java.time.Instant;
import java.util.List;

public record IncidentDto(
        Long id,
        String title,
        Long serviceId,
        String serviceName,
        Severity severity,
        Instant startedAt,
        Instant resolvedAt,
        IncidentStatus status,
        List<AlertDto> alerts
) {}
