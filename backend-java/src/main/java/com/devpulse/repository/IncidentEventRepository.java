package com.devpulse.repository;

import com.devpulse.model.entity.IncidentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentEventRepository extends JpaRepository<IncidentEventEntity, UUID> {
    List<IncidentEventEntity> findByIncidentId(UUID incidentId);
    boolean existsByIncidentIdAndEventRecordId(UUID incidentId, UUID eventRecordId);
}
