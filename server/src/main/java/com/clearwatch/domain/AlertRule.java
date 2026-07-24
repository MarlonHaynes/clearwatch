package com.clearwatch.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_rule")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // null = applies to all services
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Comparator comparator;

    @Column(nullable = false)
    private double threshold;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private boolean enabled = true;

    public AlertRule() {}

    public AlertRule(String name, ServiceEntity service, MetricType metricType, Comparator comparator,
                      double threshold, int durationSeconds, Severity severity, boolean enabled) {
        this.name = name;
        this.service = service;
        this.metricType = metricType;
        this.comparator = comparator;
        this.threshold = threshold;
        this.durationSeconds = durationSeconds;
        this.severity = severity;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) { this.metricType = metricType; }
    public Comparator getComparator() { return comparator; }
    public void setComparator(Comparator comparator) { this.comparator = comparator; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
