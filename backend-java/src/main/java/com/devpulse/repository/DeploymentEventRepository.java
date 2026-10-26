package com.devpulse.repository;

import com.devpulse.model.entity.DeploymentEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentEventRepository extends JpaRepository<DeploymentEventEntity, UUID> {
    List<DeploymentEventEntity> findByServiceIdAndEnvironmentOrderByDeployedAtDesc(UUID serviceId, String environment, Pageable pageable);
    List<DeploymentEventEntity> findByServiceIdAndEnvironmentAndDeployedAtBetweenOrderByDeployedAtDesc(UUID serviceId, String environment, Instant start, Instant end);
}
