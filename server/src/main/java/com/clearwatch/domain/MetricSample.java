package com.clearwatch.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "metric_sample", indexes = {
        @Index(name = "idx_metric_service_type_time", columnList = "service_id,metric_type,timestamp")
})
public class MetricSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private double value;

    public MetricSample() {}

    public MetricSample(ServiceEntity service, Instant timestamp, MetricType metricType, double value) {
        this.service = service;
        this.timestamp = timestamp;
        this.metricType = metricType;
        this.value = value;
    }

    public Long getId() { return id; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) { this.metricType = metricType; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
