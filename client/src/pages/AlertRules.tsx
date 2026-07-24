import { useEffect, useState, type FormEvent } from 'react'
import {
  createAlertRule,
  deleteAlertRule,
  fetchAlertRules,
  fetchAlerts,
  fetchServices,
  updateAlertRule
} from '../api/endpoints'
import { usePolling } from '../hooks/usePolling'
import { SeverityBadge } from '../components/SeverityBadge'
import type { ComparatorOp, MetricType, Severity, ServiceSummary } from '../types'

const METRIC_TYPES: MetricType[] = [
  'REQUEST_RATE',
  'LATENCY_P50',
  'LATENCY_P95',
  'LATENCY_P99',
  'ERROR_RATE',
  'CPU',
  'MEMORY'
]
const COMPARATORS: ComparatorOp[] = ['GREATER_THAN', 'GREATER_THAN_OR_EQUAL', 'LESS_THAN', 'LESS_THAN_OR_EQUAL']
const SEVERITIES: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export function AlertRules() {
  const { data: rules, refresh } = usePolling(fetchAlertRules, 15000)
  const { data: firing } = usePolling(() => fetchAlerts('FIRING'), 5000)
  const [services, setServices] = useState<ServiceSummary[]>([])

  useEffect(() => {
    fetchServices().then(setServices)
  }, [])

  return (
    <div>
      <div className="page-header">
        <h1>Alert Rules</h1>
        <p className="page-sub">The alerting engine evaluates these against live telemetry every 10 seconds.</p>
      </div>

      {firing && firing.length > 0 && (
        <div className="firing-panel">
          <h3>Currently firing ({firing.length})</h3>
          {firing.map((a) => (
            <div key={a.id} className="firing-row">
              <SeverityBadge severity={a.severity} />
              <span>{a.ruleName}</span>
              <span className="incident-alert-meta">
                {a.serviceName} · since {new Date(a.startedAt).toLocaleTimeString()}
              </span>
            </div>
          ))}
        </div>
      )}

      <NewRuleForm services={services} onCreated={refresh} />

      <table className="rule-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Scope</th>
            <th>Condition</th>
            <th>Duration</th>
            <th>Severity</th>
            <th>Enabled</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {rules?.map((rule) => (
            <tr key={rule.id}>
              <td>{rule.name}</td>
              <td>{rule.serviceName}</td>
              <td>
                {rule.metricType} {rule.comparator.replace(/_/g, ' ').toLowerCase()} {rule.threshold}
              </td>
              <td>{rule.durationSeconds}s</td>
              <td>
                <SeverityBadge severity={rule.severity} />
              </td>
              <td>
                <input
                  type="checkbox"
                  checked={rule.enabled}
                  onChange={async (e) => {
                    await updateAlertRule(rule.id, { ...rule, enabled: e.target.checked })
                    refresh()
                  }}
                />
              </td>
              <td>
                <button
                  className="btn-ghost"
                  onClick={async () => {
                    await deleteAlertRule(rule.id)
                    refresh()
                  }}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function NewRuleForm({ services, onCreated }: { services: ServiceSummary[]; onCreated: () => void }) {
  const [name, setName] = useState('')
  const [serviceId, setServiceId] = useState('')
  const [metricType, setMetricType] = useState<MetricType>('ERROR_RATE')
  const [comparator, setComparator] = useState<ComparatorOp>('GREATER_THAN')
  const [threshold, setThreshold] = useState(5)
  const [durationSeconds, setDurationSeconds] = useState(60)
  const [severity, setSeverity] = useState<Severity>('MEDIUM')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      await createAlertRule({
        name,
        serviceId: serviceId ? Number(serviceId) : null,
        metricType,
        comparator,
        threshold,
        durationSeconds,
        severity,
        enabled: true
      })
      setName('')
      onCreated()
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="new-rule-form" onSubmit={handleSubmit}>
      <input placeholder="Rule name" value={name} onChange={(e) => setName(e.target.value)} required />
      <select value={serviceId} onChange={(e) => setServiceId(e.target.value)}>
        <option value="">All services</option>
        {services.map((s) => (
          <option value={s.id} key={s.id}>
            {s.name}
          </option>
        ))}
      </select>
      <select value={metricType} onChange={(e) => setMetricType(e.target.value as MetricType)}>
        {METRIC_TYPES.map((m) => (
          <option key={m} value={m}>
            {m}
          </option>
        ))}
      </select>
      <select value={comparator} onChange={(e) => setComparator(e.target.value as ComparatorOp)}>
        {COMPARATORS.map((c) => (
          <option key={c} value={c}>
            {c.replace(/_/g, ' ')}
          </option>
        ))}
      </select>
      <input
        type="number"
        value={threshold}
        onChange={(e) => setThreshold(Number(e.target.value))}
        step="0.1"
        title="Threshold"
      />
      <input
        type="number"
        value={durationSeconds}
        onChange={(e) => setDurationSeconds(Number(e.target.value))}
        title="Sustained duration (seconds)"
      />
      <select value={severity} onChange={(e) => setSeverity(e.target.value as Severity)}>
        {SEVERITIES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
      <button type="submit" className="btn-primary" disabled={submitting}>
        Add rule
      </button>
    </form>
  )
}
