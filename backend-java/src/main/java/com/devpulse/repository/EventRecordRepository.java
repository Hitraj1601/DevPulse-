package com.devpulse.repository;

import com.devpulse.model.entity.EventRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRecordRepository extends JpaRepository<EventRecordEntity, UUID> {

    Optional<EventRecordEntity> findByServiceIdAndEventId(UUID serviceId, UUID eventId);

    Page<EventRecordEntity> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<EventRecordEntity> findByServiceId(UUID serviceId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM EventRecordEntity e " +
           "WHERE e.serviceId = :serviceId " +
           "AND e.fingerprint = :fingerprint " +
           "AND e.occurredAt >= :windowStart " +
           "AND e.occurredAt <= :windowEnd")
    long countMatchingEventsInWindow(
            @Param("serviceId") UUID serviceId,
            @Param("fingerprint") String fingerprint,
            @Param("windowStart") Instant windowStart,
            @Param("windowEnd") Instant windowEnd
    );

    @Query("SELECT e FROM EventRecordEntity e " +
           "WHERE e.serviceId = :serviceId " +
           "AND e.fingerprint = :fingerprint " +
           "AND e.occurredAt >= :since " +
           "ORDER BY e.occurredAt DESC")
    List<EventRecordEntity> findRecentMatchingEvents(
            @Param("serviceId") UUID serviceId,
            @Param("fingerprint") String fingerprint,
            @Param("since") Instant since,
            Pageable pageable
    );
}
