DEVPULSE AI
SOFTWARE REQUIREMENTS SPECIFICATION & BUILD PLAYBOOK
Java Spring Boot • Golang • React • PostgreSQL • Redis Streams • External AI API
Version: 1.0Document date: 26 October 2026Scope: Portfolio-grade, phased implementationDeliverable: Requirements, architecture, API/data contracts, acceptance tests, and copy-ready development prompts
The core philosophy: Build a real monitoring product first; add Go for event ingestion and external AI APIs for incident analysis later. No training of proprietary AI models is required.
Document map
Part A — Software Requirements Specification: goals, scope, features, security, data model, architecture, API contracts, AI constraints, nonfunctional requirements, acceptance criteria.
Part B — Part-wise Build Prompts: a reusable master prompt, practical implementation prompts for Parts 0–12, an optional Part 13, failure-resolution prompts, and completion checklist.
Important: This document describes what to build; it is not a claim that the listed software has already been implemented or tested.
PART A — SOFTWARE REQUIREMENTS SPECIFICATION
1. Document control and product definition
1.1 Purpose
This SRS specifies the functional behavior, nonfunctional constraints, interfaces, data requirements, security boundaries, and acceptance tests for DevPulse AI, a platform for developer teams to receive application events, detect repeated failures, organize incidents, and request AI-supported analysis. It also defines an incremental delivery sequence so the project can be implemented by a student developer without prematurely building complex distributed infrastructure.
1.2 Intended readers
Product owner / student developer: Understand scope and implementation priorities.
Frontend engineer: Implement React user journeys and dashboards.
Backend engineer: Implement Spring Boot APIs, security, persistence, and detection.
Go engineer: Implement authenticated event ingestion and queue publishing.
Tester / reviewer: Validate behavior against unambiguous acceptance criteria.
1.3 Problem statement
When an application fails, relevant evidence is scattered across logs, services, deployments, and notification tools. Teams spend time locating events, identifying whether failures are related, and preparing explanations for stakeholders. DevPulse AI aims to consolidate the workflow and speed up investigation; it does not guarantee automated root-cause identification.
1.4 Product goals and success metrics
ID
Goal
Evidence of success
G-01
Monitor a real application
Demo Spring Boot service sends events visible in dashboard.
G-02
Reduce incident noise
Repeated matching events create a single active incident per grouping window.
G-03
Preserve trustworthy evidence
Incident links to source events, timestamps, service, and environment.
G-04
Improve investigation workflow
User can request structured AI analysis and inspect evidence references.
G-05
Demonstrate reliable engineering
Automated tests cover authentication, deduplication, retries, and tenant isolation.
G-06
Demonstrate measured performance
Report event throughput, API latency, and detection delay from reproducible tests.
1.5 Glossary
Term
Definition
Event
One normalized report from a monitored service, usually a log-derived error or warning.
Incident
Group of related events requiring investigation and lifecycle tracking.
Fingerprint
Deterministic grouping key, e.g. service + environment + normalized error type.
Tenant / organization
Isolated customer or team whose data and permissions are separated.
Ingestion key
Rotatable credential authorizing a monitored service to submit events.
At-least-once delivery
Messages may be processed more than once; consumers must implement idempotency.
AI analysis
Advisory summary of evidence provided to an external language model.
P0 / P1 / P2
MVP requirement / portfolio requirement / future enhancement.
2. Scope and boundaries
2.1 In scope
Account authentication, organizations, projects, monitored services, and membership-based authorization.
Authenticated JSON event ingestion with input validation, sensible limits, and redaction.
Searchable recent events; deterministic rule-based alerting and incident grouping.
Incident workflow: open, acknowledged, investigating, resolved, reopened (under rule).
Realtime incident updates and notifications; AI investigation requested by a user.
PostgreSQL persistence; dedicated Go ingestion and Redis Streams in portfolio scope.
Simple deployment-event correlation; postmortem drafting; a demo app with fault injection.
Documentation, tests, Docker Compose, and performance/security checks.
2.2 Deliberately out of scope for MVP
Training or fine-tuning a custom AI model.
Automated production actions such as remote shell commands, restarts, or rollbacks.
Full parity with commercial observability tools; unrestricted long-term log retention.
Payment processing, enterprise SSO, mobile applications, or public SaaS billing.
Claims of proven root causes without human verification.
2.3 Priorities
Priority
Release slice
Included
P0
Java-first vertical slice
Auth, organizations, project/service CRUD, direct event intake, events page, alert rules, incidents, basic dashboard, tests.
P1
Portfolio-grade
Go collector, Redis Streams, consumer reliability, SSE, notifications, AI investigation, Docker, audit logs, deployment events, postmortems.
P2
Expansion
OpenTelemetry/Prometheus/Loki, advanced metrics, tracing, complex policies, rich analytics, enterprise integrations.
2.4 Assumptions and external dependencies
Development on a Windows laptop with VS Code/IntelliJ and Docker Desktop or equivalent; commands should include PowerShell variants.
PostgreSQL and Redis run locally in containers during development.
An AI provider API key may require billing and must remain server-side.
Source applications integrate by making HTTP event submissions initially; OpenTelemetry is optional later.
No real production logs or customer secrets are necessary: use synthetic, clearly labeled demo data.
3. Users, roles, and major use cases
3.1 Actors and permissions
Role
View projects & incidents
Manage services/rules
Manage members/keys
Resolve incidents
Request AI analysis
Organization owner
Yes
Yes
Yes
Yes
Yes
Admin
Yes
Yes
Yes, except ownership transfer
Yes
Yes
Engineer
Yes
No by default
No
Yes
Yes
Viewer
Yes
No
No
No
No
Ingestion client
No UI
Submit events only for scoped service
No
No
No
Membership must be checked server-side for every request. A user may belong to multiple organizations but must explicitly select an active organization context.
3.2 Main user journeys
ID
Use case
Normal flow
UC-01
Onboard team
Create account -> organization -> project -> monitored service -> ingestion key.
UC-02
Register application
Add service name/environment -> obtain credentials -> submit test event -> see ingestion status.
UC-03
Detect incident
Receive repeated events -> group fingerprint -> evaluate window/threshold -> open incident.
UC-04
Investigate incident
Open incident -> inspect evidence/timeline -> optionally request AI analysis -> assign and update status.
UC-05
Respond to notification
Alert appears -> authorized user acknowledges -> team reviews changes and resolves.
UC-06
Investigate a deployment
Receive verified deployment webhook -> display changes near incident start time.
UC-07
Produce postmortem
After resolution -> draft from stored timeline -> human reviews and saves.
4. Logical architecture and responsibilities
4.1 Component boundaries
React/TypeScript UI is responsible for authentication flows, tables, filtering, dashboards, incident pages, and calling authorized Java APIs.Java Spring Boot API is the system of record for organizations, projects, services, users, membership, rules, incidents, notifications, deployment events, AI history, and audit history.Go ingestion service validates scoped ingestion credentials and incoming telemetry events, applies limits, sanitizes fields, and publishes events to Redis Streams.Redis Streams buffers events between Go and the Java consumer; it is not a substitute for durable application records.PostgreSQL stores relational business records, deduplication keys, and bounded demo event records.External AI provider via Spring AI returns structured advisory analysis.SSE or WebSocket pushes new incidents and updates from Java to React.Optional later: Prometheus and Loki for operational metrics and log queries; OpenTelemetry for trace context.
{width=6.7in}
4.2 End-to-end data flow
1.  A monitored application submits a JSON event using a service-scoped ingestion key.
2.  Go authenticates it, validates schema and size, applies field redaction, derives trusted service identity from the key, and publishes an event to Redis Streams.
3.  Java consumes events in a consumer group, verifies event ID uniqueness, persists the event, and evaluates rules.
4.  A matching threshold either creates an incident or updates an active matching incident; it records a timeline and evidence links.
5.  Authorized users see dashboard/SSE notifications. AI analysis runs only when requested (or under a later explicit policy).
6.  AI receives bounded, redacted evidence; its JSON output is validated and saved as an advisory artifact.
7.  An engineer changes incident status and verifies the cause. A reviewed postmortem can then be saved.
MVP shortcut: before Go/Redis exists, Spring Boot accepts events directly at a local development endpoint and runs the same event-processing service. The endpoint is retired or disabled in portfolio mode, leaving a single normalized processing pipeline.
4.3 Reliability and delivery semantics
Redis Streams consumer uses explicit acknowledgment only after committed database processing.
Delivery is treated as at least once; duplicate eventId is ignored safely using a database unique constraint.
Failed processing is retried with capped attempts and eventually moved to a dead-letter queue / quarantine view.
Rate limits and queue-capacity controls protect ingestion. Failed enqueue produces a clear non-success response rather than an untrue acknowledgment.
Streaming persistence depends on Redis configuration; document AOF/backup tradeoffs and do not claim zero data loss.
Side effects such as notifications should use an outbox or equivalent deduplication so retries do not generate duplicate alerts.
5. Functional requirements
Every requirement below uses SHALL to indicate a testable expectation.
5.1 Identity, organizations, and inventory
ID
Priority
Requirement / acceptance criterion
FR-01
P0
The system SHALL register/login users with hashed passwords; unauthorized sessions cannot access API data.
FR-02
P0
The system SHALL support organization creation and membership roles owner/admin/engineer/viewer.
FR-03
P0
The API SHALL enforce tenant ownership/membership on every scoped object by identifier.
FR-04
P0
Authorized users SHALL create, update, list, and archive projects within their organization.
FR-05
P0
Authorized users SHALL manage services with name, environment, project, and active status.
FR-06
P1
Admins SHALL generate, rotate, and revoke service-scoped ingestion credentials; only a key prefix is displayed later.
5.2 Ingestion, events, detection
ID
Priority
Requirement / acceptance criterion
FR-07
P0
The system SHALL accept a valid event JSON with eventId, timestamp, severity, message, and errorType.
FR-08
P0
Invalid timestamp/severity/schema or excessive payload size SHALL return a documented validation error.
FR-09
P0
Events SHALL be scoped to a service/environment and queryable by date, severity, and fingerprint.
FR-10
P0
An identical eventId for the same service SHALL not create duplicate event records or incident counts.
FR-11
P0
Admins SHALL configure threshold/window and severity for a per-service alert rule.
FR-12
P0
Rule evaluation SHALL use an explicit sliding or fixed time window documented in code and tests.
FR-13
P0
Matching errors SHALL group into an incident identified by a stable fingerprint and scope.
FR-14
P0
The system SHALL not create duplicate active incidents for the same fingerprint and rule.
FR-15
P1
Go SHALL authenticate and validate ingestion events before enqueueing, with explicit backpressure behavior.
FR-16
P1
Java SHALL consume Redis Stream entries with acknowledgment, retry, dedupe, and failure isolation.
5.3 Incident workflows and visibility
ID
Priority
Requirement / acceptance criterion
FR-17
P0
Authorized users SHALL see incident list and details with status, severity, service, timestamps, and evidence.
FR-18
P0
Engineers/admins SHALL update incident status according to the declared state machine.
FR-19
P0
The system SHALL maintain an append-only incident timeline including status changes and relevant event links.
FR-20
P1
Authorized users SHALL assign incidents to a member of the same organization.
FR-21
P1
The system SHALL produce in-app notifications without duplicate sends on consumer retries.
FR-22
P1
The UI SHALL receive live incident updates through a server-authenticated realtime channel.
FR-23
P0
The dashboard SHALL display current incident counts, affected services, and recent error activity.
5.4 AI, deployments, and reporting
ID
Priority
Requirement / acceptance criterion
FR-24
P1
An authorized user SHALL request AI analysis for a selected incident; AI shall not run per raw log event.
FR-25
P1
The AI input SHALL be bounded, redacted, and restricted to evidence for the requesting tenant/incident.
FR-26
P1
AI response SHALL validate into typed JSON: summary, possible causes, evidence IDs, suggested checks, uncertainty.
FR-27
P1
AI outputs SHALL be labeled advisory and must not execute operational commands.
FR-28
P1
AI errors, quota limits, timeouts, and malformed outputs SHALL fail gracefully and be recorded without losing incident data.
FR-29
P1
The platform SHALL ingest deployment metadata from a signature-verified webhook or authenticated API.
FR-30
P1
The incident page SHALL display deployments within a configurable correlation window without assuming causation.
FR-31
P1
A user SHALL request an editable AI-drafted postmortem after incident resolution, with review before finalization.
FR-32
P1
Security-sensitive changes SHALL produce immutable audit entries: actor, action, tenant, timestamp, target.
FR-33
P1
Admins SHALL configure retention limits for bounded event storage and support controlled deletion.
FR-34
P2
The platform SHALL correlate OpenTelemetry trace IDs and optionally query Prometheus/Loki evidence.
6. Incident state machine and rule semantics
6.1 Status lifecycle
OPEN -> ACKNOWLEDGED -> INVESTIGATING -> RESOLVEDAllowed shortcuts: OPEN -> INVESTIGATING, OPEN -> RESOLVED (with resolution note).RESOLVED -> REOPENED -> INVESTIGATING for a repeated or manually reopened issue.Only an engineer/admin/owner in the same organization may mutate incident state; transition creates a timeline event. A viewer is read-only.
6.2 Default demonstration rule
A seeded example rule is: 20 occurrences of the same fingerprint in a rolling 60-second window create or update an incident. This is a demo default, not a universally correct threshold. The fingerprint includes tenant, project, service, environment, and normalized error type/template. Use event timestamps with bounded tolerance and define whether late-arriving events affect counts. Rate evaluation should be deterministic and unit-tested at time boundaries.
6.3 Incident severity
LOW, MEDIUM, HIGH, CRITICAL with manually configured rule severity. Do not let the LLM unilaterally change a production incident's severity. Provide clear UI labels and allow authorized users to adjust with audit logging.
7. Data model and persistence
7.1 Relational entities
Entity / table
Essential columns
Notes / constraints
users
id, email, password_hash, created_at
Unique normalized email; do not store plaintext passwords.
organizations
id, name, owner_user_id
May own many projects.
memberships
id, organization_id, user_id, role
Unique (organization_id, user_id).
projects
id, organization_id, name, archived_at
All queries enforce organization_id.
services
id, organization_id, project_id, name, environment
Unique name per project/environment.
ingestion_keys
id, organization_id, service_id, key_hash, prefix, revoked_at
Show raw secret only once; rotate/revoke.
event_records
id, organization_id, service_id, event_id, occurred_at, received_at, severity, error_type, fingerprint, message, attributes_json
Unique (service_id, event_id); retention and paging.
alert_rules
id, organization_id, service_id, threshold_count, window_seconds, severity, enabled
Version/audit rule edits.
incidents
id, organization_id, service_id, rule_id, fingerprint, status, severity, opened_at, resolved_at, assignee_id
At most one active match per intended grouping scope.
incident_events
incident_id, event_record_id
Unique pair; evidence references.
incident_timeline
id, incident_id, actor_id, action, details_json, occurred_at
Includes system events with system actor.
ai_analyses
id, incident_id, requested_by, status, provider, model, output_json, error_code, created_at
Store redaction version and evidence IDs, not raw secrets.
deployment_events
id, organization_id, service_id, source, commit_sha, deployed_at
Verify source/authenticity.
notifications
id, user_id, incident_id, channel, state, dedupe_key
Avoid duplicates across retries.
audit_logs
id, organization_id, actor_id, action, entity_type, entity_id, created_at
Append-only for sensitive changes.
outbox_messages
id, event_type, aggregate_id, payload_json, processed_at
Reliable side-effect publishing.
Use Flyway versioned SQL migrations; do not rely on Hibernate auto-create for real schema changes. Add indexes on (organization_id, occurred_at), (service_id, occurred_at), incident status, and event fingerprint, then justify them with actual queries.
7.2 Example telemetry contract
{
  "eventId": "e9b68ea1-44a0-4f9b-a060-5c98aa1a7be2",
  "occurredAt": "2026-10-26T10:30:00Z",
  "level": "ERROR",
  "message": "Database connection timeout",
  "errorType": "DATABASE_TIMEOUT",
  "traceId": "trace-abc123",
  "attributes": {
    "endpoint": "POST /checkout",
    "httpStatus": 500
  }
}
Identity rule: clients do NOT choose organizationId or serviceId in the trusted ingestion message; the receiving service derives those IDs from its validated ingestion key. Cap message length and attributes; strip access tokens, cookies, authorization headers, and likely PII before persistence and before external AI calls.
8. API and integration contracts
All human-facing APIs are versioned under /api/v1. All responses use consistent JSON errors with a code, message, correlationId, and optional field errors. Do not expose exception stack traces to clients.
Method
Endpoint
Auth
Role / purpose
POST
/api/v1/auth/register
None, rate-limited
Register account.
POST
/api/v1/auth/login
None, rate-limited
Authenticate.
GET
/api/v1/me
User
Current profile.
POST
/api/v1/organizations
User
Create organization.
GET
/api/v1/organizations/{orgId}/projects
Member
Paginated project list.
POST
/api/v1/organizations/{orgId}/projects
Admin/owner
Create project.
POST
/api/v1/projects/{projectId}/services
Admin/owner
Register service.
POST
/api/v1/services/{serviceId}/keys
Admin/owner
Issue scoped ingestion key.
POST
/api/v1/services/{serviceId}/rules
Admin/owner
Create incident rule.
GET
/api/v1/services/{serviceId}/events
Member
Paginated filtered events.
GET
/api/v1/organizations/{orgId}/incidents
Member
Filter incidents.
GET
/api/v1/incidents/{incidentId}
Member
Incident detail/evidence.
PATCH
/api/v1/incidents/{incidentId}/status
Engineer+
Transition and log status.
POST
/api/v1/incidents/{incidentId}/analyses
Engineer+
Queue AI request.
GET
/api/v1/incidents/{incidentId}/analyses
Member
Analysis history.
GET
/api/v1/organizations/{orgId}/dashboard
Member
Summary counters.
GET
/api/v1/organizations/{orgId}/stream
Member
Authenticated SSE.
POST
/api/v1/webhooks/github/deployments
Signed webhook
Deployment event intake.
POST
/v1/ingest/events (Go)
Scoped ingestion key
Ingest event; do not expose user JWT auth here.
MVP only: permit a Java POST /api/v1/dev/ingest/events route behind development-only configuration and a scoped dev key; remove/disable when Go ingestion is operational.
8.1 Ingestion response codes
202 Accepted: event verified and durably accepted to the extent guaranteed by configured queue storage; include request/event identifier.
400: invalid JSON or schema, 401/403: invalid/revoked key, 413: too large, 429: rate-limited, 503: queue not accepting writes.
A duplicate eventId should not create repeat effects; specify consistent acceptance behavior and document it.
8.2 Frontend screens
Authentication, organization switcher, projects/services, service settings/ingestion keys.
Overview with healthy/degraded services, incidents, event counts, filters and time-range selection.
Events explorer with severity, service, timestamp, request correlation; pagination.
Incident list/details with evidence timeline, status, assignment, analysis and deployment context.
Rules and notifications; administration and organization membership.
9. AI API design and safety
9.1 Intended AI tasks
Summarize an incident using only supplied evidence.
Suggest hypotheses and verification checks; label unknowns and conflicting evidence.
Explain technical logs in accessible language without hiding uncertainty.
Draft postmortems from a verified timeline; require human review and editing.
9.2 Required structured analysis output
{
  "summary": "Checkout requests failed with database timeouts.",
  "possibleCauses": [
    {"hypothesis": "Connection pool exhaustion", "confidence": "possible", "evidenceIds": ["evt-123"]}
  ],
  "suggestedChecks": ["Inspect connection-pool utilization and timeout settings"],
  "missingEvidence": ["Pool saturation metrics"],
  "limitations": "No database metric data was provided."
}
Use DTO validation. evidenceIds must be among provided event IDs; reject or flag unsupported citations. Confidence is qualitative unless a validated numeric scoring methodology exists. Do not make up statistics or present LLM output as a confirmed root cause.
9.3 Prompt-injection, cost, and privacy controls
Treat event messages, stack traces, and deployment text as untrusted data, even if they contain instructions to the LLM.
Use server-controlled system instructions; strictly delimit untrusted evidence; never pass provider keys or internal credentials.
Filter/redact secrets and PII before using third-party AI. Implement prompt budget, context length cap, timeouts, token/cost tracking, and per-organization usage limits.
Require explicit user action to trigger analysis in P1; cache or deduplicate repeated requests if evidence hasn't changed.
AI unavailability must not interrupt telemetry ingestion or incident detection.
No direct tools or production mutation permissions are granted to the AI service.
10. Nonfunctional requirements
Performance targets are acceptance goals under declared test conditions, NOT achieved measurements. Record actual laptop/hardware resources, dataset, concurrent clients, and benchmark commands in the final performance report.
ID
Category
Requirement / test target
NFR-01
Security
All authenticated API operations verify token/session and tenant resource membership.
NFR-02
Passwords
Passwords use a current secure adaptive hash, e.g. bcrypt/Argon2; never plaintext.
NFR-03
Transport
Production endpoints use HTTPS; no public access to internal Redis/PostgreSQL ports.
NFR-04
Secrets
Secret values never appear in Git, frontend bundles, logs, or returned key-history records.
NFR-05
Input limits
Bound payload size, field lengths, request rates, and API pagination sizes.
NFR-06
Privacy
Logs are redacted before AI and follow explicit retention/deletion policy.
NFR-07
Reliability
Duplicated stream deliveries do not duplicate events/incident counts/notifications.
NFR-08
Failure
If AI fails, the incident system remains usable and displays a recoverable error.
NFR-09
Latency
Target p95 typical authenticated CRUD API under 300 ms at 20 req/s locally, excluding external AI latency; measure before claiming.
NFR-10
Ingestion
Aim to sustain 100 valid events/s on documented local setup; report achieved rate, queue lag, error rate and p95 accept latency.
NFR-11
Detection
In controlled tests, detection delay should stay within 60 s after threshold crossing; define window/evaluation clocks.
NFR-12
Accessibility
Responsive UI, keyboard navigation, labels, contrast checks; verify with axe and manual review.
NFR-13
Observability
Each service exposes health/readiness, structured logs, and request/trace correlation IDs.
NFR-14
Testability
Unit, integration, authorization, and end-to-end test suites run reproducibly.
NFR-15
Maintainability
Clear ownership by service, migrations, OpenAPI documentation, typed interfaces, linting.
NFR-16
Recovery
Document restart/replay behavior, backups, and how to handle pending stream records.
NFR-17
Availability
No unsupported 99.9% promise for a single local deployment; define SLO only after hosting design and measurement.
NFR-18
AI transparency
Every analysis shows generation time, status, evidence, and advisory disclaimer.
11. Threat model and security controls
Threat / mistake
Mitigation and test
Cross-tenant ID access (IDOR)
Repository queries scoped by tenant plus authorization service; automated negative tests.
Leaked ingestion key
Store hash; reveal once; revoke and rotate; limit service scope.
Malicious event spam
Per-key rate limits, payload caps, and queue backpressure.
Sensitive logs exposed to AI provider
Preprocessing, allowlist context, automated secret-redaction tests.
Prompt injection in logs
Treat logs as data only, no model tool execution, output validation.
Duplicate message delivery
Unique event constraint, idempotent consumer, notification dedupe key.
Forged deployment webhook
Verify provider signature against raw request body and reject invalid payloads.
AI-generated inaccurate action
Show hypotheses only; no automatic production mutations.
SSRF or unrestricted external URL fetch
Do not fetch user-supplied URLs server-side without strict allowlisting.
Unbounded data retention
Configurable TTL/archive policy and cleanup jobs, tenant-scoped.
12. Testing, scenarios, and acceptance gates
12.1 Test pyramid
Java unit tests: incident grouping, window boundaries, severity, transitions, permissions, DTO validation.
Java integration tests: controllers, JPA/Flyway, PostgreSQL and Redis (Testcontainers where appropriate), transaction/idempotency behavior.
Go tests: JSON validation, authentication, rate limiting, Redis publisher errors, parallel ingestion, graceful shutdown.
Frontend tests: route guards, incident filters, mutation flows, accessibility checks.
End-to-end: demo app sends events -> incident displayed -> AI analysis -> user resolves incident.
Load/reliability: generate synthetic events, disconnect Redis temporarily, restart consumer, replay duplicates, inspect metrics.
12.2 Concrete acceptance scenarios
ID
Given
When
Then
AC-01
User in Org A
Requests Org B's project by ID
API returns forbidden/not found without data leak.
AC-02
A service key was revoked
Client submits event
Request fails; event not enqueued.
AC-03
A valid event already exists
Same service sends same eventId
One stored event, no doubled incident count.
AC-04
Rule is 20 events/60 s
20 matching errors arrive within window
Exactly one open incident appears.
AC-05
Two different services fail
Both cross threshold
Two scoped incidents appear, not one mixed incident.
AC-06
An incident is open
Engineer resolves with a note
Transition/timeline/audit recorded.
AC-07
Viewer opens incident
Viewer tries to change severity/status
Server denies request.
AC-08
AI provider is unavailable
Engineer requests analysis
Recoverable failed/pending state; existing incident unaffected.
AC-09
Malicious text in an event says "ignore rules"
AI evaluates incident
Text is treated as evidence, not instructions.
AC-10
Redis message was processed before crash
Message is redelivered
Consumer does not duplicate data or send second alert.
AC-11
Invalid GitHub webhook signature
Webhook submitted
No deployment entry created.
AC-12
Incident evidence contains a fake API key
AI analysis requested
Secret is redacted before outbound request.
12.3 Definition of done for each increment
1.  Feature works end-to-end on local development setup and respects organization boundaries.
2.  Required tests pass and commands are included in README (including Windows PowerShell options).
3.  Database changes are migrated, validated, and reversible where practical.
4.  Inputs/errors/loading/empty states are handled, with no real secrets hardcoded.
5.  Documentation contains example API calls and a manual verification scenario.
6.  Scope is limited to the current part; no unexplained unrelated refactor is introduced.
13. Delivery roadmap and dependencies
Part
Focus
Dependency
Exit evidence
0
Requirements, architecture, conventions
None
Agreed contracts and repo plan.
1
Monorepo, Docker and app skeletons
0
React/Java/Go skeletons and DB/Redis boot.
2
Java data model + Flyway
1
Schema migrates and repository tests pass.
3
Auth + tenant RBAC
2
Negative cross-tenant tests pass.
4
Project/service APIs + React setup
3
Authenticated service registration works.
5
Java-first event ingestion + events explorer
4
Demo app event appears in UI.
6
Alert rules + incidents
5
Threshold generates one incident with evidence.
7
Incident dashboard and workflows
6
Triage/resolution works end-to-end.
8
Go ingestion + Redis reliability
7
Go -> Redis -> Java processing works under retry.
9
Notifications + SSE
8
Live incident appears without manual refresh.
10
External AI investigations
9
Structured, evidence-linked analysis with fallback.
11
Deployment correlation + postmortems
10
Verified deployments/timeline and reviewed draft.
12
Hardening, Docker, docs, tests, deployment
11
Reproducible complete demo + benchmark report.
13 (optional)
OpenTelemetry, Prometheus, Loki
12
Correlated observability signals and advanced views.
14. Risks, constraints, and open decisions
Risk
Planned response
Too many technologies at once
Require Java-first end-to-end slice before Go microservice.
AI hallucination
Provide evidence IDs and uncertainty; require human review.
High API costs
Manual analysis, limits, mock provider, provider-agnostic configuration.
Huge log storage footprint
Bounded retention, pagination, later use a dedicated log backend.
Race conditions around grouping
Transactional constraints/locking or tested safe concurrency strategy.
Redis loss during local shutdown
Local persistence configured; replay limitations documented.
Unclear ownership of monitoring instrumentation
Demo service integration contract explicitly included.
Decisions to document before coding: supported JDK/Spring Boot/Spring AI compatibility; session strategy (secure cookie vs bearer JWT); incident-window semantics; Go framework (start with standard net/http); Redis persistence policy; outgoing email provider; and hosting environment. Choose stable supported versions together, verify their compatibility from official documentation, and record choices in an ADR rather than inventing version numbers.
15. Source and standards references
Spring AI ChatClient documentation: https://docs.spring.io/spring-ai/reference/api/chatclient.html
Spring AI structured outputs: https://docs.spring.io/spring-ai/reference/api/structured-output.html
Redis Streams documentation: https://redis.io/docs/latest/develop/data-types/streams/
OpenTelemetry signals: https://opentelemetry.io/docs/concepts/signals/
OWASP API Security Top 10: https://owasp.org/API-Security/
Spring Boot Actuator reference: https://docs.spring.io/spring-boot/reference/actuator/index.html
Important: This is a project specification, not an independent security audit or guarantee of production readiness. External service prices, model availability, library versions, and vendor policies may change; verify during implementation.
PART B — PART-WISE IMPLEMENTATION PROMPTS
How to use these prompts
1.  Create a Git repository named devpulse-ai and maintain a docs/ directory for architectural decisions and milestone notes.
2.  Start a coding assistant session and supply the SRS (or at minimum scope, stack, requirement IDs, event contract, and security constraints).
3.  Paste the Master Context Prompt once. Then paste Part 0, review the output, run the code and tests, and commit to Git.
4.  Paste the next part only after the previous part's acceptance checklist is satisfied. Supply actual errors and changed files when troubleshooting.
5.  Ask the assistant for complete, runnable changes; prohibit invented test results, fake integrations, exposed API keys, and TODO-only endpoints.
6.  In Windows PowerShell, provide compatible commands; where Docker is required, include Docker Compose instructions.
Master Context Prompt — paste at the beginning of a coding session
You are my senior full-stack architect, Java Spring Boot engineer, Go backend engineer, React/TypeScript engineer, and testing mentor. We are building DevPulse AI, an AI-assisted application monitoring and incident management platform. Read and strictly follow the attached DevPulse AI SRS v1.0. Stack: React + TypeScript + Vite frontend; Java + Spring Boot main API; PostgreSQL + Flyway; Go net/http ingestion service; Redis Streams; Spring AI + external Gemini/OpenAI-compatible provider; Docker Compose. Use supported mutually compatible dependency versions and explain the versions you choose. Build Java first, with a development-only Java ingestion route; replace its public ingestion responsibility with Go later, without rewriting the incident engine. We are not training an AI model.
Follow these working rules: (1) Stay inside the requested part and protect implemented interfaces. (2) Apply test-driven thinking: show acceptance criteria, proposed file tree changes, complete source code, migrations, tests, and precise PowerShell commands. (3) Use secure defaults: tenant authorization on every route, hashed passwords/API keys, server-side secrets, validated inputs, no unsafe AI auto-actions. (4) Never fabricate successful builds, screenshots, benchmark results, API responses, or tested behavior. Mark assumptions and unverifiable steps clearly. (5) Avoid unnecessary dependencies and premature microservices. (6) Show a manual end-to-end verification for each part and troubleshooting for likely errors. (7) End with a checklist, unresolved risks, and suggested Git commit message. Do not start the next part until I ask.
Part 0 — Architecture, decisions, and environment checks
Objective: Lock down conventions before writing core code.SRS mapping: Sections 2, 4, 8, 13–14.Exit evidence: Architecture decisions, service responsibilities, API naming, and dependency compatibility written down.
Copy-ready prompt
Start Part 0 of DevPulse AI. Use the SRS and Master Context. Produce an architecture decision document (ADR-001), module boundaries, final logical architecture diagram (Mermaid), sequence diagrams for event ingestion and AI analysis, an entity relationship diagram, system security/trust boundaries, and a folder structure for a monorepo. Choose and verify compatible supported versions of Java, Spring Boot, Spring AI, Go, Node.js, React, PostgreSQL, and Redis rather than guessing. Define one standard REST error structure and timestamp format (UTC ISO-8601). Define event fingerprinting, 20 events in 60 seconds demo rule, idempotency behavior, and incident-state transitions. Explain what is P0, P1, and P2. Give exact Windows 11 / PowerShell setup and verification commands (java -version, go version, node -v, docker --version, etc.), but do not assert outputs you cannot run. Deliver ADR files, setup checklist, acceptance criteria, and no unrelated application logic.
Acceptance checklist
Dependencies/version compatibility documented with official references.
Mermaid architecture and sequence diagrams are consistent with SRS.
Developer can verify Java, Go, Node, Docker and Git locally.
Service boundaries and event contract are agreed before coding.
Part 1 — Monorepo, infrastructure, and empty running apps
Objective: Boot every component with minimal code.SRS mapping: Architectural foundation.Exit evidence: React, Spring Boot, Go health endpoint, PostgreSQL and Redis start locally.
Copy-ready prompt
Build Part 1 only. Create the devpulse-ai/ monorepo with frontend/ (React + TypeScript + Vite), backend-java/ (Spring Boot), ingestion-go/ (minimal net/http server), demo-application/ placeholder directory, infrastructure/docker-compose.yml, and docs/. Define .gitignore, .editorconfig, .env.example files, and a useful root README. Configure PostgreSQL and Redis in Docker Compose with health checks and persistent local volumes, and Java API health endpoint with proper configuration separation. Go exposes only /health now, not ingestion. React homepage shows connection status to Java /actuator/health through a documented development proxy/CORS setup. Give every config file and exact PowerShell start/stop commands. Include a quick smoke test script and avoid committing secrets. Do not implement authentication or incidents yet.
Acceptance checklist
docker compose up -d starts dependencies; health checks pass.
Java and Go health endpoints respond locally; frontend builds and renders.
.env.example lists variables without real credentials.
Part 2 — PostgreSQL schema, Flyway migrations, and repositories
Objective: Establish a correct multi-tenant data model.SRS mapping: Section 7 and FR-01 to FR-06, FR-17, FR-24–32 storage needs.Exit evidence: Automated migrations and repository integration tests.
Copy-ready prompt
Implement Part 2. Create PostgreSQL Flyway migrations and Java entities/repositories for users, organizations, memberships, projects, services, ingestion_keys, event_records, alert_rules, incidents, incident_events, incident_timeline, ai_analyses, deployment_events, notifications, audit_logs, and outbox_messages. Choose UUID identifiers and timestamptz/UTC; define PK/FK/unique constraints including (service_id, event_id), organization scoping, and indexes for incident/event queries. Use Spring Data JPA with carefully chosen mappings: avoid eager-loading everything and avoid exposing entities directly in API DTOs. Include seed strategy for synthetic demo data and repository tests using PostgreSQL/Testcontainers. Provide migration commands and explain how to reset a development database without destructive production defaults. Do not build application controllers in this part.
Acceptance checklist
Flyway starts clean on an empty PostgreSQL database.
Repository tests show FK constraints, unique event key and tenant query filtering.
No JPA auto-DDL replacing migrations.
Part 3 — Authentication and tenant-aware RBAC
Objective: Make the primary API secure before adding business endpoints.SRS mapping: FR-01–03, NFR-01–06, AC-01 and AC-07.Exit evidence: Registration/login and cross-tenant denial tests.
Copy-ready prompt
Implement Part 3. Build secure register/login/current-user endpoints and organization membership logic with Spring Security. Make a clear ADR choice between secure cookie sessions and JWT bearer authentication; implement one consistently and protect against its applicable threats (CSRF for cookie-based auth, safe token storage/expiry for JWT). Use BCrypt or Argon2, request validation, duplicate-email handling, generic authentication errors, centralized exception handling, rate limiting for auth endpoints, and role checks. Every organization/project/service/incident route must later use a reusable tenant authorization service. Create organization creation and membership inspection endpoints. Include unit + integration tests for valid login, bad credentials, expired sessions/token, unauthorized requests, and Org A trying to read Org B. Provide Postman/curl examples and PowerShell equivalents. No frontend beyond a brief example of integration contracts.
Acceptance checklist
Login and organization creation run locally.
Resource/tenant checks are centralized and tested.
Password hashes and credentials never appear in responses.
Part 4 — Projects, services, ingestion credentials, and setup UI
Objective: Allow a team to register an application.SRS mapping: FR-04–06; UC-01/02.Exit evidence: Register service in UI and obtain a key once.
Copy-ready prompt
Implement Part 4. Build Spring Boot CRUD APIs and typed DTOs for projects, services, and environments under tenant authorization. Add pagination, input validation, role enforcement, archived status, and OpenAPI examples. Build secure service ingestion key issuance, hashing, prefix display, rotation, and revocation; expose the plaintext key only once at creation and never persist it plaintext. React: implement login, organization switcher, projects page, service registration form, service settings, and ingestion key display with a one-time copy warning. Use React Router, React Query/TanStack Query or an equally justified data-fetching layer, typed API client, loading/error/empty states, and accessible forms. Include frontend and Java tests, and a manual onboarding walkthrough.
Acceptance checklist
Admin creates a project/service; viewer cannot modify.
Key appears only once, and persisted records contain only the hash/prefix.
React UI works end-to-end with backend routes.
Part 5 — Java-first event intake, demo service, and events explorer
Objective: Prove event-to-dashboard functionality without Go/Redis yet.SRS mapping: FR-07–10, FR-23; UC-02.Exit evidence: A real test error from demo app appears on the events screen.
Copy-ready prompt
Implement Part 5, keeping Java as the first event-processing entry point. Create a development-only POST /api/v1/dev/ingest/events endpoint that accepts a service-scoped ingestion key, derives trusted tenant/service IDs from the key, validates eventId (UUID), occurredAt (UTC ISO-8601), level, message, errorType, traceId, and bounded attributes. Enforce payload/message limits and redact secrets before storage. Create one shared Java EventProcessor service so the future Redis consumer can call the same logic. Persist events idempotently using (service_id, event_id) uniqueness. Create a demo-application/ Spring Boot app with a controlled endpoint that throws a sanitized synthetic database-timeout style error; send events via HTTP. Build events list/filter/pagination endpoints and a React events explorer. Include tests for malformed input, revoked key, duplicates, cross-tenant access and secret redaction. Keep the dev endpoint disabled outside development. Show a complete end-to-end local demonstration.
Acceptance checklist
Triggering demo failure creates one bounded event with no leaked secret.
Duplicate event submission does not create a second record.
The React explorer filters and displays event details.
Part 6 — Rule engine, incident grouping, and state machine
Objective: Turn repeating events into actionable incidents.SRS mapping: FR-11–14, FR-17–19; AC-04–06.Exit evidence: 20 matching events in 60 seconds create exactly one incident.
Copy-ready prompt
Implement Part 6. Add alert rule CRUD for service + environment, threshold count, window seconds, severity and enabled state. Implement deterministic fingerprinting using tenant/service/environment/normalized error type (document how normalization handles variable IDs and noisy messages). Choose fixed or sliding window explicitly and implement time-boundary tests. Seed a demo rule: 20 matching events within 60 seconds. When threshold crosses, create one active incident using a race-safe approach (unique partial index or documented transactional/locking strategy); subsequent matching events update its counts/evidence timeline without opening duplicates. Define incident state transitions and guarded status update API with resolution notes and audit entries. Provide JUnit tests for 19 vs 20 events, window boundaries, different services, out-of-order events, duplicate event IDs, concurrent creates, and unauthorized state mutation. Include an API/manual reproducible demonstration.
Acceptance checklist
Threshold and grouping match written specification.
Concurrent or duplicate submissions do not duplicate incidents.
Every transition is role-checked and recorded in timeline.
Part 7 — React incident dashboard and triage workflow
Objective: Make the Java MVP feel like a real developer product.SRS mapping: FR-17–23; UI requirements.Exit evidence: Login -> service -> events -> incident -> investigate -> resolve works.
Copy-ready prompt
Implement Part 7. Build a polished responsive React TypeScript dashboard with sidebar, organization switcher, service health cards, active incident counters, recent events chart, incident table with filters (severity, status, project/service, time range), and incident details containing event evidence, timeline, and status transitions. Display all metrics from real backend endpoints, never hardcoded demo counters in normal mode. Add typed React Query hooks, auth-protected routes, pagination, loading/empty/error states, keyboard accessibility, and request correlation IDs in API errors. Design meaningful reusable components rather than a monolithic page. Add UI tests and an end-to-end scenario using the demo application's injected failures. Do not add AI or Go integration in this part.
Acceptance checklist
Dashboard displays actual incident counts.
Engineer resolves an incident and sees the timeline update.
Viewer cannot see edit controls and remains blocked server-side.
Part 8 — Go collector, Redis Streams, and reliable Java consumer
Objective: Upgrade the functional MVP to a proper event-ingestion pipeline.SRS mapping: FR-15/16, NFR-07/10/16; AC-02/03/10.Exit evidence: App -> Go -> Redis -> Java -> PostgreSQL -> dashboard.
Copy-ready prompt
Implement Part 8 without rewriting working incident/business logic. Go net/http exposes POST /v1/ingest/events, uses service-scoped key validation (design a secure cached verification or internal service lookup approach; do not share raw key DB data unnecessarily), validates JSON schema/size, applies per-key limits, redacts sensitive values, and publishes normalized events to Redis Stream telemetry.events.v1. 202 is returned only after confirmed stream publish; return 429 or 503 with safe error messages for backpressure/unavailability. Java implements a Redis Streams consumer group that calls existing EventProcessor, commits database work before XACK, has bounded retries/pending-entry recovery/dead-letter handling, and stays idempotent across crash/replay. Add Docker configuration, structured logs, graceful shutdown, Go concurrency/unit tests, Java integration tests, and a load generator for synthetic events. Disable the Java dev ingestion endpoint outside dev. Demonstrate replay and Redis outage outcomes and document durability limits.
Acceptance checklist
End-to-end Go/Redis/Java pipeline works.
Duplicate/replayed events are safe; no duplicate incident or notification.
Redis outage produces honest backpressure response, not silent data loss.
Part 9 — Real-time updates, notifications, and collaboration
Objective: Inform the right developer about incident changes.SRS mapping: FR-20–22; UC-05.Exit evidence: New incidents appear live and alerts aren't duplicated.
Copy-ready prompt
Implement Part 9. Add incident assignment with same-organization member validation. Implement an authenticated SSE endpoint (/api/v1/organizations/{orgId}/stream) or justify WebSockets if required; ensure tenant-scope filtering, reconnection strategy, heartbeat, event IDs, and cleanup. On new/updated incident, publish in-app notifications to relevant members. Use outbox/dedupe keys so stream retries do not send duplicates. Build notification menu, unread count, incident link, and an assignment control in React. Optional email integration must be configurable and have a local fake provider; no real email is sent from tests. Add end-to-end tests for alert creation, reconnect, access denial, and duplicate suppression. Do not make the AI provider necessary for alerting.
Acceptance checklist
Dashboard updates automatically without refresh.
A viewer cannot subscribe to another organization’s event stream.
Event replay does not send the same notification twice.
Part 10 — AI incident analysis with Spring AI and external API
Objective: Deliver the project's AI value without building or training an AI model.SRS mapping: FR-24–28; AI section 9; AC-08/09/12.Exit evidence: A sanitized, evidence-linked, structured analysis is generated for an incident.
Copy-ready prompt
Implement Part 10. Use Spring AI in the Java backend with an external supported OpenAI/Gemini model configured through environment variables. Provide a mock/fake AI client for tests and offline local development. Engineer/admin users may request analysis on an incident; Java collects a bounded evidence window of event IDs, sanitized error messages, service metadata and timeline. Redact secrets/PII, enforce tenant filtering, limit evidence size and AI call frequency, and treat raw logs as untrusted data (prevent prompt-injection instructions from being obeyed). Require strongly typed validated JSON: summary, possibleCauses with evidenceIds, suggestedChecks, missingEvidence, limitations. Check returned evidenceIds against supplied IDs. Persist analysis status/result/model/time; gracefully handle timeouts, quotas, invalid JSON, and unavailable API without affecting incident data. Frontend shows loading/failure/history and explicitly labels output as advisory. Include a real-provider config guide, safe mock mode, unit/integration tests and cost guardrails. No automatic restart/rollback/shell tools.
Acceptance checklist
Mock analysis passes tests without any real API key.
Secret-bearing logs are redacted before outbound request.
AI output links to real event IDs and describes uncertainty.
Part 11 — GitHub deployments and reviewed postmortems
Objective: Connect operational changes with incidents and document learning.SRS mapping: FR-29–32, UC-06/07.Exit evidence: Verified deployment timeline and editable postmortem.
Copy-ready prompt
Implement Part 11. Receive GitHub deployment events via a dedicated webhook with strict HMAC signature verification over the raw body, allowed event types, idempotent delivery ID handling, and service mapping. Store commit SHA, environment, timestamp, source and status. Show deployments near an incident on its timeline; label them temporally correlated rather than proven causes. Add postmortem editing with impact summary, timeline, hypothesis vs verified root cause, contributing factors, remediation tasks, and follow-up owners. Generate an AI first draft only when the incident is resolved, reusing the sanitized evidence pipeline. Require engineer approval before finalizing; revisions and approvals should be audited. Tests must include valid/invalid signatures, retries/duplicate delivery, malicious payload handling and cross-tenant protection.
Acceptance checklist
Forged webhooks are rejected.
Postmortem finalization requires human action.
Incident UI differentiates evidence, hypothesis and confirmed conclusion.
Part 12 — Hardening, documentation, release and measurable demo
Objective: Make the platform demonstrable, reproducible and professionally reviewable.SRS mapping: Sections 10–14; all P0/P1 acceptance criteria.Exit evidence: One reproducible local run plus evidence-based test/performance report.
Copy-ready prompt
Implement Part 12. Audit implemented functionality against every P0/P1 SRS ID and report missing items honestly. Provide complete Docker Compose to run React, Spring Boot, Go, PostgreSQL and Redis with health checks, reasonable local volumes, secrets through environment/config (not Git), and safe start order. Add Java integration tests/Testcontainers, Go race tests and HTTP tests, React component/e2e tests, OWASP-informed tenant access review, key/PII leakage checks, liveness/readiness endpoints, structured logs, lint/format/CI using GitHub Actions. Provide a seedable demo application and a deterministic demo script: register service -> induce 20 errors -> watch incident -> analyze (mock or real API) -> resolve -> export reviewed postmortem. Run/describe k6 or equivalent synthetic load tests; report measured throughput, p95 latency, detection delay and test environment only if actually executed. Supply README.md, architecture diagrams, API documentation, troubleshooting, test matrix, screenshots checklist, and resume-ready bullet templates with placeholders for verified measurements.
Acceptance checklist
A clean checkout can run from documented commands.
Security/tenant tests, integration tests, and complete demo pass on the evaluator's machine.
All claimed measurements and screenshots have reproducible evidence.
Part 13 (optional) — OpenTelemetry, Prometheus and Loki
Objective: Demonstrate deeper observability without losing project focus.SRS mapping: FR-34, P2.Exit evidence: Live metrics, logs, and trace IDs linked to incidents.
Copy-ready prompt
Extend DevPulse AI with Part 13, but do not replace existing working ingestion/incident workflows. Instrument Java and Go services using OpenTelemetry where supported; propagate trace/correlation IDs. Expose Micrometer/Prometheus metrics for application and ingestion health. Integrate Grafana Loki for higher-volume log searching through a secure, tenant-aware query abstraction and define retention policies. Build a per-incident evidence panel that connects events to nearby metrics and correlated traces. Create reproducible dashboards and synthetic tests; document limits and avoid claims of full distributed tracing until verified. Add instrumentation overhead measurements and verify that external evidence sources cannot bypass tenant boundaries.
Recovery prompt — when a build or test fails
We are at DevPulse AI Part [NUMBER]. Do not implement a new part. Here is the exact command, the full relevant error output, and the current affected files: [PASTE]. Diagnose the first root cause and propose the smallest safe patch. Distinguish compile errors, misconfiguration, database migration failures and runtime errors. Preserve the existing architecture and API contracts. Give precise file changes, updated commands for Windows PowerShell, and regression tests. Do not claim the issue is fixed until I run and confirm the commands or tool output proves it.
Review prompt — before moving to the next part
Review DevPulse AI Part [NUMBER] against the SRS and the part's acceptance checklist. Read the changed files/tests and identify missing requirements, authorization gaps, unsafe defaults, data leaks, inconsistent DTOs, unhandled failures and missing documentation. Give a pass/fail table with evidence, prioritized fixes, and a recommended Git commit message. Do not start the next part until blocking issues are addressed.
Handoff prompt — when starting a fresh coding chat
We are continuing DevPulse AI, a React + Java Spring Boot + Go + PostgreSQL + Redis Streams + external AI API platform. The attached SRS v1.0 is the product contract. Completed parts: [LIST]. Working modules and API contracts: [PASTE CURRENT REPO TREE/OPENAPI]. Current branch/commit: [HASH]. Known failures: [LIST]. Next part: [NUMBER]. First summarize confirmed existing behavior and identify exactly which files need changes for the next part. Then implement only that part, following the Master Context rules and its acceptance checklist. Do not recreate existing code or assume previous outputs you cannot see.
Final completeness checklist
[ ] Java backend is primary system of record; Go has a justified ingestion responsibility.
[ ] Tenant isolation is enforced and tested everywhere.
[ ] Redis event processing is idempotent with retries and acknowledgment.
[ ] Alerts and incident states follow documented deterministic rules.
[ ] React shows real data with clear loading and error states.
[ ] AI API key is server-side, provider failures are handled, and output is advisory.
[ ] Logs are redacted before sending to the AI provider.
[ ] Docker, tests, developer onboarding and demo run reproducibly.
[ ] Performance achievements on the resume are measured, not invented.
