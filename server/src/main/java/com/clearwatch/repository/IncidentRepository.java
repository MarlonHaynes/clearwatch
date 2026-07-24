package com.clearwatch.repository;

import com.clearwatch.domain.Incident;
import com.clearwatch.domain.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByStartedAtDesc();

    Optional<Incident> findFirstByService_IdAndStatusOrderByStartedAtDesc(Long serviceId, IncidentStatus status);
}
