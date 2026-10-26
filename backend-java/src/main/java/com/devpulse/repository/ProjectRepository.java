package com.devpulse.repository;

import com.devpulse.model.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByOrganizationId(UUID organizationId);
    Page<ProjectEntity> findByOrganizationIdAndArchivedFalse(UUID organizationId, Pageable pageable);
    Optional<ProjectEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
