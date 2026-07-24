import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { fetchLogs, fetchServices } from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import type { ServiceSummary } from '../types'

const WINDOWS = ['15m', '1h', '24h']
const LEVELS = ['', 'INFO', 'WARN', 'ERROR']

export function Logs() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [services, setServices] = useState<ServiceSummary[]>([])

  const serviceId = searchParams.get('serviceId') ?? ''
  const level = searchParams.get('level') ?? ''
  const windowParam = searchParams.get('window') ?? '1h'
  const q = searchParams.get('q') ?? ''

  useEffect(() => {
    fetchServices().then(setServices)
  }, [])

  const { data: logs, loading } = usePolling(
    () =>
      fetchLogs({
        serviceId: serviceId ? Number(serviceId) : undefined,
        level: level || undefined,
        q: q || undefined,
        window: windowParam,
        limit: 300
      }),
    6000,
    [serviceId, level, windowParam, q]
  )

  function updateParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams)
    if (value) next.set(key, value)
    else next.delete(key)
    setSearchParams(next)
  }

  return (
    <div>
      <div className="page-header">
        <h1>Log Explorer</h1>
        <p className="page-sub">Full-text search across the fleet, filterable by service, level and time window.</p>
      </div>

      <div className="log-filters">
        <input
          placeholder="Search message text…"
          defaultValue={q}
          onKeyDown={(e) => {
            if (e.key === 'Enter') updateParam('q', (e.target as HTMLInputElement).value)
          }}
          onBlur={(e) => updateParam('q', e.target.value)}
        />
        <select value={serviceId} onChange={(e) => updateParam('serviceId', e.target.value)}>
          <option value="">All services</option>
          {services.map((s) => (
            <option value={s.id} key={s.id}>
              {s.name}
            </option>
          ))}
        </select>
        <select value={level} onChange={(e) => updateParam('level', e.target.value)}>
          {LEVELS.map((l) => (
            <option value={l} key={l}>
              {l || 'All levels'}
            </option>
          ))}
        </select>
        <div className="window-picker">
          {WINDOWS.map((w) => (
            <button key={w} className={w === windowParam ? 'active' : ''} onClick={() => updateParam('window', w)}>
              {w}
            </button>
          ))}
        </div>
      </div>

      {loading && !logs && <div className="empty-state">Loading logs…</div>}
      {logs && logs.length === 0 && <div className="empty-state">No log lines match these filters.</div>}

      <div className="log-list">
        {logs?.map((log) => (
          <div key={log.id} className={`log-row log-level-${log.level.toLowerCase()}`}>
            <span className="log-time">{new Date(log.timestamp).toLocaleString()}</span>
            <span className="log-service">{log.serviceName}</span>
            <span className="log-level">{log.level}</span>
            <span className="log-message">{log.message}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
