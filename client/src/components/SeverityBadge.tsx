import type { Severity } from '../types'

const STYLES: Record<Severity, { bg: string; fg: string }> = {
  LOW: { bg: '#1e293b', fg: '#94a3b8' },
  MEDIUM: { bg: '#3d3410', fg: '#facc15' },
  HIGH: { bg: '#3d2410', fg: '#fb923c' },
  CRITICAL: { bg: '#3d1414', fg: '#f87171' }
}

export function SeverityBadge({ severity }: { severity: Severity }) {
  const style = STYLES[severity]
  return (
    <span className="severity-badge" style={{ backgroundColor: style.bg, color: style.fg }}>
      {severity}
    </span>
  )
}
