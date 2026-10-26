package com.devpulse.repository;

import com.devpulse.model.NotificationState;
import com.devpulse.model.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<NotificationEntity> findByUserIdAndStateOrderByCreatedAtDesc(UUID userId, NotificationState state, Pageable pageable);
    long countByUserIdAndState(UUID userId, NotificationState state);
    Optional<NotificationEntity> findByDedupeKey(String dedupeKey);
    boolean existsByDedupeKey(String dedupeKey);
}
