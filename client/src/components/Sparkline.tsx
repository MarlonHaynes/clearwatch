import { LineChart, Line, ResponsiveContainer, YAxis } from 'recharts'

export function Sparkline({ values, color = '#38bdf8' }: { values: number[]; color?: string }) {
  if (!values || values.length < 2) {
    return <div className="sparkline-empty">collecting data…</div>
  }
  const data = values.map((v, i) => ({ i, v }))
  return (
    <ResponsiveContainer width="100%" height={40}>
      <LineChart data={data}>
        <YAxis hide domain={['dataMin', 'dataMax']} />
        <Line type="monotone" dataKey="v" stroke={color} strokeWidth={2} dot={false} isAnimationActive={false} />
      </LineChart>
    </ResponsiveContainer>
  )
}
