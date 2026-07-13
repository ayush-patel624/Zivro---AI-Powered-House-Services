import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { formatInr } from '../lib/format'
import { mediaUrl } from '../lib/serviceIcons'
import AiAnalysisCard from '../components/AiAnalysisCard'

function statusBadge(status) {
  const map = {
    PENDING: 'bg-amber-500/20 text-amber-300',
    ACCEPTED: 'bg-zivro-blue/20 text-zivro-blue',
    IN_PROGRESS: 'bg-purple-500/20 text-purple-200',
    COMPLETED: 'bg-zivro-green/20 text-zivro-green',
    CANCELLED: 'bg-slate-600/40 text-slate-300',
  }
  return map[status] || 'bg-slate-600/40 text-slate-300'
}

function Thumb({ url, label }) {
  if (!url) return null
  return (
    <div className="mt-2">
      <p className="text-xs text-slate-500">{label}</p>
      <img src={mediaUrl(url)} alt="" className="mt-1 h-20 w-28 rounded-lg border border-white/10 object-cover" />
    </div>
  )
}

function JobLocation({ booking }) {
  if (!booking.serviceAddress) return null
  return (
    <div className="mt-2 rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm">
      <p className="font-medium text-white">📍 {booking.locationLabel || 'Service location'}</p>
      <p className="mt-1 text-xs text-slate-400">{booking.serviceAddress}</p>
      {booking.mapsUrl && (
        <a
          href={booking.mapsUrl}
          target="_blank"
          rel="noreferrer"
          className="mt-2 inline-flex items-center gap-1 rounded-lg bg-zivro-blue/20 px-3 py-1.5 text-xs font-semibold text-zivro-blue hover:bg-zivro-blue/30"
        >
          Open in Google Maps →
        </a>
      )}
    </div>
  )
}

export default function WorkerJobs() {
  const { user } = useAuth()
  const [pool, setPool] = useState([])
  const [mine, setMine] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    if (!user || user.role !== 'WORKER') return
    setLoading(true)
    setError('')
    try {
      const [p, m] = await Promise.all([api.get('/api/bookings/unassigned'), api.get('/api/bookings/worker')])
      setPool(p.data)
      setMine(m.data)
    } catch {
      setError('Could not load worker jobs.')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    load()
  }, [load])

  async function postAction(path) {
    try {
      await api.post(path)
      load()
    } catch (e) {
      const msg = e.response?.data?.message || 'Action failed.'
      setError(msg)
    }
  }

  async function uploadWorkImage(bookingId, kind, file) {
    if (!file) return
    setError('')
    try {
      const fd = new FormData()
      fd.append('file', file)
      const url =
        kind === 'before'
          ? `/api/bookings/${bookingId}/images/before-work`
          : `/api/bookings/${bookingId}/images/after-work`
      await api.post(url, fd)
      load()
    } catch (e) {
      const msg = e.response?.data?.message || 'Upload failed.'
      setError(msg)
    }
  }

  if (!user) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center text-slate-400">
        <Link to="/login" className="text-zivro-blue hover:underline">
          Sign in
        </Link>
      </div>
    )
  }

  if (user.role !== 'WORKER') {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center text-slate-400">
        Worker account required.{' '}
        <Link to="/register" className="text-zivro-blue hover:underline">
          Register as worker
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-16">
      <h1 className="text-3xl font-bold text-white">Worker board</h1>
      <p className="mt-2 text-slate-400">
        See customer location before accepting. Open Google Maps for directions. Upload before/after photos to complete
        jobs.
      </p>
      {error && (
        <p className="mt-6 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {error}
        </p>
      )}
      {loading && <p className="mt-8 text-slate-500">Loading…</p>}

      <section className="mt-12">
        <h2 className="text-lg font-semibold text-white">Unassigned (claim)</h2>
        <ul className="mt-4 space-y-3">
          {pool.map((b) => (
            <li
              key={b.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-white/10 bg-white/[0.03] px-4 py-3"
            >
              <div>
                <p className="font-medium text-white">{b.service.name}</p>
                <p className="text-sm text-slate-500">
                  #{b.id} · {formatInr(b.price)} · {b.urgencyLevel}
                </p>
                <JobLocation booking={b} />
                <Thumb url={b.images?.referenceImageUrl} label="Customer reference" />
                {b.images?.aiAnalysis && (
                  <div className="mt-2 max-w-sm">
                    <AiAnalysisCard analysis={b.images.aiAnalysis} />
                  </div>
                )}
              </div>
              <button
                type="button"
                onClick={() => postAction(`/api/bookings/${b.id}/accept`)}
                className="rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green px-4 py-2 text-sm font-semibold text-zivro-ink"
              >
                Accept / claim
              </button>
            </li>
          ))}
        </ul>
        {!loading && pool.length === 0 && <p className="mt-4 text-sm text-slate-500">No open jobs right now.</p>}
      </section>

      <section className="mt-14">
        <h2 className="text-lg font-semibold text-white">My jobs</h2>
        <ul className="mt-4 space-y-4">
          {mine.map((b) => (
            <li key={b.id} className="rounded-2xl border border-white/10 bg-white/[0.03] px-4 py-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-medium text-white">{b.service.name}</p>
                  <p className="text-sm text-slate-500">#{b.id}</p>
                </div>
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusBadge(b.status)}`}>
                  {b.status}
                </span>
              </div>
              <p className="mt-2 text-sm text-zivro-green">{formatInr(b.price)}</p>
              <JobLocation booking={b} />
              <div className="mt-3 flex flex-wrap gap-4 text-xs text-slate-500">
                <Thumb url={b.images?.referenceImageUrl} label="Reference" />
                <Thumb url={b.images?.beforeWorkImageUrl} label="Before work" />
                <Thumb url={b.images?.afterWorkImageUrl} label="After work" />
              </div>
              {b.images?.aiAnalysis && (
                <div className="mt-3 max-w-sm">
                  <AiAnalysisCard analysis={b.images.aiAnalysis} />
                </div>
              )}
              <div className="mt-4 flex flex-wrap gap-2">
                {b.status === 'PENDING' && (
                  <>
                    <button
                      type="button"
                      onClick={() => postAction(`/api/bookings/${b.id}/accept`)}
                      className="rounded-lg bg-white/10 px-3 py-2 text-sm text-white hover:bg-white/15"
                    >
                      Accept invite
                    </button>
                    <button
                      type="button"
                      onClick={() => postAction(`/api/bookings/${b.id}/reject`)}
                      className="rounded-lg border border-white/15 px-3 py-2 text-sm text-slate-300 hover:bg-white/5"
                    >
                      Reject
                    </button>
                  </>
                )}
                {b.status === 'ACCEPTED' && (
                  <>
                    <label className="flex flex-col gap-1 text-xs text-slate-400">
                      <span>BEFORE_WORK (required before start)</span>
                      <span className="flex flex-wrap items-center gap-2">
                        <input
                          type="file"
                          accept="image/jpeg,image/png,image/webp,image/gif"
                          id={`before-${b.id}`}
                          className="max-w-[200px] text-xs file:rounded file:bg-white/10 file:px-2 file:py-1"
                        />
                        <button
                          type="button"
                          onClick={() => {
                            const el = document.getElementById(`before-${b.id}`)
                            const f = el?.files?.[0]
                            uploadWorkImage(b.id, 'before', f)
                          }}
                          className="rounded-lg bg-white/10 px-3 py-2 text-sm text-white"
                        >
                          Upload
                        </button>
                      </span>
                    </label>
                    <button
                      type="button"
                      disabled={!b.images?.beforeWorkImageUrl}
                      onClick={() => postAction(`/api/bookings/${b.id}/start`)}
                      className="rounded-lg bg-zivro-blue/20 px-3 py-2 text-sm font-medium text-zivro-blue disabled:cursor-not-allowed disabled:opacity-40"
                    >
                      Start job
                    </button>
                    <button
                      type="button"
                      onClick={() => postAction(`/api/bookings/${b.id}/cancel`)}
                      className="rounded-lg text-sm text-red-300 hover:underline"
                    >
                      Cancel before start
                    </button>
                  </>
                )}
                {b.status === 'IN_PROGRESS' && (
                  <>
                    <label className="flex flex-col gap-1 text-xs text-slate-400">
                      <span>AFTER_WORK (required before complete)</span>
                      <span className="flex flex-wrap items-center gap-2">
                        <input
                          type="file"
                          accept="image/jpeg,image/png,image/webp,image/gif"
                          id={`after-${b.id}`}
                          className="max-w-[200px] text-xs file:rounded file:bg-white/10 file:px-2 file:py-1"
                        />
                        <button
                          type="button"
                          onClick={() => {
                            const el = document.getElementById(`after-${b.id}`)
                            const f = el?.files?.[0]
                            uploadWorkImage(b.id, 'after', f)
                          }}
                          className="rounded-lg bg-white/10 px-3 py-2 text-sm text-white"
                        >
                          Upload
                        </button>
                      </span>
                    </label>
                    <button
                      type="button"
                      disabled={!b.images?.afterWorkImageUrl}
                      onClick={() => postAction(`/api/bookings/${b.id}/complete`)}
                      className="rounded-lg bg-zivro-green/20 px-3 py-2 text-sm font-medium text-zivro-green disabled:cursor-not-allowed disabled:opacity-40"
                    >
                      Mark complete
                    </button>
                  </>
                )}
              </div>
            </li>
          ))}
        </ul>
        {!loading && mine.length === 0 && (
          <p className="mt-4 text-sm text-slate-500">No assignments yet — check the unassigned list.</p>
        )}
      </section>
    </div>
  )
}
