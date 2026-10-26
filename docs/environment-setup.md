# DevPulse AI — Windows 11 & Local Environment Setup Guide

- **Document Version:** 1.0.0
- **Companion:** [ADR-001](file:///s:/Personal%20Project/DevPulse/docs/adr/ADR-001-architecture-foundation.md), [System Contracts](file:///s:/Personal%20Project/DevPulse/docs/spec/system-contracts.md)
- **Scope:** Host verification, toolchain versions, Windows PowerShell commands, and diagnostic troubleshooting.

---

## 1. Local Environment Diagnostics (Executed Baseline)

The following verification commands were executed on the developer workstation (Windows 11):

```powershell
java -version
javac -version
mvn -v
go version
node -v
npm -v
docker --version
docker compose version
git --version
```

### 1.1 Verified Toolchain Status

| Component | Detected Version | Target Baseline | Status | Action Required |
| :--- | :--- | :--- | :--- | :--- |
| **Java Runtime** | `25.0.1+8-LTS-27` | Java 21 LTS | **Verified** | Works with Maven `--release 21` compiler flag. |
| **Java Compiler** | `javac 25.0.1` | Java 21 LTS | **Verified** | Standard compiler available. |
| **Maven** | `Apache Maven 3.9.11` | Maven 3.9+ | **Verified** | Full build support present in PATH. |
| **Node.js** | `v24.3.0` | Node 20+ LTS | **Verified** | Fully compatible with Vite 6 & React 19. |
| **npm** | `11.4.2` | npm 10+ | **Verified** | Package manager ready. |
| **Docker CLI** | `29.6.1` | Docker 24+ | **Verified** | CLI is installed. |
| **Docker Compose** | `v5.3.0` | Compose v2.20+ | **Verified** | Compose plugin is installed. |
| **Docker Daemon** | `Offline` (Engine not running) | Daemon Active | **Attention** | Launch **Docker Desktop** before running `docker compose up`. |
| **Git** | `2.50.1.windows.1` | Git 2.40+ | **Verified** | Initialized repository at workspace root. |
| **Go** | *Not found in PATH* | Go 1.23+ | **Attention** | Install Go via `winget` (see section 2.1). Required for Part 8. |

---

## 2. Windows 11 Prerequisites & Setup Commands

### 2.1 Installing Go (Required for Part 8 Ingestion Gateway)
If `go` is not recognized, run the following in an elevated PowerShell terminal:

```powershell
# Install Go via Windows Package Manager
winget install --id GoLang.Go -e

# After installation, refresh environment variables or reopen terminal:
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Verify Go installation:
go version
```

### 2.2 Starting Docker Desktop
Docker Compose requires the Docker Desktop daemon to be running. If you encounter:
`failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`

Start Docker Desktop via PowerShell:
```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"

# Wait a few seconds for the engine to initialize, then verify:
docker ps
```

### 2.3 Java Compilation Target (Java 21 Release Flag)
Although the host has JDK 25 installed, our project strictly targets **Java 21 LTS** to guarantee 100% compatibility with Spring Boot 3.4.1, Spring AI, and containerized runtime images (`eclipse-temurin:21-jre-alpine`).

In `backend-java/pom.xml`:
```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.release>21</maven.compiler.release>
</properties>
```
This instructs `javac` (version 25) to compile against Java 21 language specification and standard library APIs, preventing accidental usage of JDK 22-25 preview APIs.

---

## 3. End-to-End Verification Commands

To check the complete local environment at any time, execute this single diagnostic script:

```powershell
Write-Host "=== DevPulse AI Environment Verification ===" -ForegroundColor Cyan
Write-Host -NoNewline "Git: " ; git --version
Write-Host -NoNewline "Java: " ; java -version 2>&1 | Select-Object -First 1
Write-Host -NoNewline "Maven: " ; mvn -v 2>&1 | Select-Object -First 1
Write-Host -NoNewline "Node: " ; node -v
Write-Host -NoNewline "NPM: " ; npm -v
Write-Host -NoNewline "Docker CLI: " ; docker --version
Write-Host -NoNewline "Docker Compose: " ; docker compose version
try {
    $dockerRunning = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker Engine: Running" -ForegroundColor Green
    } else {
        Write-Host "Docker Engine: Not running (Start Docker Desktop)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Docker Engine: Error checking daemon" -ForegroundColor Red
}

try {
    $goVer = go version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Go: $goVer" -ForegroundColor Green
    } else {
        Write-Host "Go: Not found (Run 'winget install GoLang.Go')" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Go: Not found (Run 'winget install GoLang.Go')" -ForegroundColor Yellow
}
```

---

## 4. Part 0 Acceptance Checklist

- [x] Dependencies and version compatibility documented with official references ([ADR-001](file:///s:/Personal%20Project/DevPulse/docs/adr/ADR-001-architecture-foundation.md)).
- [x] Mermaid architecture, ingestion, AI advisory, and ERD diagrams consistent with SRS ([ADR-001](file:///s:/Personal%20Project/DevPulse/docs/adr/ADR-001-architecture-foundation.md), [System Contracts](file:///s:/Personal%20Project/DevPulse/docs/spec/system-contracts.md)).
- [x] Developer toolchain verified locally; actionable steps provided for Docker daemon and Go.
- [x] Service boundaries, REST error contracts (RFC 7807), event schema, and UTC ISO-8601 timestamps agreed upon.
- [x] Event fingerprinting algorithm, "20-in-60s" sliding window rule, and race-safe incident state transitions defined.
- [x] P0, P1, and P2 priority taxonomy defined with clear scope boundaries.
