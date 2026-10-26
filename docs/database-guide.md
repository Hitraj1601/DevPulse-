# DevPulse AI — Database & Migration Guide

## 1. Schema Overview

DevPulse AI uses a multi-tenant relational schema managed via **Flyway** migrations against **PostgreSQL 16**.
All identifiers are `UUID` (`gen_random_uuid()`) and all timestamps are stored in UTC as `TIMESTAMPTZ`.

### 16 Core Tables

| Table | Multi-Tenant Scoping | Purpose |
| :--- | :--- | :--- |
| `users` | Global / Identity | System users with bcrypt-hashed credentials |
| `organizations` | Root Tenant | Organizational boundary for isolation |
| `memberships` | `organization_id` + `user_id` | RBAC roles (`OWNER`, `ADMIN`, `ENGINEER`, `VIEWER`) |
| `projects` | `organization_id` | Logical groupings of services within an org |
| `services` | `organization_id` + `project_id` | Monitored microservices / applications |
| `ingestion_keys` | `organization_id` + `service_id` | SHA-256 hashed API keys for telemetry ingestion |
| `event_records` | `organization_id` + `service_id` | Append-only raw telemetry events |
| `alert_rules` | `organization_id` + `service_id` | Sliding-window threshold alert rules (e.g., 20 in 60s) |
| `incidents` | `organization_id` + `service_id` | Aggregated failure states with partial unique active index |
| `incident_events` | `incident_id` + `event_record_id` | Many-to-many junction linking evidence events to incidents |
| `incident_timeline`| `incident_id` | Append-only audit trail of state transitions & actions |
| `ai_analyses` | `incident_id` | AI root-cause analysis results and recommendations |
| `deployment_events`| `organization_id` + `service_id` | CI/CD deployments correlated with incidents |
| `notifications` | `organization_id` + `user_id` | In-app alerts with deduplication keys |
| `audit_logs` | `organization_id` | Immutable audit log of administrative mutations |
| `outbox_messages` | Global / Async Dispatch | Transactional outbox pattern for side-effects |

---

## 2. Key Constraints & Performance Indexes

### Unique Ingestion Key
`event_records` enforces idempotency with a unique constraint on `(service_id, event_id)`:
```sql
CONSTRAINT uq_event_records_service_event UNIQUE (service_id, event_id)
```

### Race-Safe Active Incident Partial Index
Only one active incident can exist for a given `(service_id, environment, fingerprint)`:
```sql
CREATE UNIQUE INDEX idx_incidents_active_unique ON incidents (service_id, environment, fingerprint)
WHERE status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING', 'REOPENED');
```

### Sliding-Window Ingestion Index
Optimizes `countMatchingEventsInWindow` for the "20 events in 60 seconds" rule:
```sql
CREATE INDEX idx_events_fingerprint_occurred ON event_records (service_id, fingerprint, occurred_at DESC);
CREATE INDEX idx_events_service_occurred ON event_records (service_id, occurred_at DESC);
```

---

## 3. Flyway Migration Versioning

Flyway migrations live in `backend-java/src/main/resources/db/migration/`:
- `V1__init_schema.sql`: Full DDL creation of all 16 tables, foreign keys, unique constraints, and indexes.
- `V2__seed_synthetic_demo_data.sql`: Synthetic demo seed data for Acme Corporation (`acme-corp`), users (`admin@devpulse.demo`, `engineer@devpulse.demo`), a demo project (`Checkout Platform`), service (`Order Service`), demo API key (`dp_live_demo_secret_key_12345`), and demo alert rule (20 events in 60s).

### Strict Schema Validation Policy
In `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Hibernate NEVER modifies or drops production DDL
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

---

## 4. How to Reset the Development Database Safely

> [!CAUTION]
> Never run reset commands against staging or production databases. Development database resets must be explicit.

### Resetting Local Docker Environment (Recommended)
To start clean with an empty database:
```powershell
# 1. Stop containers and destroy PostgreSQL persistent volume
docker compose -f infrastructure/docker-compose.yml down -v

# 2. Boot fresh PostgreSQL and Redis instances
docker compose -f infrastructure/docker-compose.yml up -d

# 3. Start Spring Boot API (Flyway automatically executes V1 & V2)
cd backend-java
mvn spring-boot:run
```

### Resetting via Flyway Clean (Development Only)
If Flyway Clean is enabled in local profile:
```powershell
mvn flyway:clean flyway:migrate
```
*(Flyway `clean` is disabled by default in production configurations to prevent accidental data loss).*

---

## 5. Running Repository Integration Tests

DevPulse AI provides dual testing strategies:
1. **In-Memory Fast Repository Suite (`DataJpaRepositoryTests`)**: Runs with in-memory H2 PostgreSQL compatibility mode for instant feedback on entity mappings, queries, constraints, and multi-tenant isolation.
2. **Containerized PostgreSQL Suite (`RepositoryIntegrationTests`)**: Uses Testcontainers to run tests against an exact replica of `postgres:16.4-alpine`, executing real Flyway migrations and testing PostgreSQL-specific constraints. Includes a graceful fallback condition (`DockerAvailableCondition`) that skips if Docker daemon is offline.

```powershell
# Run all repository tests
cd backend-java
mvn test
```
