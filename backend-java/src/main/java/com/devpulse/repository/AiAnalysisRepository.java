package com.devpulse.repository;

import com.devpulse.model.entity.AiAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisRepository extends JpaRepository<AiAnalysisEntity, UUID> {
    List<AiAnalysisEntity> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
    Optional<AiAnalysisEntity> findFirstByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
}
