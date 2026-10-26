package com.devpulse.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "deployment_events")
public class DeploymentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(nullable = false, length = 64)
    private String environment = "production";

    @Column(name = "commit_sha", nullable = false, length = 64)
    private String commitSha;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    @Column(length = 255)
    private String author;

    @Column(nullable = false, length = 32)
    private String source = "GITHUB";

    @Column(nullable = false, length = 32)
    private String status = "SUCCESS";

    @CreationTimestamp
    @Column(name = "deployed_at", nullable = false, updatable = false)
    private Instant deployedAt;

    public DeploymentEventEntity() {}

    public DeploymentEventEntity(UUID organizationId, UUID serviceId, String environment, String commitSha, String source) {
        this.organizationId = organizationId;
        this.serviceId = serviceId;
        this.environment = environment != null ? environment : "production";
        this.commitSha = commitSha;
        this.source = source != null ? source : "GITHUB";
        this.status = "SUCCESS";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }

    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getDeployedAt() { return deployedAt; }
    public void setDeployedAt(Instant deployedAt) { this.deployedAt = deployedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentEventEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
