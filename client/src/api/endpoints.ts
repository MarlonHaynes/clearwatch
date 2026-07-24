import { api } from './client'
import type {
  AlertItem,
  AlertRule,
  Incident,
  LogEntry,
  MetricPoint,
  MetricType,
  ServiceSummary
} from '../types'

export async function login(email: string, password: string) {
  const res = await api.post('/api/auth/login', { email, password })
  return res.data as { token: string; email: string; role: string }
}

export async function fetchServices() {
  const res = await api.get('/api/services')
  return res.data as ServiceSummary[]
}

export async function fetchService(id: number) {
  const res = await api.get(`/api/services/${id}`)
  return res.data as ServiceSummary
}

export async function fetchServiceMetrics(id: number, type: MetricType, window: string) {
  const res = await api.get(`/api/services/${id}/metrics`, { params: { type, window } })
  return res.data as MetricPoint[]
}

export async function fetchLogs(params: {
  serviceId?: number
  level?: string
  q?: string
  window?: string
  limit?: number
}) {
  const res = await api.get('/api/logs', { params })
  return res.data as LogEntry[]
}

export async function fetchAlerts(state?: string) {
  const res = await api.get('/api/alerts', { params: state ? { state } : {} })
  return res.data as AlertItem[]
}

export async function fetchAlertRules() {
  const res = await api.get('/api/alert-rules')
  return res.data as AlertRule[]
}

export async function createAlertRule(rule: Partial<AlertRule>) {
  const res = await api.post('/api/alert-rules', rule)
  return res.data as AlertRule
}

export async function updateAlertRule(id: number, rule: Partial<AlertRule>) {
  const res = await api.put(`/api/alert-rules/${id}`, rule)
  return res.data as AlertRule
}

export async function deleteAlertRule(id: number) {
  await api.delete(`/api/alert-rules/${id}`)
}

export async function fetchIncidents() {
  const res = await api.get('/api/incidents')
  return res.data as Incident[]
}

export async function fetchIncident(id: number) {
  const res = await api.get(`/api/incidents/${id}`)
  return res.data as Incident
}
