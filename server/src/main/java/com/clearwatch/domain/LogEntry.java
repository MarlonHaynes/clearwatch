package com.clearwatch.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "log_entry", indexes = {
        @Index(name = "idx_log_service_time", columnList = "service_id,timestamp")
})
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "trace_id")
    private String traceId;

    public LogEntry() {}

    public LogEntry(ServiceEntity service, Instant timestamp, LogLevel level, String message, String traceId) {
        this.service = service;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.traceId = traceId;
    }

    public Long getId() { return id; }
    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public LogLevel getLevel() { return level; }
    public void setLevel(LogLevel level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
