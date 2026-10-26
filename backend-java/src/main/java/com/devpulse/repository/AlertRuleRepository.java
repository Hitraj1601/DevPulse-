package com.devpulse.repository;

import com.devpulse.model.entity.AlertRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, UUID> {
    List<AlertRuleEntity> findByServiceIdAndEnabledTrue(UUID serviceId);
    List<AlertRuleEntity> findByServiceIdAndEnvironmentAndEnabledTrue(UUID serviceId, String environment);
    List<AlertRuleEntity> findByOrganizationId(UUID organizationId);
    Optional<AlertRuleEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
