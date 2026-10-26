package com.devpulse.model.entity;

import com.devpulse.model.NotificationState;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(nullable = false, length = 32)
    private String channel = "IN_APP";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationState state = NotificationState.UNREAD;

    @Column(name = "dedupe_key", nullable = false, unique = true, length = 128)
    private String dedupeKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false)
    private Map<String, Object> payload;

    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public NotificationEntity() {}

    public NotificationEntity(UUID organizationId, UUID userId, UUID incidentId, String dedupeKey, Map<String, Object> payload) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.incidentId = incidentId;
        this.dedupeKey = dedupeKey;
        this.payload = payload;
        this.state = NotificationState.UNREAD;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public NotificationState getState() { return state; }
    public void setState(NotificationState state) { this.state = state; }

    public String getDedupeKey() { return dedupeKey; }
    public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationEntity that)) return false;
        return Objects.equals(dedupeKey, that.dedupeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dedupeKey);
    }
}
