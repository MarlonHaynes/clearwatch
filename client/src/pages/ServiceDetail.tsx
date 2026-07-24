import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchService, fetchServiceMetrics } from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import { StatusBadge } from '../components/StatusBadge'
import { MetricChart } from '../components/MetricChart'
import type { MetricType } from '../types'

const WINDOWS = [
  { value: '15m', label: 'Last 15m' },
  { value: '1h', label: 'Last 1h' },
  { value: '24h', label: 'Last 24h' }
]

const CHARTS: { type: MetricType; label: string; color: string; unit: string }[] = [
  { type: 'REQUEST_RATE', label: 'Request rate', color: '#38bdf8', unit: '/s' },
  { type: 'LATENCY_P50', label: 'Latency p50', color: '#a78bfa', unit: 'ms' },
  { type: 'LATENCY_P95', label: 'Latency p95', color: '#facc15', unit: 'ms' },
  { type: 'LATENCY_P99', label: 'Latency p99', color: '#fb923c', unit: 'ms' },
  { type: 'ERROR_RATE', label: 'Error rate', color: '#f87171', unit: '%' },
  { type: 'CPU', label: 'CPU', color: '#4ade80', unit: '%' },
  { type: 'MEMORY', label: 'Memory', color: '#22d3ee', unit: '%' }
]

export function ServiceDetail() {
  const { id } = useParams()
  const serviceId = Number(id)
  const [window, setWindow] = useState('1h')

  const { data: service } = usePolling(() => fetchService(serviceId), 5000, [serviceId])

  return (
    <div>
      <div className="page-header">
        <div>
          <Link to="/" className="back-link">
            ← All services
          </Link>
          <h1>
            {service?.name ?? '…'} {service && <StatusBadge status={service.status} />}
          </h1>
          <p className="page-sub">{service?.description}</p>
        </div>
        <div className="window-picker">
          {WINDOWS.map((w) => (
            <button key={w.value} className={w.value === window ? 'active' : ''} onClick={() => setWindow(w.value)}>
              {w.label}
            </button>
          ))}
        </div>
      </div>

      <div className="quick-links">
        <Link to={`/logs?serviceId=${serviceId}&window=${window}`} className="btn-ghost">
          View logs for this service in this window →
        </Link>
      </div>

      <div className="chart-grid">
        {CHARTS.map((c) => (
          <ChartCard key={c.type} serviceId={serviceId} window={window} config={c} />
        ))}
      </div>
    </div>
  )
}

function ChartCard({
  serviceId,
  window,
  config
}: {
  serviceId: number
  window: string
  config: { type: MetricType; label: string; color: string; unit: string }
}) {
  const { data: points } = usePolling(
    () => fetchServiceMetrics(serviceId, config.type, window),
    10000,
    [serviceId, window, config.type]
  )

  return (
    <div className="chart-card">
      <h4>{config.label}</h4>
      <MetricChart points={points ?? []} color={config.color} unit={config.unit} />
    </div>
  )
}
