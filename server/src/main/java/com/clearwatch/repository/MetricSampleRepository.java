package com.clearwatch.repository;

import com.clearwatch.domain.MetricSample;
import com.clearwatch.domain.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MetricSampleRepository extends JpaRepository<MetricSample, Long> {

    List<MetricSample> findByService_IdAndMetricTypeAndTimestampBetweenOrderByTimestampAsc(
            Long serviceId, MetricType metricType, Instant from, Instant to);

    List<MetricSample> findByService_IdAndTimestampBetweenOrderByTimestampAsc(
            Long serviceId, Instant from, Instant to);

    @Query("select m from MetricSample m where m.service.id = :serviceId and m.metricType = :metricType " +
           "order by m.timestamp desc limit 1")
    Optional<MetricSample> findLatest(@Param("serviceId") Long serviceId, @Param("metricType") MetricType metricType);

    @Query("select avg(m.value) from MetricSample m where m.service.id = :serviceId and m.metricType = :metricType " +
           "and m.timestamp >= :since")
    Double averageSince(@Param("serviceId") Long serviceId, @Param("metricType") MetricType metricType, @Param("since") Instant since);

    @Modifying
    @Query("delete from MetricSample m where m.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
