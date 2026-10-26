package com.devpulse.repository;

import com.devpulse.model.IncidentStatus;
import com.devpulse.model.entity.IncidentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    @Query("SELECT i FROM IncidentEntity i " +
           "WHERE i.serviceId = :serviceId " +
           "AND i.environment = :environment " +
           "AND i.fingerprint = :fingerprint " +
           "AND i.status IN (com.devpulse.model.IncidentStatus.OPEN, " +
           "                 com.devpulse.model.IncidentStatus.ACKNOWLEDGED, " +
           "                 com.devpulse.model.IncidentStatus.INVESTIGATING, " +
           "                 com.devpulse.model.IncidentStatus.REOPENED)")
    Optional<IncidentEntity> findActiveIncident(
            @Param("serviceId") UUID serviceId,
            @Param("environment") String environment,
            @Param("fingerprint") String fingerprint
    );

    Optional<IncidentEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Page<IncidentEntity> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<IncidentEntity> findByOrganizationIdAndStatusIn(UUID organizationId, Collection<IncidentStatus> statuses, Pageable pageable);

    long countByOrganizationIdAndStatusIn(UUID organizationId, Collection<IncidentStatus> statuses);
}
