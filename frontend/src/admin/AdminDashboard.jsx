import { useEffect, useState } from 'react'//admin dashboard page, shows some stats and charts for the admin to see. Uses recharts for the charts
import {
  Bar,
  BarChart,
  CartesianGrid,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  Cell,
  Legend,
} from 'recharts'
import api from '../api/client'
import { formatInr } from '../lib/format'

const PIE_COLORS = ['#38bdf8', '#4ade80', '#a78bfa', '#f472b6', '#fbbf24', '#94a3b8']

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .get('/api/admin/dashboard')
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load dashboard.'))
  }, [])

  if (error) {
    return <p className="text-sm text-red-300">{error}</p>
  }
  if (!data) {
    return <p className="text-slate-500">Loading analytics…</p>
  }

  const bookingChart = (data.bookingsByStatus || []).map((r) => ({
    name: r.status,
    count: r.count,
  }))
  const paymentChart = (data.paymentsByStatus || []).map((r) => ({
    name: r.status,
    value: r.count,
  }))

  return (
    <div className="space-y-10">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total users" value={data.totalUsers} />
        <StatCard label="Customers" value={data.usersByRoleUser} />
        <StatCard label="Workers" value={data.usersByRoleWorker} />
        <StatCard label="Open disputes" value={data.openDisputes} accent="text-amber-300" />
        <StatCard label="Workers verified" value={data.workersVerified} />
        <StatCard label="Workers pending" value={data.workersUnverified} />
        <StatCard label="Admins" value={data.usersByRoleAdmin} />
        <StatCard
          label="Captured payments (sum)"
          value={formatInr(data.totalAmountPaid)}
          sub="amountPaid field"
        />
      </div>

      <div className="grid gap-8 lg:grid-cols-2">
        <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-4">
          <h2 className="text-sm font-semibold text-white">Bookings by status</h2>
          <div className="mt-4 h-72 w-full min-w-0">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={bookingChart} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={{ stroke: '#334155' }} />
                <YAxis allowDecimals={false} tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={{ stroke: '#334155' }} />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 8 }}
                  labelStyle={{ color: '#e2e8f0' }}
                />
                <Bar dataKey="count" fill="#38bdf8" radius={[4, 4, 0, 0]} name="Bookings" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-4">
          <h2 className="text-sm font-semibold text-white">Payment status mix</h2>
          <div className="mt-4 h-72 w-full min-w-0">
            {paymentChart.length === 0 ? (
              <p className="flex h-full items-center justify-center text-sm text-slate-500">No payment rows yet.</p>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={paymentChart}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={48}
                    outerRadius={96}
                    paddingAngle={2}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  >
                    {paymentChart.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 8 }}
                  />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function StatCard({ label, value, sub, accent }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/[0.03] p-5">
      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</p>
      <p className={`mt-2 text-2xl font-bold text-white ${accent || ''}`}>{value}</p>
      {sub && <p className="mt-1 text-xs text-slate-500">{sub}</p>}
    </div>
  )
}
