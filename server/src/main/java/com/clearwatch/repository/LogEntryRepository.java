package com.clearwatch.repository;

import com.clearwatch.domain.LogEntry;
import com.clearwatch.domain.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    @Query("select l from LogEntry l where " +
           "(:serviceId is null or l.service.id = :serviceId) and " +
           "(:level is null or l.level = :level) and " +
           "l.timestamp between :from and :to and " +
           "(:search is null or lower(l.message) like lower(concat('%', :search, '%'))) " +
           "order by l.timestamp desc")
    Page<LogEntry> search(@Param("serviceId") Long serviceId,
                          @Param("level") LogLevel level,
                          @Param("from") Instant from,
                          @Param("to") Instant to,
                          @Param("search") String search,
                          Pageable pageable);

    @Modifying
    @Query("delete from LogEntry l where l.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
