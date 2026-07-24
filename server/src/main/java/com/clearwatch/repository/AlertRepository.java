package com.clearwatch.repository;

import com.clearwatch.domain.Alert;
import com.clearwatch.domain.AlertState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStateOrderByStartedAtDesc(AlertState state);

    Optional<Alert> findFirstByRule_IdAndService_IdAndStateOrderByStartedAtDesc(
            Long ruleId, Long serviceId, AlertState state);

    List<Alert> findByIncident_IdOrderByStartedAtAsc(Long incidentId);

    List<Alert> findTop50ByOrderByStartedAtDesc();
}
