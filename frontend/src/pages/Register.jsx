import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../auth/AuthContext'
import { GoogleLogin } from '@react-oauth/google'

export default function Register() {
  const { user, register, loginWithGoogle } = useAuth()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [phone, setPhone] = useState('')
  const [address, setAddress] = useState('')
  const [role, setRole] = useState('USER')
  const [workerCategory, setWorkerCategory] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to="/dashboard" replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await register({
        name,
        email,
        password,
        phone: phone || undefined,
        address: address || undefined,
        role,
        workerCategory: role === 'WORKER' ? workerCategory : undefined,
      })
    } catch (err) {
      const body = err.response?.data
      const msg =
        body?.message ||
        (body?.fieldErrors && Object.values(body.fieldErrors).join(' ')) ||
        'Registration failed.'
      setError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    setError('')
    try {
      await loginWithGoogle(credentialResponse.credential, {
        role,
        workerCategory: role === 'WORKER' ? workerCategory : undefined,
      })
    } catch (err) {
      const body = err.response?.data
      const msg = body?.message || 'Google sign up failed.'
      setError(msg)
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col px-4 py-16">
      <h1 className="text-3xl font-bold text-white">Join Zivro</h1>
      <p className="mt-2 text-slate-400">Customers book services; workers get a generated employee ID.</p>
      <motion.form
        onSubmit={handleSubmit}
        className="mt-10 space-y-5 rounded-3xl border border-white/10 bg-white/[0.03] p-8 backdrop-blur"
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
      >
        {error && (
          <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
            {error}
          </div>
        )}
        <div className="flex justify-center">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => setError('Google sign up failed.')}
            theme="filled_black"
            shape="pill"
            text="signup_with"
          />
        </div>
        <div className="relative flex items-center py-2">
          <div className="flex-grow border-t border-white/10"></div>
          <span className="mx-4 flex-shrink-0 text-xs font-medium uppercase text-slate-500">or</span>
          <div className="flex-grow border-t border-white/10"></div>
        </div>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Full name</span>
          <input
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Email</span>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Password (min 8)</span>
          <input
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Phone (optional)</span>
          <input
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Address (optional)</span>
          <input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <fieldset>
          <legend className="text-xs font-medium uppercase tracking-wide text-slate-500">I am signing up as</legend>
          <div className="mt-3 flex gap-3">
            {['USER', 'WORKER'].map((r) => (
              <label
                key={r}
                className={`flex flex-1 cursor-pointer items-center justify-center rounded-xl border px-3 py-3 text-sm font-medium ${
                  role === r
                    ? 'border-zivro-blue/60 bg-zivro-blue/10 text-white'
                    : 'border-white/10 text-slate-400 hover:border-white/20'
                }`}
              >
                <input
                  type="radio"
                  name="role"
                  value={r}
                  checked={role === r}
                  onChange={() => setRole(r)}
                  className="sr-only"
                />
                {r === 'USER' ? 'Customer' : 'Worker'}
              </label>
            ))}
          </div>
        </fieldset>
        {role === 'WORKER' && (
          <label className="block">
            <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Primary category</span>
            <input
              required
              placeholder="e.g. Deep cleaning, Plumbing"
              value={workerCategory}
              onChange={(e) => setWorkerCategory(e.target.value)}
              className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
            />
          </label>
        )}
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green py-3.5 text-sm font-semibold text-zivro-ink disabled:opacity-60"
        >
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
        <p className="text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-zivro-blue hover:underline">
            Sign in
          </Link>
        </p>
      </motion.form>
    </div>
  )
}
