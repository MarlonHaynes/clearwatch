package com.clearwatch.controller;

import com.clearwatch.domain.Incident;
import com.clearwatch.dto.AlertDto;
import com.clearwatch.dto.IncidentDto;
import com.clearwatch.dto.Mappers;
import com.clearwatch.repository.AlertRepository;
import com.clearwatch.repository.IncidentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;

    public IncidentController(IncidentRepository incidentRepository, AlertRepository alertRepository) {
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<IncidentDto> listIncidents() {
        return incidentRepository.findAllByOrderByStartedAtDesc().stream()
                .map(i -> Mappers.toDto(i, alertsFor(i)))
                .toList();
    }

    @GetMapping("/{id}")
    public IncidentDto getIncident(@PathVariable Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such incident: " + id));
        return Mappers.toDto(incident, alertsFor(incident));
    }

    private List<AlertDto> alertsFor(Incident incident) {
        return alertRepository.findByIncident_IdOrderByStartedAtAsc(incident.getId())
                .stream().map(Mappers::toDto).toList();
    }
}
