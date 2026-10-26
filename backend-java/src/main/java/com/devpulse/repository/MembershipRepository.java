package com.devpulse.repository;

import com.devpulse.model.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {
    List<MembershipEntity> findByUserId(UUID userId);
    List<MembershipEntity> findByOrganizationId(UUID organizationId);
    Optional<MembershipEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
