-- ==============================================================================
-- DevPulse AI — Database Schema Migration V2
-- Seed Strategy for Synthetic Demo Data
-- Strictly uses clearly-labeled synthetic sample records. No production secrets.
-- ==============================================================================

-- 1. Insert Synthetic Demo Users (Password: DevPulseDemo123!)
-- BCrypt Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, email, password_hash, full_name, is_active)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'admin@devpulse.demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alex Mercer (Admin)', TRUE),
    ('a0000000-0000-0000-0000-000000000002', 'engineer@devpulse.demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Elena Rostova (Engineer)', TRUE),
    ('a0000000-0000-0000-0000-000000000003', 'viewer@devpulse.demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Marcus Vance (Viewer)', TRUE)
ON CONFLICT (email) DO NOTHING;

-- 2. Insert Synthetic Demo Organization
INSERT INTO organizations (id, slug, name, owner_user_id)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'acme-corp', 'Acme Corporation (Demo)', 'a0000000-0000-0000-0000-000000000001')
ON CONFLICT (slug) DO NOTHING;

-- 3. Insert Memberships (RBAC mapping)
INSERT INTO memberships (id, organization_id, user_id, role)
VALUES 
    ('b0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'a0000000-0000-0000-0000-000000000001', 'OWNER'),
    ('b0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'a0000000-0000-0000-0000-000000000002', 'ENGINEER'),
    ('b0000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'a0000000-0000-0000-0000-000000000003', 'VIEWER')
ON CONFLICT (organization_id, user_id) DO NOTHING;

-- 4. Insert Demo Project
INSERT INTO projects (id, organization_id, name, description)
VALUES 
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Checkout Platform', 'Core ecommerce checkout and payment services')
ON CONFLICT DO NOTHING;

-- 5. Insert Demo Monitored Service
INSERT INTO services (id, organization_id, project_id, name, environment, description, is_active)
VALUES 
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'Order Service', 'production', 'Processes customer checkout transactions and payment authorizations', TRUE)
ON CONFLICT (project_id, name, environment) DO NOTHING;

-- 6. Insert Synthetic Ingestion Key (SHA-256 hash of 'dp_live_demo_secret_key_12345')
-- Raw key for local demo: dp_live_demo_secret_key_12345
-- Hash: d93b39c09bf43fc80d6b63d085600ff5ec0a69cb4529ec9d2737c35dff59a4bb
INSERT INTO ingestion_keys (id, organization_id, service_id, key_prefix, key_hash, name)
VALUES 
    ('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'dp_live_demo', 'd93b39c09bf43fc80d6b63d085600ff5ec0a69cb4529ec9d2737c35dff59a4bb', 'Order Service Production Key')
ON CONFLICT (key_hash) DO NOTHING;

-- 7. Insert "20 events in 60 seconds" Demo Alert Rule
INSERT INTO alert_rules (id, organization_id, service_id, environment, threshold_count, window_seconds, severity, enabled)
VALUES 
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'production', 20, 60, 'HIGH', TRUE)
ON CONFLICT DO NOTHING;
