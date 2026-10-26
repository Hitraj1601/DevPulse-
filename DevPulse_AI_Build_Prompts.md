# DevPulse AI — Part-wise Build Prompts

**Purpose:** Copy one prompt at a time into ChatGPT, Cursor, or another coding assistant. Finish and test the current part before moving to the next.  
**Companion document:** DevPulse AI Software Requirements Specification v1.0, sections 1–15.  
**Project approach:** Java-first vertical slice -> Go ingestion + Redis -> AI API -> hardening. No custom AI model is trained.

## How to use these prompts

1. Create a Git repository named `devpulse-ai` and maintain a `docs/` directory for architectural decisions and milestone notes.
2. Start a coding assistant session and supply the SRS (or at minimum scope, stack, requirement IDs, event contract, and security constraints).
3. Paste the **Master Context Prompt** once. Then paste **Part 0**, review the output, run the code and tests, and commit to Git.
4. Paste the next part only after the previous part's acceptance checklist is satisfied. Supply actual errors and changed files when troubleshooting.
5. Ask the assistant for **complete, runnable changes**; prohibit invented test results, fake integrations, exposed API keys, and TODO-only endpoints.
6. In Windows PowerShell, provide compatible commands; where Docker is required, include Docker Compose instructions.

### Master Context Prompt — paste at the beginning of a coding session

> You are my senior full-stack architect, Java Spring Boot engineer, Go backend engineer, React/TypeScript engineer, and testing mentor. We are building **DevPulse AI**, an AI-assisted application monitoring and incident management platform. Read and strictly follow the attached **DevPulse AI SRS v1.0**. Stack: React + TypeScript + Vite frontend; Java + Spring Boot main API; PostgreSQL + Flyway; Go `net/http` ingestion service; Redis Streams; Spring AI + external Gemini/OpenAI-compatible provider; Docker Compose. Use supported mutually compatible dependency versions and explain the versions you choose. Build **Java first**, with a development-only Java ingestion route; replace its public ingestion responsibility with Go later, without rewriting the incident engine. We are not training an AI model.
>
> Follow these working rules: (1) Stay inside the requested part and protect implemented interfaces. (2) Apply test-driven thinking: show acceptance criteria, proposed file tree changes, complete source code, migrations, tests, and precise PowerShell commands. (3) Use secure defaults: tenant authorization on every route, hashed passwords/API keys, server-side secrets, validated inputs, no unsafe AI auto-actions. (4) Never fabricate successful builds, screenshots, benchmark results, API responses, or tested behavior. Mark assumptions and unverifiable steps clearly. (5) Avoid unnecessary dependencies and premature microservices. (6) Show a manual end-to-end verification for each part and troubleshooting for likely errors. (7) End with a checklist, unresolved risks, and suggested Git commit message. **Do not start the next part** until I ask.

## Part 0 — Architecture, decisions, and environment checks

**Objective:** Lock down conventions before writing core code.  
**SRS mapping:** Sections 2, 4, 8, 13–14.  
**Exit evidence:** Architecture decisions, service responsibilities, API naming, and dependency compatibility written down.

**Copy-ready prompt**

> Start **Part 0** of DevPulse AI. Use the SRS and Master Context. Produce an architecture decision document (ADR-001), module boundaries, final logical architecture diagram (Mermaid), sequence diagrams for event ingestion and AI analysis, an entity relationship diagram, system security/trust boundaries, and a folder structure for a monorepo. Choose and verify compatible supported versions of Java, Spring Boot, Spring AI, Go, Node.js, React, PostgreSQL, and Redis rather than guessing. Define one standard REST error structure and timestamp format (UTC ISO-8601). Define event fingerprinting, 20 events in 60 seconds demo rule, idempotency behavior, and incident-state transitions. Explain what is P0, P1, and P2. Give exact Windows 11 / PowerShell setup and verification commands (`java -version`, `go version`, `node -v`, `docker --version`, etc.), but do not assert outputs you cannot run. Deliver ADR files, setup checklist, acceptance criteria, and no unrelated application logic.

**Acceptance checklist**

- Dependencies/version compatibility documented with official references.
- Mermaid architecture and sequence diagrams are consistent with SRS.
- Developer can verify Java, Go, Node, Docker and Git locally.
- Service boundaries and event contract are agreed before coding.

## Part 1 — Monorepo, infrastructure, and empty running apps

**Objective:** Boot every component with minimal code.  
**SRS mapping:** Architectural foundation.  
**Exit evidence:** React, Spring Boot, Go health endpoint, PostgreSQL and Redis start locally.

**Copy-ready prompt**

> Build **Part 1** only. Create the `devpulse-ai/` monorepo with `frontend/` (React + TypeScript + Vite), `backend-java/` (Spring Boot), `ingestion-go/` (minimal `net/http` server), `demo-application/` placeholder directory, `infrastructure/docker-compose.yml`, and `docs/`. Define `.gitignore`, `.editorconfig`, `.env.example` files, and a useful root README. Configure PostgreSQL and Redis in Docker Compose with health checks and persistent local volumes, and Java API health endpoint with proper configuration separation. Go exposes only `/health` now, not ingestion. React homepage shows connection status to Java `/actuator/health` through a documented development proxy/CORS setup. Give every config file and exact PowerShell start/stop commands. Include a quick smoke test script and avoid committing secrets. Do not implement authentication or incidents yet.

**Acceptance checklist**

- `docker compose up -d` starts dependencies; health checks pass.
- Java and Go health endpoints respond locally; frontend builds and renders.
- `.env.example` lists variables without real credentials.

## Part 2 — PostgreSQL schema, Flyway migrations, and repositories

**Objective:** Establish a correct multi-tenant data model.  
**SRS mapping:** Section 7 and FR-01 to FR-06, FR-17, FR-24–32 storage needs.  
**Exit evidence:** Automated migrations and repository integration tests.

**Copy-ready prompt**

> Implement **Part 2**. Create PostgreSQL Flyway migrations and Java entities/repositories for users, organizations, memberships, projects, services, ingestion_keys, event_records, alert_rules, incidents, incident_events, incident_timeline, ai_analyses, deployment_events, notifications, audit_logs, and outbox_messages. Choose UUID identifiers and `timestamptz`/UTC; define PK/FK/unique constraints including `(service_id, event_id)`, organization scoping, and indexes for incident/event queries. Use Spring Data JPA with carefully chosen mappings: avoid eager-loading everything and avoid exposing entities directly in API DTOs. Include seed strategy for **synthetic** demo data and repository tests using PostgreSQL/Testcontainers. Provide migration commands and explain how to reset a development database without destructive production defaults. Do not build application controllers in this part.

**Acceptance checklist**

- Flyway starts clean on an empty PostgreSQL database.
- Repository tests show FK constraints, unique event key and tenant query filtering.
- No JPA auto-DDL replacing migrations.

## Part 3 — Authentication and tenant-aware RBAC

**Objective:** Make the primary API secure before adding business endpoints.  
**SRS mapping:** FR-01–03, NFR-01–06, AC-01 and AC-07.  
**Exit evidence:** Registration/login and cross-tenant denial tests.

**Copy-ready prompt**

> Implement **Part 3**. Build secure register/login/current-user endpoints and organization membership logic with Spring Security. Make a clear ADR choice between secure cookie sessions and JWT bearer authentication; implement one consistently and protect against its applicable threats (CSRF for cookie-based auth, safe token storage/expiry for JWT). Use BCrypt or Argon2, request validation, duplicate-email handling, generic authentication errors, centralized exception handling, rate limiting for auth endpoints, and role checks. Every organization/project/service/incident route must later use a reusable tenant authorization service. Create organization creation and membership inspection endpoints. Include unit + integration tests for valid login, bad credentials, expired sessions/token, unauthorized requests, and Org A trying to read Org B. Provide Postman/curl examples and PowerShell equivalents. No frontend beyond a brief example of integration contracts.

**Acceptance checklist**

- Login and organization creation run locally.
- Resource/tenant checks are centralized and tested.
- Password hashes and credentials never appear in responses.

## Part 4 — Projects, services, ingestion credentials, and setup UI

**Objective:** Allow a team to register an application.  
**SRS mapping:** FR-04–06; UC-01/02.  
**Exit evidence:** Register service in UI and obtain a key once.

**Copy-ready prompt**

> Implement **Part 4**. Build Spring Boot CRUD APIs and typed DTOs for projects, services, and environments under tenant authorization. Add pagination, input validation, role enforcement, archived status, and OpenAPI examples. Build secure service ingestion key issuance, hashing, prefix display, rotation, and revocation; expose the plaintext key **only once** at creation and never persist it plaintext. React: implement login, organization switcher, projects page, service registration form, service settings, and ingestion key display with a one-time copy warning. Use React Router, React Query/TanStack Query or an equally justified data-fetching layer, typed API client, loading/error/empty states, and accessible forms. Include frontend and Java tests, and a manual onboarding walkthrough.

**Acceptance checklist**

- Admin creates a project/service; viewer cannot modify.
- Key appears only once, and persisted records contain only the hash/prefix.
- React UI works end-to-end with backend routes.

## Part 5 — Java-first event intake, demo service, and events explorer

**Objective:** Prove event-to-dashboard functionality without Go/Redis yet.  
**SRS mapping:** FR-07–10, FR-23; UC-02.  
**Exit evidence:** A real test error from demo app appears on the events screen.

**Copy-ready prompt**

> Implement **Part 5**, keeping Java as the first event-processing entry point. Create a development-only `POST /api/v1/dev/ingest/events` endpoint that accepts a service-scoped ingestion key, derives trusted tenant/service IDs from the key, validates eventId (UUID), occurredAt (UTC ISO-8601), level, message, errorType, traceId, and bounded attributes. Enforce payload/message limits and redact secrets before storage. Create one shared Java `EventProcessor` service so the future Redis consumer can call the **same logic**. Persist events idempotently using `(service_id, event_id)` uniqueness. Create a `demo-application/` Spring Boot app with a controlled endpoint that throws a sanitized synthetic database-timeout style error; send events via HTTP. Build events list/filter/pagination endpoints and a React events explorer. Include tests for malformed input, revoked key, duplicates, cross-tenant access and secret redaction. Keep the dev endpoint disabled outside development. Show a complete end-to-end local demonstration.

**Acceptance checklist**

- Triggering demo failure creates one bounded event with no leaked secret.
- Duplicate event submission does not create a second record.
- The React explorer filters and displays event details.

## Part 6 — Rule engine, incident grouping, and state machine

**Objective:** Turn repeating events into actionable incidents.  
**SRS mapping:** FR-11–14, FR-17–19; AC-04–06.  
**Exit evidence:** 20 matching events in 60 seconds create exactly one incident.

**Copy-ready prompt**

> Implement **Part 6**. Add alert rule CRUD for service + environment, threshold count, window seconds, severity and enabled state. Implement deterministic fingerprinting using tenant/service/environment/normalized error type (document how normalization handles variable IDs and noisy messages). Choose fixed or sliding window explicitly and implement time-boundary tests. Seed a demo rule: 20 matching events within 60 seconds. When threshold crosses, create **one** active incident using a race-safe approach (unique partial index or documented transactional/locking strategy); subsequent matching events update its counts/evidence timeline without opening duplicates. Define incident state transitions and guarded status update API with resolution notes and audit entries. Provide JUnit tests for 19 vs 20 events, window boundaries, different services, out-of-order events, duplicate event IDs, concurrent creates, and unauthorized state mutation. Include an API/manual reproducible demonstration.

**Acceptance checklist**

- Threshold and grouping match written specification.
- Concurrent or duplicate submissions do not duplicate incidents.
- Every transition is role-checked and recorded in timeline.

## Part 7 — React incident dashboard and triage workflow

**Objective:** Make the Java MVP feel like a real developer product.  
**SRS mapping:** FR-17–23; UI requirements.  
**Exit evidence:** Login -> service -> events -> incident -> investigate -> resolve works.

**Copy-ready prompt**

> Implement **Part 7**. Build a polished responsive React TypeScript dashboard with sidebar, organization switcher, service health cards, active incident counters, recent events chart, incident table with filters (severity, status, project/service, time range), and incident details containing event evidence, timeline, and status transitions. Display all metrics from real backend endpoints, never hardcoded demo counters in normal mode. Add typed React Query hooks, auth-protected routes, pagination, loading/empty/error states, keyboard accessibility, and request correlation IDs in API errors. Design meaningful reusable components rather than a monolithic page. Add UI tests and an end-to-end scenario using the demo application's injected failures. Do not add AI or Go integration in this part.

**Acceptance checklist**

- Dashboard displays actual incident counts.
- Engineer resolves an incident and sees the timeline update.
- Viewer cannot see edit controls and remains blocked server-side.

## Part 8 — Go collector, Redis Streams, and reliable Java consumer

**Objective:** Upgrade the functional MVP to a proper event-ingestion pipeline.  
**SRS mapping:** FR-15/16, NFR-07/10/16; AC-02/03/10.  
**Exit evidence:** App -> Go -> Redis -> Java -> PostgreSQL -> dashboard.

**Copy-ready prompt**

> Implement **Part 8** without rewriting working incident/business logic. Go `net/http` exposes `POST /v1/ingest/events`, uses service-scoped key validation (design a secure cached verification or internal service lookup approach; do not share raw key DB data unnecessarily), validates JSON schema/size, applies per-key limits, redacts sensitive values, and publishes normalized events to Redis Stream `telemetry.events.v1`. `202` is returned only after confirmed stream publish; return `429` or `503` with safe error messages for backpressure/unavailability. Java implements a Redis Streams consumer group that calls existing `EventProcessor`, commits database work before `XACK`, has bounded retries/pending-entry recovery/dead-letter handling, and stays idempotent across crash/replay. Add Docker configuration, structured logs, graceful shutdown, Go concurrency/unit tests, Java integration tests, and a load generator for synthetic events. Disable the Java dev ingestion endpoint outside dev. Demonstrate replay and Redis outage outcomes and document durability limits.

**Acceptance checklist**

- End-to-end Go/Redis/Java pipeline works.
- Duplicate/replayed events are safe; no duplicate incident or notification.
- Redis outage produces honest backpressure response, not silent data loss.

## Part 9 — Real-time updates, notifications, and collaboration

**Objective:** Inform the right developer about incident changes.  
**SRS mapping:** FR-20–22; UC-05.  
**Exit evidence:** New incidents appear live and alerts aren't duplicated.

**Copy-ready prompt**

> Implement **Part 9**. Add incident assignment with same-organization member validation. Implement an authenticated SSE endpoint (`/api/v1/organizations/{orgId}/stream`) or justify WebSockets if required; ensure tenant-scope filtering, reconnection strategy, heartbeat, event IDs, and cleanup. On new/updated incident, publish in-app notifications to relevant members. Use outbox/dedupe keys so stream retries do not send duplicates. Build notification menu, unread count, incident link, and an assignment control in React. Optional email integration must be configurable and have a local fake provider; no real email is sent from tests. Add end-to-end tests for alert creation, reconnect, access denial, and duplicate suppression. Do not make the AI provider necessary for alerting.

**Acceptance checklist**

- Dashboard updates automatically without refresh.
- A viewer cannot subscribe to another organization’s event stream.
- Event replay does not send the same notification twice.

## Part 10 — AI incident analysis with Spring AI and external API

**Objective:** Deliver the project's AI value without building or training an AI model.  
**SRS mapping:** FR-24–28; AI section 9; AC-08/09/12.  
**Exit evidence:** A sanitized, evidence-linked, structured analysis is generated for an incident.

**Copy-ready prompt**

> Implement **Part 10**. Use **Spring AI** in the Java backend with an external supported OpenAI/Gemini model configured through environment variables. Provide a mock/fake AI client for tests and offline local development. Engineer/admin users may request analysis on an incident; Java collects a bounded evidence window of event IDs, sanitized error messages, service metadata and timeline. Redact secrets/PII, enforce tenant filtering, limit evidence size and AI call frequency, and treat raw logs as untrusted data (prevent prompt-injection instructions from being obeyed). Require strongly typed validated JSON: summary, possibleCauses with evidenceIds, suggestedChecks, missingEvidence, limitations. Check returned evidenceIds against supplied IDs. Persist analysis status/result/model/time; gracefully handle timeouts, quotas, invalid JSON, and unavailable API without affecting incident data. Frontend shows loading/failure/history and explicitly labels output as advisory. Include a real-provider config guide, safe mock mode, unit/integration tests and cost guardrails. No automatic restart/rollback/shell tools.

**Acceptance checklist**

- Mock analysis passes tests without any real API key.
- Secret-bearing logs are redacted before outbound request.
- AI output links to real event IDs and describes uncertainty.

## Part 11 — GitHub deployments and reviewed postmortems

**Objective:** Connect operational changes with incidents and document learning.  
**SRS mapping:** FR-29–32, UC-06/07.  
**Exit evidence:** Verified deployment timeline and editable postmortem.

**Copy-ready prompt**

> Implement **Part 11**. Receive GitHub deployment events via a dedicated webhook with strict HMAC signature verification over the raw body, allowed event types, idempotent delivery ID handling, and service mapping. Store commit SHA, environment, timestamp, source and status. Show deployments near an incident on its timeline; label them **temporally correlated** rather than proven causes. Add postmortem editing with impact summary, timeline, hypothesis vs verified root cause, contributing factors, remediation tasks, and follow-up owners. Generate an AI first draft only when the incident is resolved, reusing the sanitized evidence pipeline. Require engineer approval before finalizing; revisions and approvals should be audited. Tests must include valid/invalid signatures, retries/duplicate delivery, malicious payload handling and cross-tenant protection.

**Acceptance checklist**

- Forged webhooks are rejected.
- Postmortem finalization requires human action.
- Incident UI differentiates evidence, hypothesis and confirmed conclusion.

## Part 12 — Hardening, documentation, release and measurable demo

**Objective:** Make the platform demonstrable, reproducible and professionally reviewable.  
**SRS mapping:** Sections 10–14; all P0/P1 acceptance criteria.  
**Exit evidence:** One reproducible local run plus evidence-based test/performance report.

**Copy-ready prompt**

> Implement **Part 12**. Audit implemented functionality against every P0/P1 SRS ID and report missing items honestly. Provide complete Docker Compose to run React, Spring Boot, Go, PostgreSQL and Redis with health checks, reasonable local volumes, secrets through environment/config (not Git), and safe start order. Add Java integration tests/Testcontainers, Go race tests and HTTP tests, React component/e2e tests, OWASP-informed tenant access review, key/PII leakage checks, liveness/readiness endpoints, structured logs, lint/format/CI using GitHub Actions. Provide a seedable demo application and a deterministic demo script: register service -> induce 20 errors -> watch incident -> analyze (mock or real API) -> resolve -> export reviewed postmortem. Run/describe k6 or equivalent synthetic load tests; report **measured** throughput, p95 latency, detection delay and test environment only if actually executed. Supply `README.md`, architecture diagrams, API documentation, troubleshooting, test matrix, screenshots checklist, and resume-ready bullet templates with placeholders for verified measurements.

**Acceptance checklist**

- A clean checkout can run from documented commands.
- Security/tenant tests, integration tests, and complete demo pass on the evaluator's machine.
- All claimed measurements and screenshots have reproducible evidence.

## Part 13 (optional) — OpenTelemetry, Prometheus and Loki

**Objective:** Demonstrate deeper observability without losing project focus.  
**SRS mapping:** FR-34, P2.  
**Exit evidence:** Live metrics, logs, and trace IDs linked to incidents.

**Copy-ready prompt**

> Extend DevPulse AI with **Part 13**, but do not replace existing working ingestion/incident workflows. Instrument Java and Go services using OpenTelemetry where supported; propagate trace/correlation IDs. Expose Micrometer/Prometheus metrics for application and ingestion health. Integrate Grafana Loki for higher-volume log searching through a secure, tenant-aware query abstraction and define retention policies. Build a per-incident evidence panel that connects events to nearby metrics and correlated traces. Create reproducible dashboards and synthetic tests; document limits and avoid claims of full distributed tracing until verified. Add instrumentation overhead measurements and verify that external evidence sources cannot bypass tenant boundaries.

## Recovery prompt — when a build or test fails

> We are at **DevPulse AI Part [NUMBER]**. Do not implement a new part. Here is the exact command, the full relevant error output, and the current affected files: [PASTE]. Diagnose the first root cause and propose the smallest safe patch. Distinguish compile errors, misconfiguration, database migration failures and runtime errors. Preserve the existing architecture and API contracts. Give precise file changes, updated commands for Windows PowerShell, and regression tests. Do not claim the issue is fixed until I run and confirm the commands or tool output proves it.

## Review prompt — before moving to the next part

> Review DevPulse AI **Part [NUMBER]** against the SRS and the part's acceptance checklist. Read the changed files/tests and identify missing requirements, authorization gaps, unsafe defaults, data leaks, inconsistent DTOs, unhandled failures and missing documentation. Give a pass/fail table with evidence, prioritized fixes, and a recommended Git commit message. Do not start the next part until blocking issues are addressed.

## Handoff prompt — when starting a fresh coding chat

> We are continuing DevPulse AI, a React + Java Spring Boot + Go + PostgreSQL + Redis Streams + external AI API platform. The attached SRS v1.0 is the product contract. **Completed parts:** [LIST]. **Working modules and API contracts:** [PASTE CURRENT REPO TREE/OPENAPI]. **Current branch/commit:** [HASH]. **Known failures:** [LIST]. **Next part:** [NUMBER]. First summarize confirmed existing behavior and identify exactly which files need changes for the next part. Then implement only that part, following the Master Context rules and its acceptance checklist. Do not recreate existing code or assume previous outputs you cannot see.

## Final completeness checklist

- [ ] Java backend is primary system of record; Go has a justified ingestion responsibility.
- [ ] Tenant isolation is enforced and tested everywhere.
- [ ] Redis event processing is idempotent with retries and acknowledgment.
- [ ] Alerts and incident states follow documented deterministic rules.
- [ ] React shows real data with clear loading and error states.
- [ ] AI API key is server-side, provider failures are handled, and output is advisory.
- [ ] Logs are redacted before sending to the AI provider.
- [ ] Docker, tests, developer onboarding and demo run reproducibly.
- [ ] Performance achievements on the resume are measured, not invented.
