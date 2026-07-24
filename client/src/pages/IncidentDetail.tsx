import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchIncident, fetchLogs, fetchServiceMetrics } from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import { SeverityBadge } from '../components/SeverityBadge'
import { MetricChart } from '../components/MetricChart'
import type { LogEntry } from '../types'

export function IncidentDetail() {
  const { id } = useParams()
  const incidentId = Number(id)
  const { data: incident } = usePolling(() => fetchIncident(incidentId), 8000, [incidentId])

  const [logs, setLogs] = useState<LogEntry[]>([])
  const [logsLoading, setLogsLoading] = useState(true)
  const [errorRate, setErrorRate] = useState<Awaited<ReturnType<typeof fetchServiceMetrics>>>([])
  const [latency, setLatency] = useState<Awaited<ReturnType<typeof fetchServiceMetrics>>>([])

  useEffect(() => {
    if (!incident) return
    setLogsLoading(true)
    fetchLogs({ serviceId: incident.serviceId, window: '24h', limit: 300 })
      .then((all) => {
        const start = new Date(incident.startedAt).getTime() - 60_000
        const end = incident.resolvedAt ? new Date(incident.resolvedAt).getTime() + 60_000 : Date.now()
        setLogs(all.filter((l) => {
          const t = new Date(l.timestamp).getTime()
          return t >= start && t <= end
        }))
      })
      .finally(() => setLogsLoading(false))

    fetchServiceMetrics(incident.serviceId, 'ERROR_RATE', '24h').then(setErrorRate)
    fetchServiceMetrics(incident.serviceId, 'LATENCY_P95', '24h').then(setLatency)
  }, [incident?.id, incident?.startedAt, incident?.resolvedAt, incident?.serviceId])

  if (!incident) {
    return <div className="empty-state">Loading incident…</div>
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <Link to="/incidents" className="back-link">
            ← Incident timeline
          </Link>
          <h1>{incident.title}</h1>
          <p className="page-sub">
            <SeverityBadge severity={incident.severity} /> ·{' '}
            <Link to={`/services/${incident.serviceId}`}>{incident.serviceName}</Link> ·{' '}
            {new Date(incident.startedAt).toLocaleString()}
            {incident.resolvedAt ? ` → ${new Date(incident.resolvedAt).toLocaleString()}` : ' (ongoing)'}
          </p>
        </div>
      </div>

      <div className="chart-grid">
        <div className="chart-card">
          <h4>Error rate around this incident</h4>
          <MetricChart points={errorRate} color="#f87171" unit="%" />
        </div>
        <div className="chart-card">
          <h4>p95 latency around this incident</h4>
          <MetricChart points={latency} color="#facc15" unit="ms" />
        </div>
      </div>

      <h3 className="section-title">Alerts in this incident</h3>
      <div className="incident-alert-list">
        {incident.alerts.map((alert) => (
          <div key={alert.id} className="incident-alert-row">
            <SeverityBadge severity={alert.severity} />
            <span>{alert.ruleName}</span>
            <span className="incident-alert-meta">
              value {alert.triggeringValue.toFixed(2)} · {new Date(alert.startedAt).toLocaleTimeString()}
              {alert.resolvedAt ? ` → ${new Date(alert.resolvedAt).toLocaleTimeString()}` : ' (firing)'}
            </span>
          </div>
        ))}
      </div>

      <h3 className="section-title">Correlated logs</h3>
      <p className="page-sub">Log lines from {incident.serviceName} during this incident window.</p>
      {logsLoading && <div className="empty-state">Loading logs…</div>}
      <div className="log-list">
        {logs.map((log) => (
          <div key={log.id} className={`log-row log-level-${log.level.toLowerCase()}`}>
            <span className="log-time">{new Date(log.timestamp).toLocaleTimeString()}</span>
            <span className="log-level">{log.level}</span>
            <span className="log-message">{log.message}</span>
          </div>
        ))}
        {!logsLoading && logs.length === 0 && <div className="empty-state">No log lines in this window.</div>}
      </div>
    </div>
  )
}
