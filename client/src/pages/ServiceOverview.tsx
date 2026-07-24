import { Link } from 'react-router-dom'
import { fetchServices } from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import { StatusBadge } from '../components/StatusBadge'
import { Sparkline } from '../components/Sparkline'

export function ServiceOverview() {
  const { data: services, loading } = usePolling(fetchServices, 5000)

  return (
    <div>
      <div className="page-header">
        <h1>Service Health</h1>
        <p className="page-sub">Live view of the fleet — updates every 5 seconds.</p>
      </div>

      {loading && !services && <div className="empty-state">Loading fleet status…</div>}

      <div className="service-grid">
        {services?.map((svc) => (
          <Link to={`/services/${svc.id}`} key={svc.id} className="service-card">
            <div className="service-card-head">
              <h3>{svc.name}</h3>
              <StatusBadge status={svc.status} />
            </div>
            <p className="service-desc">{svc.description}</p>
            <Sparkline
              values={svc.requestRateSparkline}
              color={svc.status === 'DOWN' ? '#f87171' : svc.status === 'DEGRADED' ? '#facc15' : '#38bdf8'}
            />
            <div className="service-stats">
              <div>
                <span className="stat-label">Req/s</span>
                <span className="stat-value">{svc.requestRatePerSec.toFixed(1)}</span>
              </div>
              <div>
                <span className="stat-label">p95</span>
                <span className="stat-value">{svc.p95LatencyMs.toFixed(0)}ms</span>
              </div>
              <div>
                <span className="stat-label">Errors</span>
                <span className="stat-value">{svc.errorRatePercent.toFixed(2)}%</span>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
