import type { ServiceStatus } from '../types'

const STYLES: Record<ServiceStatus, { bg: string; fg: string; label: string }> = {
  HEALTHY: { bg: '#0f3d2a', fg: '#4ade80', label: 'Healthy' },
  DEGRADED: { bg: '#3d3410', fg: '#facc15', label: 'Degraded' },
  DOWN: { bg: '#3d1414', fg: '#f87171', label: 'Down' }
}

export function StatusBadge({ status }: { status: ServiceStatus }) {
  const style = STYLES[status]
  return (
    <span
      className="status-badge"
      style={{ backgroundColor: style.bg, color: style.fg }}
    >
      <span className="status-dot" style={{ backgroundColor: style.fg }} />
      {style.label}
    </span>
  )
}
