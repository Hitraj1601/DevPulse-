package com.devpulse.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "incident_events", uniqueConstraints = {
    @UniqueConstraint(name = "uq_incident_events_pair", columnNames = {"incident_id", "event_record_id"})
})
public class IncidentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "event_record_id", nullable = false)
    private UUID eventRecordId;

    @CreationTimestamp
    @Column(name = "linked_at", nullable = false, updatable = false)
    private Instant linkedAt;

    public IncidentEventEntity() {}

    public IncidentEventEntity(UUID incidentId, UUID eventRecordId) {
        this.incidentId = incidentId;
        this.eventRecordId = eventRecordId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }

    public UUID getEventRecordId() { return eventRecordId; }
    public void setEventRecordId(UUID eventRecordId) { this.eventRecordId = eventRecordId; }

    public Instant getLinkedAt() { return linkedAt; }
    public void setLinkedAt(Instant linkedAt) { this.linkedAt = linkedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncidentEventEntity that)) return false;
        return Objects.equals(incidentId, that.incidentId) && Objects.equals(eventRecordId, that.eventRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId, eventRecordId);
    }
}
