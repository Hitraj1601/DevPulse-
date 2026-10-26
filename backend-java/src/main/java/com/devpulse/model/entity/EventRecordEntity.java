package com.devpulse.model.entity;

import com.devpulse.model.EventLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "event_records", uniqueConstraints = {
    @UniqueConstraint(name = "uq_event_records_service_event", columnNames = {"service_id", "event_id"})
})
public class EventRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EventLevel level;

    @Column(nullable = false, length = 2048)
    private String message;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(nullable = false, length = 64)
    private String fingerprint;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "span_id", length = 32)
    private String spanId;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json")
    private Map<String, Object> attributes;

    public EventRecordEntity() {}

    public EventRecordEntity(UUID organizationId, UUID serviceId, UUID eventId, Instant occurredAt, 
                             EventLevel level, String message, String errorType, String fingerprint) {
        this.organizationId = organizationId;
        this.serviceId = serviceId;
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.level = level;
        this.message = message;
        this.errorType = errorType;
        this.fingerprint = fingerprint;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public EventLevel getLevel() { return level; }
    public void setLevel(EventLevel level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventRecordEntity that)) return false;
        return Objects.equals(serviceId, that.serviceId) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, eventId);
    }
}
