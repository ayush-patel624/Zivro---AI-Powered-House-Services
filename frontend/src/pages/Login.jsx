import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../auth/AuthContext'
import { GoogleLogin } from '@react-oauth/google'

export default function Login() {
  const { user, login, loginWithGoogle } = useAuth()
  const location = useLocation()
  const from = location.state?.from?.pathname || '/dashboard'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to={from} replace />

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(email, password)
    } catch (err) {
      const msg = err.response?.data?.message || 'Unable to sign in. Check your email and password.'
      setError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleGoogleSuccess(credentialResponse) {
    setError('')
    try {
      await loginWithGoogle(credentialResponse.credential)
    } catch (err) {
      setError('Google sign in failed.')
    }
  }

  return (
    <div className="mx-auto flex max-w-md flex-col px-4 py-16">
      <h1 className="text-3xl font-bold text-white">Welcome back</h1>
      <p className="mt-2 text-slate-400">Sign in to manage bookings and ratings.</p>
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
            onError={() => setError('Google sign in failed.')}
            theme="filled_black"
            shape="pill"
          />
        </div>
        <div className="relative flex items-center py-2">
          <div className="flex-grow border-t border-white/10"></div>
          <span className="mx-4 flex-shrink-0 text-xs font-medium uppercase text-slate-500">or</span>
          <div className="flex-grow border-t border-white/10"></div>
        </div>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Email</span>
          <input
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Password</span>
          <input
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
        </label>
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green py-3.5 text-sm font-semibold text-zivro-ink disabled:opacity-60"
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
        <p className="text-center text-sm text-slate-500">
          New here?{' '}
          <Link to="/register" className="font-medium text-zivro-blue hover:underline">
            Create an account
          </Link>
        </p>
      </motion.form>
    </div>
  )
}
