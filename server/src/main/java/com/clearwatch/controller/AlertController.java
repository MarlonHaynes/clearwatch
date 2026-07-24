package com.clearwatch.controller;

import com.clearwatch.domain.*;
import com.clearwatch.dto.AlertDto;
import com.clearwatch.dto.AlertRuleDto;
import com.clearwatch.dto.AlertRuleRequest;
import com.clearwatch.dto.Mappers;
import com.clearwatch.repository.AlertRepository;
import com.clearwatch.repository.AlertRuleRepository;
import com.clearwatch.repository.ServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final ServiceRepository serviceRepository;

    public AlertController(AlertRepository alertRepository, AlertRuleRepository alertRuleRepository,
                            ServiceRepository serviceRepository) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/alerts")
    public List<AlertDto> listAlerts(@RequestParam(required = false) AlertState state) {
        List<Alert> alerts = state != null
                ? alertRepository.findByStateOrderByStartedAtDesc(state)
                : alertRepository.findTop50ByOrderByStartedAtDesc();
        return alerts.stream().map(Mappers::toDto).toList();
    }

    @GetMapping("/alert-rules")
    public List<AlertRuleDto> listRules() {
        return alertRuleRepository.findAll().stream().map(Mappers::toDto).toList();
    }

    @PostMapping("/alert-rules")
    public ResponseEntity<AlertRuleDto> createRule(@Valid @RequestBody AlertRuleRequest req) {
        ServiceEntity service = req.serviceId() != null
                ? serviceRepository.findById(req.serviceId())
                    .orElseThrow(() -> new IllegalArgumentException("No such service: " + req.serviceId()))
                : null;
        AlertRule rule = new AlertRule(req.name(), service, req.metricType(), req.comparator(),
                req.threshold(), req.durationSeconds(), req.severity(), req.enabled());
        rule = alertRuleRepository.save(rule);
        return ResponseEntity.ok(Mappers.toDto(rule));
    }

    @PutMapping("/alert-rules/{id}")
    public ResponseEntity<AlertRuleDto> updateRule(@PathVariable Long id, @Valid @RequestBody AlertRuleRequest req) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such alert rule: " + id));
        ServiceEntity service = req.serviceId() != null
                ? serviceRepository.findById(req.serviceId())
                    .orElseThrow(() -> new IllegalArgumentException("No such service: " + req.serviceId()))
                : null;
        rule.setName(req.name());
        rule.setService(service);
        rule.setMetricType(req.metricType());
        rule.setComparator(req.comparator());
        rule.setThreshold(req.threshold());
        rule.setDurationSeconds(req.durationSeconds());
        rule.setSeverity(req.severity());
        rule.setEnabled(req.enabled());
        return ResponseEntity.ok(Mappers.toDto(alertRuleRepository.save(rule)));
    }

    @DeleteMapping("/alert-rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
