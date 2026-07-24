export type ServiceStatus = 'HEALTHY' | 'DEGRADED' | 'DOWN'

export type MetricType =
  | 'REQUEST_RATE'
  | 'LATENCY_P50'
  | 'LATENCY_P95'
  | 'LATENCY_P99'
  | 'ERROR_RATE'
  | 'CPU'
  | 'MEMORY'

export type LogLevel = 'INFO' | 'WARN' | 'ERROR'
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type AlertState = 'FIRING' | 'RESOLVED'
export type IncidentStatus = 'OPEN' | 'RESOLVED'
export type ComparatorOp = 'GREATER_THAN' | 'GREATER_THAN_OR_EQUAL' | 'LESS_THAN' | 'LESS_THAN_OR_EQUAL'

export interface ServiceSummary {
  id: number
  name: string
  description: string
  status: ServiceStatus
  errorRatePercent: number
  p95LatencyMs: number
  requestRatePerSec: number
  requestRateSparkline: number[]
}

export interface MetricPoint {
  timestamp: string
  value: number
}

export interface LogEntry {
  id: number
  serviceId: number
  serviceName: string
  timestamp: string
  level: LogLevel
  message: string
  traceId: string | null
}

export interface AlertItem {
  id: number
  ruleId: number
  ruleName: string
  serviceId: number
  serviceName: string
  metricType: MetricType
  state: AlertState
  severity: Severity
  startedAt: string
  resolvedAt: string | null
  triggeringValue: number
  incidentId: number | null
}

export interface AlertRule {
  id: number
  name: string
  serviceId: number | null
  serviceName: string
  metricType: MetricType
  comparator: ComparatorOp
  threshold: number
  durationSeconds: number
  severity: Severity
  enabled: boolean
}

export interface Incident {
  id: number
  title: string
  serviceId: number
  serviceName: string
  severity: Severity
  startedAt: string
  resolvedAt: string | null
  status: IncidentStatus
  alerts: AlertItem[]
}
