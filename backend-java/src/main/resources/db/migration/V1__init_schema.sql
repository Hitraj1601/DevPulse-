-- ==============================================================================
-- DevPulse AI — Database Schema Migration V1
-- Target: PostgreSQL 16
-- Multi-tenant application monitoring and incident management schema
-- ==============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- 2. Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    owner_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_organizations_slug UNIQUE (slug),
    CONSTRAINT fk_organizations_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 3. Memberships Table (Multi-tenant RBAC)
CREATE TABLE IF NOT EXISTS memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_memberships_org_user UNIQUE (organization_id, user_id),
    CONSTRAINT fk_memberships_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_memberships_role CHECK (role IN ('OWNER', 'ADMIN', 'ENGINEER', 'VIEWER'))
);

-- 4. Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    archived_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_projects_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_projects_org ON projects (organization_id, name);

-- 5. Services Table
CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    environment VARCHAR(64) NOT NULL DEFAULT 'production',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_services_project_name_env UNIQUE (project_id, name, environment),
    CONSTRAINT fk_services_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_services_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_services_org ON services (organization_id, name);

-- 6. Ingestion Keys Table (Hashed Storage)
CREATE TABLE IF NOT EXISTS ingestion_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    service_id UUID NOT NULL,
    key_prefix VARCHAR(16) NOT NULL,
    key_hash VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    revoked_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ingestion_keys_hash UNIQUE (key_hash),
    CONSTRAINT fk_ingestion_keys_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_ingestion_keys_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_keys_active_lookup ON ingestion_keys (key_hash) WHERE revoked_at IS NULL;

-- 7. Event Records Table (Telemetry Events)
CREATE TABLE IF NOT EXISTS event_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    service_id UUID NOT NULL,
    event_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    level VARCHAR(16) NOT NULL,
    message VARCHAR(2048) NOT NULL,
    error_type VARCHAR(255) NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64),
    span_id VARCHAR(32),
    stack_trace TEXT,
    attributes_json JSONB,
    CONSTRAINT uq_event_records_service_event UNIQUE (service_id, event_id),
    CONSTRAINT fk_event_records_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_records_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT chk_event_records_level CHECK (level IN ('INFO', 'WARN', 'ERROR', 'FATAL'))
);

CREATE INDEX idx_events_service_occurred ON event_records (service_id, occurred_at DESC);
CREATE INDEX idx_events_fingerprint_occurred ON event_records (service_id, fingerprint, occurred_at DESC);
CREATE INDEX idx_events_org_occurred ON event_records (organization_id, occurred_at DESC);

-- 8. Alert Rules Table
CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    service_id UUID NOT NULL,
    environment VARCHAR(64) NOT NULL DEFAULT 'production',
    threshold_count INT NOT NULL DEFAULT 20,
    window_seconds INT NOT NULL DEFAULT 60,
    severity VARCHAR(16) NOT NULL DEFAULT 'HIGH',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alert_rules_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_rules_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT chk_alert_rules_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- 9. Incidents Table
CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    service_id UUID NOT NULL,
    rule_id UUID,
    environment VARCHAR(64) NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    severity VARCHAR(16) NOT NULL DEFAULT 'HIGH',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    event_count INT NOT NULL DEFAULT 1,
    first_event_at TIMESTAMPTZ NOT NULL,
    last_event_at TIMESTAMPTZ NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    assignee_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_incidents_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_rule FOREIGN KEY (rule_id) REFERENCES alert_rules(id) ON DELETE SET NULL,
    CONSTRAINT fk_incidents_assignee FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_incidents_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_incidents_status CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING', 'RESOLVED', 'REOPENED'))
);

-- Race-safe Partial Unique Index: at most ONE active incident per (service, environment, fingerprint)
CREATE UNIQUE INDEX idx_incidents_active_unique ON incidents (service_id, environment, fingerprint)
WHERE status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING', 'REOPENED');

CREATE INDEX idx_incidents_org_status ON incidents (organization_id, status, opened_at DESC);
CREATE INDEX idx_incidents_service_status ON incidents (service_id, status, opened_at DESC);

-- 10. Incident Events Mapping (Evidence References)
CREATE TABLE IF NOT EXISTS incident_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL,
    event_record_id UUID NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_incident_events_pair UNIQUE (incident_id, event_record_id),
    CONSTRAINT fk_incident_events_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_events_record FOREIGN KEY (event_record_id) REFERENCES event_records(id) ON DELETE CASCADE
);

-- 11. Incident Timeline Table (Audit & Workflow History)
CREATE TABLE IF NOT EXISTS incident_timeline (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL,
    action VARCHAR(64) NOT NULL,
    actor_id UUID,
    actor_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    summary VARCHAR(512) NOT NULL,
    details_json JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_timeline_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT chk_timeline_actor_type CHECK (actor_type IN ('USER', 'SYSTEM', 'AI'))
);

CREATE INDEX idx_timeline_incident_occurred ON incident_timeline (incident_id, occurred_at ASC);

-- 12. AI Analyses Table (Advisory Artifacts)
CREATE TABLE IF NOT EXISTS ai_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL,
    requested_by UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    provider VARCHAR(32) NOT NULL DEFAULT 'MOCK',
    model VARCHAR(64) NOT NULL,
    output_json JSONB,
    error_code VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_ai_analyses_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_analyses_user FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_ai_analyses_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- 13. Deployment Events Table (Operational Change Correlation)
CREATE TABLE IF NOT EXISTS deployment_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    service_id UUID NOT NULL,
    environment VARCHAR(64) NOT NULL DEFAULT 'production',
    commit_sha VARCHAR(64) NOT NULL,
    commit_message TEXT,
    author VARCHAR(255),
    source VARCHAR(32) NOT NULL DEFAULT 'GITHUB',
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    deployed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_deployments_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_deployments_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_deployments_service_time ON deployment_events (service_id, environment, deployed_at DESC);

-- 14. Notifications Table (In-App Alerts with Deduplication)
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    incident_id UUID,
    channel VARCHAR(32) NOT NULL DEFAULT 'IN_APP',
    state VARCHAR(32) NOT NULL DEFAULT 'UNREAD',
    dedupe_key VARCHAR(128) NOT NULL,
    payload_json JSONB NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notifications_dedupe UNIQUE (dedupe_key),
    CONSTRAINT fk_notifications_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_state CHECK (state IN ('UNREAD', 'READ', 'DISMISSED'))
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, state) WHERE state = 'UNREAD';

-- 15. Audit Logs Table (Append-Only Sensitive Mutations)
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    actor_id UUID,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID,
    details_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_logs_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_org_created ON audit_logs (organization_id, created_at DESC);

-- 16. Outbox Messages Table (Reliable Transactional Side-Effects)
CREATE TABLE IF NOT EXISTS outbox_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload_json JSONB NOT NULL,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outbox_unprocessed ON outbox_messages (created_at ASC) WHERE processed_at IS NULL;
