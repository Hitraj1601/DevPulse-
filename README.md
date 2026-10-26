# DevPulse AI — Operational Health & Incident Intelligence Platform

> **DevPulse AI** is an AI-assisted application monitoring and incident management platform built with Java Spring Boot, Go, React, PostgreSQL, Redis Streams, and external LLM advisory analysis.

---

## 1. Monorepo Structure

```
devpulse-ai/
├── .editorconfig                          # Global code style formatting
├── .gitignore                             # Ignored build artefacts, secrets, dependencies
├── .env.example                           # Configuration environment template
├── README.md                              # Root documentation and runbook
├── DevPulse_AI_Build_Prompts.md           # Master prompts and milestone roadmap
├── docs/
│   ├── DevPulse_AI_SRS_v1.0.md            # Official SRS v1.0 specification
│   ├── adr/
│   │   └── ADR-001-architecture-foundation.md  # Architectural decision record
│   ├── spec/
│   │   └── system-contracts.md            # API schemas, error models, and ERD
│   └── environment-setup.md               # Toolchain verification runbook
├── infrastructure/
│   ├── docker-compose.yml                 # PostgreSQL 16 + Redis 7.4 + health checks
│   └── postgres/
│       └── init-extensions.sql            # pgcrypto and uuid-ossp extension init
├── backend-java/                          # Core System of Record API (Spring Boot 3.4.1)
│   ├── pom.xml
│   └── src/
├── ingestion-go/                          # High-throughput Telemetry Collector (Go 1.23+)
│   ├── go.mod
│   └── cmd/server/main.go
├── frontend/                              # Real-time Web Dashboard (React 18/19 + Vite 6)
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
├── demo-application/                      # Synthetic burst and error generator (Part 5)
│   └── README.md
└── scripts/
    └── smoke-test.ps1                     # Automated local verification script
```

---

## 2. Prerequisites & Verified Toolchain

- **Operating System:** Windows 11 (PowerShell 5.1 / 7+)
- **Java:** JDK 21+ (Java 21 LTS bytecode target configured via Maven `<release>21</release>`)
- **Maven:** Apache Maven 3.9+
- **Node.js:** Node.js 20+ (Node v24.3.0 and npm 11.4.2 verified)
- **Go:** Go 1.23+ (`C:\Program Files\Go\bin\go.exe`)
- **Docker:** Docker Desktop with Compose v2.20+ (running daemon required for Postgres and Redis)

---

## 3. Quick Start Runbook (Windows PowerShell)

### Step 1: Start Infrastructure (PostgreSQL & Redis)
Ensure Docker Desktop is running, then execute:

```powershell
docker compose -f infrastructure/docker-compose.yml up -d
```
*Health checks will automatically verify PostgreSQL on `5432` and Redis on `6379`.*

### Step 2: Start Java Main Backend API
In a new PowerShell terminal:

```powershell
cd backend-java
mvn spring-boot:run
```
*Java API starts on `http://localhost:8080`. Health endpoint: `http://localhost:8080/actuator/health`.*

### Step 3: Start Go Ingestion Collector
In a second PowerShell terminal:

```powershell
cd ingestion-go
go run ./cmd/server/main.go
```
*Go service listens on `http://localhost:8081`. Health endpoint: `http://localhost:8081/health`.*

### Step 4: Start Frontend Development Server
In a third PowerShell terminal:

```powershell
cd frontend
npm install
npm run dev
```
*Frontend opens at `http://localhost:5173`. Proxies `/actuator` and `/api` to Java, and `/go-health` to Go.*

---

## 4. Stopping Services

```powershell
# Stop Docker containers and preserve persistent local data:
docker compose -f infrastructure/docker-compose.yml down

# Or stop and completely wipe persistent local volumes (Development reset):
docker compose -f infrastructure/docker-compose.yml down -v
```

---

## 5. Automated Smoke Test Script

Run the automated smoke test script to verify all services in one step:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```
