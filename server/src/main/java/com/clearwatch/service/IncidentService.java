package com.clearwatch.service;

import com.clearwatch.domain.*;
import com.clearwatch.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    /** Attaches the alert to an existing open incident for the service, or opens a new one. */
    public Incident openOrAttach(ServiceEntity service, Alert alert, Instant startedAt) {
        Incident incident = incidentRepository
                .findFirstByService_IdAndStatusOrderByStartedAtDesc(service.getId(), IncidentStatus.OPEN)
                .orElseGet(() -> {
                    Incident created = new Incident(
                            alert.getRule().getName() + " on " + service.getName(),
                            service, alert.getSeverity(), startedAt);
                    return incidentRepository.save(created);
                });

        if (alert.getSeverity().ordinal() > incident.getSeverity().ordinal()) {
            incident.setSeverity(alert.getSeverity());
        }
        alert.setIncident(incident);
        return incident;
    }

    public void resolveIfAllAlertsResolved(Incident incident, boolean anyStillFiring, Instant resolvedAt) {
        if (!anyStillFiring && incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(resolvedAt);
            incidentRepository.save(incident);
        }
    }
}
