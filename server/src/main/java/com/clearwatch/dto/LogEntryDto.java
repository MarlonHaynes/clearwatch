package com.clearwatch.dto;

import com.clearwatch.domain.LogLevel;

import java.time.Instant;

public record LogEntryDto(
        Long id,
        Long serviceId,
        String serviceName,
        Instant timestamp,
        LogLevel level,
        String message,
        String traceId
) {}
