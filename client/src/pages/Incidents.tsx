import { Link } from 'react-router-dom'
import { fetchIncidents } from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import { SeverityBadge } from '../components/SeverityBadge'

function formatDuration(startedAt: string, resolvedAt: string | null) {
  const start = new Date(startedAt).getTime()
  const end = resolvedAt ? new Date(resolvedAt).getTime() : Date.now()
  const minutes = Math.max(0, Math.round((end - start) / 60000))
  if (minutes < 60) return `${minutes}m`
  return `${Math.floor(minutes / 60)}h ${minutes % 60}m`
}

export function Incidents() {
  const { data: incidents, loading } = usePolling(fetchIncidents, 8000)

  return (
    <div>
      <div className="page-header">
        <h1>Incident Timeline</h1>
        <p className="page-sub">Every incident the alerting engine has opened, newest first.</p>
      </div>

      {loading && !incidents && <div className="empty-state">Loading incident history…</div>}
      {incidents && incidents.length === 0 && (
        <div className="empty-state">No incidents yet — the simulator injects one periodically.</div>
      )}

      <div className="incident-list">
        {incidents?.map((incident) => (
          <Link to={`/incidents/${incident.id}`} key={incident.id} className="incident-row">
            <SeverityBadge severity={incident.severity} />
            <div className="incident-main">
              <div className="incident-title">{incident.title}</div>
              <div className="incident-meta">
                {incident.serviceName} · {new Date(incident.startedAt).toLocaleString()} ·{' '}
                {formatDuration(incident.startedAt, incident.resolvedAt)}
              </div>
            </div>
            <span className={`incident-status incident-status-${incident.status.toLowerCase()}`}>
              {incident.status}
            </span>
          </Link>
        ))}
      </div>
    </div>
  )
}
