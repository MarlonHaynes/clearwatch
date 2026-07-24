package com.clearwatch.service;

import com.clearwatch.domain.*;
import com.clearwatch.repository.AlertRepository;
import com.clearwatch.repository.AlertRuleRepository;
import com.clearwatch.repository.MetricSampleRepository;
import com.clearwatch.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AlertEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluationService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final MetricSampleRepository metricSampleRepository;
    private final ServiceRepository serviceRepository;
    private final IncidentService incidentService;

    public AlertEvaluationService(AlertRuleRepository alertRuleRepository,
                                   AlertRepository alertRepository,
                                   MetricSampleRepository metricSampleRepository,
                                   ServiceRepository serviceRepository,
                                   IncidentService incidentService) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertRepository = alertRepository;
        this.metricSampleRepository = metricSampleRepository;
        this.serviceRepository = serviceRepository;
        this.incidentService = incidentService;
    }

    @Scheduled(fixedDelay = 10_000, initialDelay = 15_000)
    @Transactional
    public void evaluate() {
        List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();
        if (rules.isEmpty()) {
            return;
        }
        List<ServiceEntity> allServices = serviceRepository.findAll();
        Instant now = Instant.now();

        for (AlertRule rule : rules) {
            List<ServiceEntity> targets = rule.getService() != null ? List.of(rule.getService()) : allServices;
            for (ServiceEntity svc : targets) {
                evaluateRuleForService(rule, svc, now);
            }
        }

        for (ServiceEntity svc : allServices) {
            recomputeStatus(svc);
        }
    }

    private void evaluateRuleForService(AlertRule rule, ServiceEntity svc, Instant now) {
        Instant since = now.minusSeconds(rule.getDurationSeconds());
        Double avg = metricSampleRepository.averageSince(svc.getId(), rule.getMetricType(), since);
        if (avg == null) {
            return;
        }

        boolean breached = rule.getComparator().test(avg, rule.getThreshold());
        var existingFiring = alertRepository.findFirstByRule_IdAndService_IdAndStateOrderByStartedAtDesc(
                rule.getId(), svc.getId(), AlertState.FIRING);

        if (breached && existingFiring.isEmpty()) {
            Alert alert = new Alert(rule, svc, AlertState.FIRING, rule.getSeverity(), now, avg);
            alertRepository.save(alert);
            incidentService.openOrAttach(svc, alert, now);
            alertRepository.save(alert);
            log.info("ALERT FIRING: rule='{}' service='{}' value={}", rule.getName(), svc.getName(), avg);
        } else if (!breached && existingFiring.isPresent()) {
            Alert alert = existingFiring.get();
            alert.setState(AlertState.RESOLVED);
            alert.setResolvedAt(now);
            alertRepository.save(alert);
            log.info("ALERT RESOLVED: rule='{}' service='{}' value={}", rule.getName(), svc.getName(), avg);

            if (alert.getIncident() != null) {
                boolean anyStillFiring = !alertRepository
                        .findByIncident_IdOrderByStartedAtAsc(alert.getIncident().getId())
                        .stream().filter(a -> a.getState() == AlertState.FIRING).toList().isEmpty();
                incidentService.resolveIfAllAlertsResolved(alert.getIncident(), anyStillFiring, now);
            }
        }
    }

    private void recomputeStatus(ServiceEntity svc) {
        List<Alert> firing = alertRepository.findByStateOrderByStartedAtDesc(AlertState.FIRING).stream()
                .filter(a -> a.getService().getId().equals(svc.getId()))
                .toList();

        ServiceStatus status;
        if (firing.stream().anyMatch(a -> a.getSeverity() == Severity.CRITICAL)) {
            status = ServiceStatus.DOWN;
        } else if (!firing.isEmpty()) {
            status = ServiceStatus.DEGRADED;
        } else {
            status = ServiceStatus.HEALTHY;
        }

        if (svc.getCurrentStatus() != status) {
            svc.setCurrentStatus(status);
            serviceRepository.save(svc);
        }
    }
}
