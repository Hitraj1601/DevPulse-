package com.devpulse.repository;

import com.devpulse.model.entity.IncidentTimelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentTimelineRepository extends JpaRepository<IncidentTimelineEntity, UUID> {
    List<IncidentTimelineEntity> findByIncidentIdOrderByOccurredAtAsc(UUID incidentId);
}
