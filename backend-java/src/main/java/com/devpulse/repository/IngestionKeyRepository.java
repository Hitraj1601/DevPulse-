package com.devpulse.repository;

import com.devpulse.model.entity.IngestionKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngestionKeyRepository extends JpaRepository<IngestionKeyEntity, UUID> {
    List<IngestionKeyEntity> findByServiceId(UUID serviceId);
    List<IngestionKeyEntity> findByOrganizationId(UUID organizationId);
    Optional<IngestionKeyEntity> findByKeyHash(String keyHash);
    Optional<IngestionKeyEntity> findByKeyHashAndRevokedAtIsNull(String keyHash);
    Optional<IngestionKeyEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
