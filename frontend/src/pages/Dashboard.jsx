import { Navigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../auth/AuthContext'

export default function Dashboard() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-slate-400">
        Loading your profile…
      </div>
    )
  }

  if (!user) return <Navigate to="/login" replace state={{ from: { pathname: '/dashboard' } }} />

  return (
    <div className="mx-auto max-w-3xl px-4 py-16">
      <h1 className="text-3xl font-bold text-white">Account</h1>
      <p className="mt-2 text-slate-400">Loaded from JWT + <code className="text-zivro-blue">GET /api/auth/me</code>.</p>
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="mt-10 space-y-4 rounded-3xl border border-white/10 bg-white/[0.03] p-8"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-500">Name</p>
            <p className="mt-1 font-medium text-white">{user.name}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-500">Email</p>
            <p className="mt-1 font-medium text-white">{user.email}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-500">Role</p>
            <p className="mt-1 font-medium text-zivro-green">{user.role}</p>
          </div>
          {user.phone && (
            <div>
              <p className="text-xs uppercase tracking-wide text-slate-500">Phone</p>
              <p className="mt-1 font-medium text-white">{user.phone}</p>
            </div>
          )}
        </div>
        {user.worker && (
          <div className="border-t border-white/10 pt-6">
            <p className="text-xs uppercase tracking-wide text-slate-500">Worker profile</p>
            <dl className="mt-3 grid gap-3 text-sm sm:grid-cols-2">
              <div>
                <dt className="text-slate-500">Employee ID</dt>
                <dd className="font-mono text-white">{user.worker.employeeId}</dd>
              </div>
              <div>
                <dt className="text-slate-500">Category</dt>
                <dd className="text-white">{user.worker.category}</dd>
              </div>
              <div>
                <dt className="text-slate-500">Verified</dt>
                <dd className="text-white">{user.worker.verified ? 'Yes' : 'Pending admin'}</dd>
              </div>
              <div>
                <dt className="text-slate-500">Deposit</dt>
                <dd className="text-white">{user.worker.depositPaid ? 'Paid' : 'Not paid'}</dd>
              </div>
            </dl>
          </div>
        )}
      </motion.div>
    </div>
  )
}
