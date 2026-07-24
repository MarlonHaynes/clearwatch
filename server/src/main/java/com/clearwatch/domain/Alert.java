package com.clearwatch.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private AlertRule rule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "triggering_value", nullable = false)
    private double triggeringValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    public Alert() {}

    public Alert(AlertRule rule, ServiceEntity service, AlertState state, Severity severity,
                 Instant startedAt, double triggeringValue) {
        this.rule = rule;
        this.service = service;
        this.state = state;
        this.severity = severity;
        this.startedAt = startedAt;
        this.triggeringValue = triggeringValue;
    }

    public Long getId() { return id; }
    public AlertRule getRule() { return rule; }
    public void setRule(AlertRule rule) { this.rule = rule; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public AlertState getState() { return state; }
    public void setState(AlertState state) { this.state = state; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public double getTriggeringValue() { return triggeringValue; }
    public void setTriggeringValue(double triggeringValue) { this.triggeringValue = triggeringValue; }
    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }
}
