package com.devpulse.repository;

import com.devpulse.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByOrganizationId(UUID organizationId);
    List<ServiceEntity> findByProjectId(UUID projectId);
    Optional<ServiceEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Optional<ServiceEntity> findByProjectIdAndNameAndEnvironment(UUID projectId, String name, String environment);
}
