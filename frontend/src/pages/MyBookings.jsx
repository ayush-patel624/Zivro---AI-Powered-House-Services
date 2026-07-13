import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import api from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { formatInr } from '../lib/format'
import { openRazorpayCheckout } from '../lib/razorpayCheckout'
import { mediaUrl } from '../lib/serviceIcons'
import AiAnalysisCard from '../components/AiAnalysisCard'
import NearbyWorkersPanel from '../components/NearbyWorkersPanel'

function statusStyle(status) {
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
    <div>
      <p className="text-[10px] uppercase tracking-wide text-slate-500">{label}</p>
      <img src={mediaUrl(url)} alt="" className="mt-1 h-16 w-24 rounded-md border border-white/10 object-cover" />
    </div>
  )
}

export default function MyBookings() {
  const { user } = useAuth()
  const routeLocation = useLocation()
  const highlightBookingId = routeLocation.state?.highlightBookingId
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [ratingFor, setRatingFor] = useState(null)
  const [workerStars, setWorkerStars] = useState(5)
  const [satisfactionStars, setSatisfactionStars] = useState(5)
  const [feedback, setFeedback] = useState('')
  const [ratingSubmitting, setRatingSubmitting] = useState(false)
  const [payingId, setPayingId] = useState(null)
  const [disputeFor, setDisputeFor] = useState(null)
  const [disputeReason, setDisputeReason] = useState('')
  const [disputeSubmitting, setDisputeSubmitting] = useState(false)

  const load = useCallback(() => {
    if (!user) return
    setLoading(true)
    setError('')
    api
      .get('/api/bookings/my')
      .then((res) => setRows(res.data))
      .catch(() => setError('Could not load bookings.'))
      .finally(() => setLoading(false))
  }, [user])

  useEffect(() => {
    load()
  }, [load])

  async function cancelBooking(id) {
    if (!window.confirm('Cancel this booking?')) return
    try {
      await api.post(`/api/bookings/${id}/cancel`)
      load()
    } catch {
      setError('Cancel failed.')
    }
  }

  async function payBooking(b) {
    const { data: payCfg } = await api.get('/api/payments/public-config')
    if (!payCfg.razorpayEnabled || !payCfg.razorpayKeyId || !b.activeRazorpayOrderId) {
      setError('Online payment is not available.')
      return
    }
    setPayingId(b.id)
    setError('')
    try {
      await openRazorpayCheckout({
        key: payCfg.razorpayKeyId,
        orderId: b.activeRazorpayOrderId,
        description:
          b.razorpayBalanceOrderId && b.activeRazorpayOrderId === b.razorpayBalanceOrderId
            ? `Balance · booking #${b.id}`
            : `Deposit · booking #${b.id}`,
        prefill: { email: user.email, name: user.name },
        handler: async (rzpRes) => {
          await api.post(`/api/bookings/${b.id}/payments/verify`, {
            orderId: rzpRes.razorpay_order_id,
            paymentId: rzpRes.razorpay_payment_id,
            signature: rzpRes.razorpay_signature,
          })
          load()
        },
      })
    } catch (e) {
      setError(e.message || 'Payment could not start.')
    } finally {
      setPayingId(null)
    }
  }

  async function submitDispute(bookingId) {
    if (!disputeReason.trim()) {
      setError('Please describe the issue.')
      return
    }
    setDisputeSubmitting(true)
    setError('')
    try {
      await api.post(`/api/bookings/${bookingId}/disputes`, { reason: disputeReason.trim() })
      setDisputeFor(null)
      setDisputeReason('')
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not submit dispute.')
    } finally {
      setDisputeSubmitting(false)
    }
  }

  async function submitRating(bookingId) {
    setRatingSubmitting(true)
    setError('')
    try {
      await api.post(`/api/bookings/${bookingId}/rating`, {
        workerStars,
        satisfactionStars,
        feedback: feedback || undefined,
      })
      setRatingFor(null)
      setFeedback('')
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not submit rating.')
    } finally {
      setRatingSubmitting(false)
    }
  }

  if (!user) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center">
        <p className="text-slate-400">Sign in to see your bookings.</p>
        <Link to="/login" className="mt-4 inline-block text-zivro-blue hover:underline">
          Sign in
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-16">
      <h1 className="text-3xl font-bold text-white">My bookings</h1>
      <p className="mt-2 text-slate-400">Photos and ratings sync with the Spring Boot workflow.</p>
      {loading && <p className="mt-8 text-slate-500">Loading…</p>}
      {error && <p className="mt-8 text-sm text-red-300">{error}</p>}
      <ul className="mt-8 space-y-4">
        {rows.map((b, i) => (
          <motion.li
            key={b.id}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.04 }}
            className="rounded-2xl border border-white/10 bg-white/[0.03] p-5"
          >
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="font-semibold text-white">{b.service.name}</p>
                <p className="mt-1 text-sm text-slate-500">
                  {new Date(b.bookingTime).toLocaleString()} · {b.urgencyLevel}
                </p>
                {b.serviceAddress && (
                  <p className="mt-1 text-xs text-slate-500">
                    📍 {b.locationLabel}: {b.serviceAddress}
                  </p>
                )}
                {b.mapsUrl && (
                  <a
                    href={b.mapsUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="mt-1 inline-block text-xs text-zivro-blue hover:underline"
                  >
                    Open in Google Maps
                  </a>
                )}
                <p className="mt-2 text-lg font-medium text-zivro-green">{formatInr(b.price)}</p>
                {b.paymentStatus && b.paymentStatus !== 'NOT_CONFIGURED' && (
                  <p className="mt-1 text-xs text-slate-500">
                    Payment: <span className="text-slate-300">{b.paymentStatus}</span>
                    {b.amountPaid != null && (
                      <>
                        {' '}
                        · paid {formatInr(b.amountPaid)}
                        {b.finalPriceAfterSatisfaction != null && (
                          <> · final {formatInr(b.finalPriceAfterSatisfaction)}</>
                        )}
                      </>
                    )}
                  </p>
                )}
                {b.activeRazorpayOrderId && (
                  <button
                    type="button"
                    disabled={payingId === b.id}
                    onClick={() => payBooking(b)}
                    className="mt-3 rounded-lg bg-zivro-blue/20 px-4 py-2 text-sm font-medium text-zivro-blue disabled:opacity-50"
                  >
                    {payingId === b.id
                      ? 'Opening checkout…'
                      : b.razorpayBalanceOrderId && b.activeRazorpayOrderId === b.razorpayBalanceOrderId
                        ? `Pay balance ${formatInr(b.amountDueNext)}`
                        : `Pay deposit ${formatInr(b.amountDueNext)}`}
                  </button>
                )}
                {b.images && (
                  <div className="mt-3 flex flex-wrap gap-4">
                    <Thumb url={b.images.referenceImageUrl} label="Your reference" />
                    <Thumb url={b.images.beforeWorkImageUrl} label="Before work" />
                    <Thumb url={b.images.afterWorkImageUrl} label="After work" />
                  </div>
                )}
                {b.images?.aiAnalysis && (
                  <div className="mt-3 max-w-md">
                    <AiAnalysisCard analysis={b.images.aiAnalysis} />
                  </div>
                )}
              </div>
              <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusStyle(b.status)}`}>
                {b.status}
              </span>
            </div>
            {b.status === 'PENDING' && !b.workerId && (
              <NearbyWorkersPanel bookingId={b.id} active onAssigned={load} />
            )}
            {b.workerEmployeeId && (
              <p className="mt-3 text-sm text-zivro-green">
                Assigned worker: <span className="font-medium">{b.workerEmployeeId}</span>
              </p>
            )}
            {b.rating && (
              <p className="mt-3 text-sm text-slate-400">
                Your rating: worker {b.rating.workerStars}/5 · satisfaction {b.rating.satisfactionStars}/5
                {b.rating.feedback ? ` · “${b.rating.feedback}”` : ''}
              </p>
            )}
            {b.status === 'COMPLETED' && !b.rating && (
              <div className="mt-4 border-t border-white/10 pt-4">
                {ratingFor === b.id ? (
                  <div className="space-y-3">
                    <p className="text-sm font-medium text-white">Rate this visit</p>
                    <label className="flex items-center gap-2 text-sm text-slate-400">
                      Worker quality (1–5)
                      <input
                        type="number"
                        min={1}
                        max={5}
                        value={workerStars}
                        onChange={(e) => setWorkerStars(Number(e.target.value))}
                        className="w-16 rounded border border-white/10 bg-zivro-ink px-2 py-1 text-white"
                      />
                    </label>
                    <label className="flex items-center gap-2 text-sm text-slate-400">
                      Overall satisfaction (1–5)
                      <input
                        type="number"
                        min={1}
                        max={5}
                        value={satisfactionStars}
                        onChange={(e) => setSatisfactionStars(Number(e.target.value))}
                        className="w-16 rounded border border-white/10 bg-zivro-ink px-2 py-1 text-white"
                      />
                    </label>
                    <textarea
                      placeholder="Optional feedback"
                      value={feedback}
                      onChange={(e) => setFeedback(e.target.value)}
                      rows={2}
                      className="w-full rounded-lg border border-white/10 bg-zivro-ink px-3 py-2 text-sm text-white"
                    />
                    <div className="flex gap-2">
                      <button
                        type="button"
                        disabled={ratingSubmitting}
                        onClick={() => submitRating(b.id)}
                        className="rounded-lg bg-zivro-green/20 px-4 py-2 text-sm font-medium text-zivro-green"
                      >
                        {ratingSubmitting ? 'Submitting…' : 'Submit'}
                      </button>
                      <button
                        type="button"
                        onClick={() => setRatingFor(null)}
                        className="rounded-lg px-4 py-2 text-sm text-slate-400"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => {
                      setRatingFor(b.id)
                      setWorkerStars(5)
                      setSatisfactionStars(5)
                      setFeedback('')
                    }}
                    className="text-sm font-medium text-zivro-blue hover:underline"
                  >
                    Rate worker & satisfaction
                  </button>
                )}
              </div>
            )}
            {b.status === 'COMPLETED' && disputeFor === b.id && (
              <div className="mt-4 border-t border-white/10 pt-4">
                <p className="text-sm font-medium text-white">Open a dispute</p>
                <textarea
                  value={disputeReason}
                  onChange={(e) => setDisputeReason(e.target.value)}
                  rows={3}
                  className="mt-2 w-full rounded-lg border border-white/10 bg-zivro-ink px-3 py-2 text-sm text-white"
                  placeholder="Describe what went wrong"
                />
                <div className="mt-2 flex gap-2">
                  <button
                    type="button"
                    disabled={disputeSubmitting}
                    onClick={() => submitDispute(b.id)}
                    className="rounded-lg bg-amber-500/20 px-4 py-2 text-sm font-medium text-amber-200"
                  >
                    {disputeSubmitting ? 'Submitting…' : 'Submit dispute'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setDisputeFor(null)
                      setDisputeReason('')
                    }}
                    className="rounded-lg px-4 py-2 text-sm text-slate-400"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
            {b.status === 'COMPLETED' && disputeFor !== b.id && (
              <button
                type="button"
                onClick={() => {
                  setDisputeFor(b.id)
                  setDisputeReason('')
                }}
                className="mt-4 text-sm font-medium text-amber-200/90 hover:text-amber-100"
              >
                Report a problem
              </button>
            )}
            {(b.status === 'PENDING' || b.status === 'ACCEPTED') && (
              <button
                type="button"
                onClick={() => cancelBooking(b.id)}
                className="mt-4 text-sm font-medium text-red-300 hover:text-red-200"
              >
                Cancel booking
              </button>
            )}
          </motion.li>
        ))}
      </ul>
      {!loading && rows.length === 0 && (
        <p className="mt-10 text-slate-500">
          No bookings yet.{' '}
          <Link to="/services" className="text-zivro-blue hover:underline">
            Browse services
          </Link>
        </p>
      )}
    </div>
  )
}
