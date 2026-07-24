package com.clearwatch.dto;

import com.clearwatch.domain.Alert;
import com.clearwatch.domain.AlertRule;
import com.clearwatch.domain.Incident;
import com.clearwatch.domain.LogEntry;

import java.util.List;

public final class Mappers {

    private Mappers() {}

    public static LogEntryDto toDto(LogEntry e) {
        return new LogEntryDto(e.getId(), e.getService().getId(), e.getService().getName(),
                e.getTimestamp(), e.getLevel(), e.getMessage(), e.getTraceId());
    }

    public static AlertRuleDto toDto(AlertRule r) {
        return new AlertRuleDto(r.getId(), r.getName(),
                r.getService() != null ? r.getService().getId() : null,
                r.getService() != null ? r.getService().getName() : "All services",
                r.getMetricType(), r.getComparator(), r.getThreshold(), r.getDurationSeconds(),
                r.getSeverity(), r.isEnabled());
    }

    public static AlertDto toDto(Alert a) {
        return new AlertDto(a.getId(), a.getRule().getId(), a.getRule().getName(),
                a.getService().getId(), a.getService().getName(), a.getRule().getMetricType(),
                a.getState(), a.getSeverity(), a.getStartedAt(), a.getResolvedAt(),
                a.getTriggeringValue(), a.getIncident() != null ? a.getIncident().getId() : null);
    }

    public static IncidentDto toDto(Incident i, List<AlertDto> alerts) {
        return new IncidentDto(i.getId(), i.getTitle(), i.getService().getId(), i.getService().getName(),
                i.getSeverity(), i.getStartedAt(), i.getResolvedAt(), i.getStatus(), alerts);
    }
}
