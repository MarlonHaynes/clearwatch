import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, CartesianGrid } from 'recharts'
import type { MetricPoint } from '../types'

function formatTime(ts: string) {
  const d = new Date(ts)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function MetricChart({
  points,
  color = '#38bdf8',
  unit = '',
  height = 220
}: {
  points: MetricPoint[]
  color?: string
  unit?: string
  height?: number
}) {
  if (!points || points.length === 0) {
    return <div className="chart-empty">No data in this window yet</div>
  }
  const data = points.map((p) => ({ time: p.timestamp, value: p.value }))

  return (
    <ResponsiveContainer width="100%" height={height}>
      <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
        <XAxis dataKey="time" tickFormatter={formatTime} stroke="#64748b" minTickGap={40} />
        <YAxis stroke="#64748b" width={50} />
        <Tooltip
          contentStyle={{ background: '#0f172a', border: '1px solid #1e293b' }}
          labelFormatter={(v) => new Date(v as string).toLocaleString()}
          formatter={(value: number) => [`${value}${unit}`, '']}
        />
        <Line type="monotone" dataKey="value" stroke={color} strokeWidth={2} dot={false} isAnimationActive={false} />
      </LineChart>
    </ResponsiveContainer>
  )
}
