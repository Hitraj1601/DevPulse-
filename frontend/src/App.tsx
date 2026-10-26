import { useState, useEffect, useCallback } from 'react'
import { 
  Activity, 
  Server, 
  Database, 
  Zap, 
  RefreshCw, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  Layers, 
  Terminal, 
  ExternalLink,
  ShieldCheck
} from 'lucide-react'

interface ServiceHealth {
  status: 'UP' | 'DOWN' | 'CHECKING'
  latencyMs?: number
  lastChecked?: string
  data?: Record<string, unknown>
  error?: string
}

export default function App() {
  const [javaHealth, setJavaHealth] = useState<ServiceHealth>({ status: 'CHECKING' })
  const [goHealth, setGoHealth] = useState<ServiceHealth>({ status: 'CHECKING' })
  const [autoRefresh, setAutoRefresh] = useState(true)
  const [activeTab, setActiveTab] = useState<'overview' | 'payloads' | 'architecture'>('overview')

  const checkHealth = useCallback(async () => {
    // 1. Check Java Spring Boot Health via Vite Proxy (/actuator/health)
    const javaStart = performance.now()
    try {
      const res = await fetch('/actuator/health', { method: 'GET', headers: { 'Accept': 'application/json' } })
      const latency = Math.round(performance.now() - javaStart)
      if (res.ok) {
        const data = await res.json()
        setJavaHealth({
          status: data.status === 'UP' ? 'UP' : 'DOWN',
          latencyMs: latency,
          lastChecked: new Date().toLocaleTimeString(),
          data
        })
      } else {
        setJavaHealth({
          status: 'DOWN',
          latencyMs: latency,
          lastChecked: new Date().toLocaleTimeString(),
          error: `HTTP ${res.status}: ${res.statusText}`
        })
      }
    } catch (err: unknown) {
      setJavaHealth({
        status: 'DOWN',
        latencyMs: Math.round(performance.now() - javaStart),
        lastChecked: new Date().toLocaleTimeString(),
        error: err instanceof Error ? err.message : 'Connection refused'
      })
    }

    // 2. Check Go Ingestion Gateway Health via Vite Proxy (/go-health -> /health)
    const goStart = performance.now()
    try {
      const res = await fetch('/go-health', { method: 'GET', headers: { 'Accept': 'application/json' } })
      const latency = Math.round(performance.now() - goStart)
      if (res.ok) {
        const data = await res.json()
        setGoHealth({
          status: data.status === 'UP' ? 'UP' : 'DOWN',
          latencyMs: latency,
          lastChecked: new Date().toLocaleTimeString(),
          data
        })
      } else {
        setGoHealth({
          status: 'DOWN',
          latencyMs: latency,
          lastChecked: new Date().toLocaleTimeString(),
          error: `HTTP ${res.status}: ${res.statusText}`
        })
      }
    } catch (err: unknown) {
      setGoHealth({
        status: 'DOWN',
        latencyMs: Math.round(performance.now() - goStart),
        lastChecked: new Date().toLocaleTimeString(),
        error: err instanceof Error ? err.message : 'Connection refused (Go service offline)'
      })
    }
  }, [])

  useEffect(() => {
    checkHealth()
    if (!autoRefresh) return
    const interval = setInterval(checkHealth, 5000)
    return () => clearInterval(interval)
  }, [checkHealth, autoRefresh])

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2.5rem 1.5rem' }}>
      {/* Header */}
      <header style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        flexWrap: 'wrap', 
        gap: '1rem',
        marginBottom: '2.5rem',
        paddingBottom: '1.5rem',
        borderBottom: '1px solid var(--border-color)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.875rem' }}>
          <div style={{ 
            background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-indigo))', 
            borderRadius: 'var(--radius-md)', 
            padding: '0.625rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)'
          }}>
            <Activity size={26} color="#0a0d14" strokeWidth={2.5} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
              <h1 style={{ fontSize: '1.5rem', fontWeight: 800, letterSpacing: '-0.02em' }}>DevPulse AI</h1>
              <span style={{ 
                fontSize: '0.75rem', 
                background: 'rgba(56, 189, 248, 0.15)', 
                color: 'var(--accent-cyan)', 
                padding: '0.15rem 0.5rem', 
                borderRadius: '999px',
                fontWeight: 600,
                border: '1px solid rgba(56, 189, 248, 0.3)'
              }}>
                PART 1 — SKELETON
              </span>
            </div>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
              Operational Health & Incident Intelligence Platform
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: autoRefresh ? 'rgba(56, 189, 248, 0.1)' : 'rgba(255, 255, 255, 0.05)',
              border: `1px solid ${autoRefresh ? 'var(--border-color-glow)' : 'var(--border-color)'}`,
              color: autoRefresh ? 'var(--accent-cyan)' : 'var(--text-secondary)',
              padding: '0.5rem 0.875rem',
              borderRadius: 'var(--radius-md)',
              cursor: 'pointer',
              fontSize: '0.8125rem',
              fontWeight: 500,
              transition: 'all 0.2s ease'
            }}
          >
            <Clock size={15} />
            {autoRefresh ? 'Polling: 5s' : 'Polling: Paused'}
          </button>

          <button
            onClick={checkHealth}
            id="refresh-health-button"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'var(--accent-blue)',
              border: 'none',
              color: '#ffffff',
              padding: '0.5rem 1rem',
              borderRadius: 'var(--radius-md)',
              cursor: 'pointer',
              fontSize: '0.8125rem',
              fontWeight: 600,
              boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
              transition: 'all 0.2s ease'
            }}
          >
            <RefreshCw size={15} className={javaHealth.status === 'CHECKING' ? 'animate-spin' : ''} />
            Refresh All
          </button>
        </div>
      </header>

      {/* Navigation Tabs */}
      <nav style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem' }}>
        {(['overview', 'payloads', 'architecture'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              background: activeTab === tab ? 'var(--bg-card-hover)' : 'transparent',
              border: `1px solid ${activeTab === tab ? 'var(--border-color-glow)' : 'transparent'}`,
              color: activeTab === tab ? 'var(--text-primary)' : 'var(--text-secondary)',
              padding: '0.5rem 1rem',
              borderRadius: 'var(--radius-md)',
              cursor: 'pointer',
              fontSize: '0.875rem',
              fontWeight: activeTab === tab ? 600 : 400,
              textTransform: 'capitalize',
              transition: 'all 0.15s ease'
            }}
          >
            {tab}
          </button>
        ))}
      </nav>

      {/* Tab: Overview */}
      {activeTab === 'overview' && (
        <main>
          {/* Service Health Cards Grid */}
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', 
            gap: '1.5rem',
            marginBottom: '2.5rem'
          }}>
            {/* Card 1: Java Spring Boot */}
            <div style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-lg)',
              padding: '1.5rem',
              backdropFilter: 'blur(12px)',
              boxShadow: 'var(--shadow-card)',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ 
                    background: 'rgba(56, 189, 248, 0.1)', 
                    padding: '0.5rem', 
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid rgba(56, 189, 248, 0.2)'
                  }}>
                    <Server size={20} color="var(--accent-cyan)" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Java Main API</h2>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Spring Boot 3.4.1 (Java 21)</p>
                  </div>
                </div>

                {/* Status Badge */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.375rem',
                  padding: '0.3rem 0.625rem',
                  borderRadius: '999px',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  background: javaHealth.status === 'UP' ? 'var(--status-up-bg)' : javaHealth.status === 'DOWN' ? 'var(--status-down-bg)' : 'var(--status-checking-bg)',
                  color: javaHealth.status === 'UP' ? 'var(--status-up-text)' : javaHealth.status === 'DOWN' ? 'var(--status-down-text)' : 'var(--status-checking-text)',
                  border: `1px solid ${javaHealth.status === 'UP' ? 'var(--status-up-border)' : javaHealth.status === 'DOWN' ? 'var(--status-down-border)' : 'var(--status-checking-border)'}`
                }}>
                  {javaHealth.status === 'UP' && <CheckCircle2 size={13} />}
                  {javaHealth.status === 'DOWN' && <XCircle size={13} />}
                  {javaHealth.status === 'CHECKING' && <RefreshCw size={13} className="animate-spin" />}
                  {javaHealth.status}
                </div>
              </div>

              <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', display: 'grid', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Endpoint:</span>
                  <code style={{ color: 'var(--accent-cyan)' }}>/actuator/health</code>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Port:</span>
                  <span style={{ color: 'var(--text-primary)' }}>8080 (via Vite proxy)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Latency:</span>
                  <span style={{ color: javaHealth.latencyMs ? 'var(--status-up-text)' : 'var(--text-muted)' }}>
                    {javaHealth.latencyMs !== undefined ? `${javaHealth.latencyMs} ms` : '—'}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Last Ping:</span>
                  <span>{javaHealth.lastChecked || '—'}</span>
                </div>
                {javaHealth.error && (
                  <div style={{ 
                    marginTop: '0.5rem', 
                    padding: '0.5rem', 
                    borderRadius: 'var(--radius-sm)', 
                    background: 'rgba(239, 68, 68, 0.1)', 
                    color: 'var(--status-down-text)',
                    fontSize: '0.75rem'
                  }}>
                    {javaHealth.error}
                  </div>
                )}
              </div>
            </div>

            {/* Card 2: Go Ingestion Service */}
            <div style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-lg)',
              padding: '1.5rem',
              backdropFilter: 'blur(12px)',
              boxShadow: 'var(--shadow-card)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ 
                    background: 'rgba(99, 102, 241, 0.1)', 
                    padding: '0.5rem', 
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid rgba(99, 102, 241, 0.2)'
                  }}>
                    <Zap size={20} color="var(--accent-indigo)" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Go Ingestion Service</h2>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>net/http (Go 1.23+)</p>
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.375rem',
                  padding: '0.3rem 0.625rem',
                  borderRadius: '999px',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  background: goHealth.status === 'UP' ? 'var(--status-up-bg)' : goHealth.status === 'DOWN' ? 'var(--status-down-bg)' : 'var(--status-checking-bg)',
                  color: goHealth.status === 'UP' ? 'var(--status-up-text)' : goHealth.status === 'DOWN' ? 'var(--status-down-text)' : 'var(--status-checking-text)',
                  border: `1px solid ${goHealth.status === 'UP' ? 'var(--status-up-border)' : goHealth.status === 'DOWN' ? 'var(--status-down-border)' : 'var(--status-checking-border)'}`
                }}>
                  {goHealth.status === 'UP' && <CheckCircle2 size={13} />}
                  {goHealth.status === 'DOWN' && <XCircle size={13} />}
                  {goHealth.status === 'CHECKING' && <RefreshCw size={13} className="animate-spin" />}
                  {goHealth.status}
                </div>
              </div>

              <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', display: 'grid', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Endpoint:</span>
                  <code style={{ color: 'var(--accent-indigo)' }}>/health</code>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Port:</span>
                  <span style={{ color: 'var(--text-primary)' }}>8081 (proxy via /go-health)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Latency:</span>
                  <span style={{ color: goHealth.latencyMs ? 'var(--status-up-text)' : 'var(--text-muted)' }}>
                    {goHealth.latencyMs !== undefined ? `${goHealth.latencyMs} ms` : '—'}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Last Ping:</span>
                  <span>{goHealth.lastChecked || '—'}</span>
                </div>
                {goHealth.error && (
                  <div style={{ 
                    marginTop: '0.5rem', 
                    padding: '0.5rem', 
                    borderRadius: 'var(--radius-sm)', 
                    background: 'rgba(239, 68, 68, 0.1)', 
                    color: 'var(--status-down-text)',
                    fontSize: '0.75rem'
                  }}>
                    {goHealth.error}
                  </div>
                )}
              </div>
            </div>

            {/* Card 3: Storage & Infrastructure */}
            <div style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-lg)',
              padding: '1.5rem',
              backdropFilter: 'blur(12px)',
              boxShadow: 'var(--shadow-card)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ 
                    background: 'rgba(52, 211, 153, 0.1)', 
                    padding: '0.5rem', 
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid rgba(52, 211, 153, 0.2)'
                  }}>
                    <Database size={20} color="var(--status-up-text)" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Infrastructure</h2>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>PostgreSQL 16 & Redis 7.4</p>
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.375rem',
                  padding: '0.3rem 0.625rem',
                  borderRadius: '999px',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  background: 'rgba(56, 189, 248, 0.12)',
                  color: 'var(--accent-cyan)',
                  border: '1px solid rgba(56, 189, 248, 0.3)'
                }}>
                  <Layers size={13} />
                  DOCKER
                </div>
              </div>

              <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', display: 'grid', gap: '0.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>PostgreSQL:</span>
                  <span style={{ color: 'var(--text-primary)' }}>Port 5432 (devpulse_db)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Redis Streams:</span>
                  <span style={{ color: 'var(--text-primary)' }}>Port 6379 (telemetry.events.v1)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Health Checks:</span>
                  <span style={{ color: 'var(--status-up-text)' }}>Configured in compose.yml</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Local Volumes:</span>
                  <span style={{ color: 'var(--text-primary)' }}>devpulse_postgres_data</span>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Verification Guide */}
          <section style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-lg)',
            padding: '1.75rem',
            backdropFilter: 'blur(12px)'
          }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Terminal size={18} color="var(--accent-cyan)" />
              PowerShell Local Verification Runbook
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              To boot and verify all services locally on Windows 11:
            </p>
            <div style={{ 
              background: '#07090e', 
              padding: '1rem', 
              borderRadius: 'var(--radius-md)', 
              fontSize: '0.8125rem',
              color: '#38bdf8',
              border: '1px solid rgba(255, 255, 255, 0.05)',
              overflowX: 'auto',
              lineHeight: 1.7
            }}>
              <div><span style={{ color: '#64748b' }}># 1. Start Infrastructure (PostgreSQL & Redis)</span></div>
              <div>docker compose -f infrastructure/docker-compose.yml up -d</div>
              <div style={{ marginTop: '0.5rem' }}><span style={{ color: '#64748b' }}># 2. Run Java Main API (Terminal A)</span></div>
              <div>cd backend-java; mvn spring-boot:run</div>
              <div style={{ marginTop: '0.5rem' }}><span style={{ color: '#64748b' }}># 3. Run Go Ingestion Collector (Terminal B)</span></div>
              <div>cd ingestion-go; go run ./cmd/server/main.go</div>
              <div style={{ marginTop: '0.5rem' }}><span style={{ color: '#64748b' }}># 4. Run Frontend (Terminal C)</span></div>
              <div>cd frontend; npm run dev</div>
            </div>
          </section>
        </main>
      )}

      {/* Tab: Payloads */}
      {activeTab === 'payloads' && (
        <section style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          <div style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-lg)',
            padding: '1.5rem'
          }}>
            <h3 style={{ fontSize: '0.9375rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--accent-cyan)' }}>
              Java /actuator/health Response
            </h3>
            <pre style={{ 
              background: '#07090e', 
              padding: '1rem', 
              borderRadius: 'var(--radius-sm)', 
              fontSize: '0.75rem',
              overflowX: 'auto',
              color: '#e2e8f0'
            }}>
              {JSON.stringify(javaHealth.data || { error: javaHealth.error || 'No data yet' }, null, 2)}
            </pre>
          </div>

          <div style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-lg)',
            padding: '1.5rem'
          }}>
            <h3 style={{ fontSize: '0.9375rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--accent-indigo)' }}>
              Go /health Response
            </h3>
            <pre style={{ 
              background: '#07090e', 
              padding: '1rem', 
              borderRadius: 'var(--radius-sm)', 
              fontSize: '0.75rem',
              overflowX: 'auto',
              color: '#e2e8f0'
            }}>
              {JSON.stringify(goHealth.data || { error: goHealth.error || 'No data yet' }, null, 2)}
            </pre>
          </div>
        </section>
      )}

      {/* Tab: Architecture */}
      {activeTab === 'architecture' && (
        <section style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-lg)',
          padding: '1.75rem'
        }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldCheck size={20} color="var(--accent-cyan)" />
            DevPulse AI Monorepo Architecture
          </h3>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
            Multi-tiered, resilient observability pipeline with strict multi-tenant boundaries and advisory AI.
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
            <div style={{ padding: '1rem', borderRadius: 'var(--radius-md)', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--border-color)' }}>
              <h4 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--accent-cyan)' }}>frontend/</h4>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                React 18 + TypeScript + Vite. SPA with TanStack Query and SSE real-time listener.
              </p>
            </div>
            <div style={{ padding: '1rem', borderRadius: 'var(--radius-md)', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--border-color)' }}>
              <h4 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--accent-blue)' }}>backend-java/</h4>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Spring Boot 3.4.1. Core system of record, Tenant RBAC, EventProcessor, Rule Engine, and Spring AI.
              </p>
            </div>
            <div style={{ padding: '1rem', borderRadius: 'var(--radius-md)', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--border-color)' }}>
              <h4 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--accent-indigo)' }}>ingestion-go/</h4>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Go 1.23+ net/http. High-concurrency ingestion gateway validating keys and streaming to Redis.
              </p>
            </div>
            <div style={{ padding: '1rem', borderRadius: 'var(--radius-md)', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid var(--border-color)' }}>
              <h4 style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--status-up-text)' }}>infrastructure/</h4>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Docker Compose for PostgreSQL 16 (persistent relational data) and Redis 7.4 (event stream buffer).
              </p>
            </div>
          </div>
        </section>
      )}

      {/* Footer */}
      <footer style={{ 
        marginTop: '3.5rem', 
        paddingTop: '1.5rem', 
        borderTop: '1px solid var(--border-color)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem',
        fontSize: '0.8125rem',
        color: 'var(--text-muted)'
      }}>
        <div>DevPulse AI — Phased Implementation (Built with Java 21, Spring Boot, Go, React & Redis)</div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <a href="/actuator/health" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-cyan)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            Actuator Health <ExternalLink size={12} />
          </a>
          <a href="/go-health" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-indigo)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            Go Health <ExternalLink size={12} />
          </a>
        </div>
      </footer>
    </div>
  )
}
