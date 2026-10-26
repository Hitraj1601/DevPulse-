# ==============================================================================
# DevPulse AI — Local Environment Smoke Test Script
# Verifies Docker containers, Java API health, Go health, and Frontend setup
# ==============================================================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "         DevPulse AI Smoke Test (Part 1)             " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan

$allPassed = $true

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$ExpectedStatus = "UP"
    )
    Write-Host -NoNewline "Checking $Name ($Url)... "
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 3 -ErrorAction Stop
        if ($response.status -eq $ExpectedStatus) {
            Write-Host "[PASS] Status: $($response.status)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "[FAIL] Unexpected payload: $($response | ConvertTo-Json -Compress)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "[OFFLINE] $($_.Exception.Message)" -ForegroundColor Yellow
        return $false
    }
}

# 1. Check Java Spring Boot Health
$javaPassed = Test-Endpoint -Name "Java Actuator Health" -Url "http://localhost:8080/actuator/health"
$javaApiPassed = Test-Endpoint -Name "Java /api/v1/health" -Url "http://localhost:8080/api/v1/health"

# 2. Check Go Ingestion Health
$goPassed = Test-Endpoint -Name "Go Ingestion Health" -Url "http://localhost:8081/health"

# 3. Check Docker Containers (Postgres & Redis)
Write-Host -NoNewline "Checking Docker Containers (PostgreSQL & Redis)... "
try {
    $psOutput = docker compose -f infrastructure/docker-compose.yml ps --format json 2>$null
    if ($LASTEXITCODE -eq 0 -and $psOutput) {
        Write-Host "[RUNNING] Docker Compose active" -ForegroundColor Green
    } else {
        Write-Host "[OFFLINE] Containers not running (Run 'docker compose -f infrastructure/docker-compose.yml up -d')" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Could not query Docker CLI" -ForegroundColor Yellow
}

Write-Host "`nSmoke test evaluation completed." -ForegroundColor Cyan
