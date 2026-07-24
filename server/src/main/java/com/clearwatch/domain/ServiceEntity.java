package com.clearwatch.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "service")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus currentStatus = ServiceStatus.HEALTHY;

    public ServiceEntity() {}

    public ServiceEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ServiceStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(ServiceStatus currentStatus) { this.currentStatus = currentStatus; }
}
