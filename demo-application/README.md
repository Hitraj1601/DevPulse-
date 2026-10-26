# DevPulse AI — Demo Application Placeholder

This directory is reserved for the synthetic fault-injection demo service, implemented in **Part 5**.

## Planned Scope (Part 5)
- Lightweight Spring Boot service simulating production workloads.
- Controlled error-burst endpoints (e.g. `/api/trigger-db-timeout`, `/api/trigger-burst?count=25`).
- DevPulse telemetry integration client configured with scoped ingestion keys.
- Deterministic verification target for the "20 events in 60 seconds" rule.
