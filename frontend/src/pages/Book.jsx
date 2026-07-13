import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import api from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { formatInr } from '../lib/format'
import { openRazorpayCheckout } from '../lib/razorpayCheckout'
import { getServiceIcon } from '../lib/serviceIcons'
import { clientAiFallback } from '../lib/clientAiFallback'
import LocationPicker from '../components/LocationPicker'
import AiAnalysisCard from '../components/AiAnalysisCard'

const URGENCIES = [
  { value: 'NORMAL', label: 'Normal' },
  { value: 'URGENT', label: 'Urgent' },
  { value: 'SAME_DAY', label: 'Same day' },
]

export default function Book() {
  const { serviceId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const cameraRef = useRef(null)
  const [service, setService] = useState(null)
  const [urgency, setUrgency] = useState('NORMAL')
  const [quote, setQuote] = useState(null)
  const [scheduleOpen, setScheduleOpen] = useState(false)
  const [scheduleDate, setScheduleDate] = useState('')
  const [timeSlot, setTimeSlot] = useState('')
  const [referenceFile, setReferenceFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [aiAnalysis, setAiAnalysis] = useState(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState('')
  const [location, setLocation] = useState(null)
  const [locationError, setLocationError] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const TIME_SLOTS = [
    { value: '08:00', label: '8:00 AM – 9:00 AM' },
    { value: '09:00', label: '9:00 AM – 10:00 AM' },
    { value: '10:00', label: '10:00 AM – 11:00 AM' },
    { value: '11:00', label: '11:00 AM – 12:00 PM' },
    { value: '12:00', label: '12:00 PM – 1:00 PM' },
    { value: '13:00', label: '1:00 PM – 2:00 PM' },
    { value: '14:00', label: '2:00 PM – 3:00 PM' },
    { value: '15:00', label: '3:00 PM – 4:00 PM' },
    { value: '16:00', label: '4:00 PM – 5:00 PM' },
    { value: '17:00', label: '5:00 PM – 6:00 PM' },
  ]

  function resolveScheduledAtIso() {
    if (!scheduleDate || !timeSlot) return undefined
    const d = new Date(`${scheduleDate}T${timeSlot}:00`)
    if (Number.isNaN(d.getTime())) return undefined
    return d.toISOString()
  }

  useEffect(() => {
    api
      .get(`/api/services/${serviceId}`)
      .then((res) => setService(res.data))
      .catch(() => setError('Service not found.'))
  }, [serviceId])

  useEffect(() => {
    if (!serviceId || !service) return
    api
      .get(`/api/services/${serviceId}/quote`, { params: { urgency } })
      .then((res) => setQuote(res.data))
      .catch(() => setQuote(null))
  }, [serviceId, service, urgency])

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl)
    }
  }, [previewUrl])

  async function analyzeImage(file) {
    if (!file) return
    setAiLoading(true)
    setAiAnalysis(null)
    setAiError('')
    try {
      const fd = new FormData()
      fd.append('image', file)
      const { data } = await api.post('/api/ai/analyze-image', fd, {
        params: { serviceIconKey: service?.iconKey },
      })
      setAiAnalysis(data)
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        (err.response?.status === 401
          ? 'Sign in to run AI analysis, or continue — analysis runs again when you confirm booking.'
          : 'Could not reach AI service. Showing a preview estimate.')
      setAiError(msg)
      setAiAnalysis(clientAiFallback(service?.iconKey))
    } finally {
      setAiLoading(false)
    }
  }

  function handleFileChange(file) {
    setReferenceFile(file)
    if (previewUrl) URL.revokeObjectURL(previewUrl)
    if (file) {
      setPreviewUrl(URL.createObjectURL(file))
      analyzeImage(file)
    } else {
      setPreviewUrl('')
      setAiAnalysis(null)
      setAiError('')
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!user) {
      navigate('/login', { state: { from: { pathname: `/book/${serviceId}` } } })
      return
    }
    if (!referenceFile) {
      setError('Please attach or capture a reference photo.')
      return
    }
    if (!location?.latitude || !location?.longitude || !location?.address) {
      setLocationError('Service location is required before booking.')
      return
    }
    setError('')
    setLocationError('')
    setSubmitting(true)
    try {
      const scheduledAtIso = resolveScheduledAtIso()
      const payload = {
        serviceId: Number(serviceId),
        urgencyLevel: urgency,
        scheduledAt: scheduledAtIso,
        location,
      }
      const formData = new FormData()
      formData.append('booking', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
      formData.append('referenceImage', referenceFile)
      const { data: booking } = await api.post('/api/bookings', formData)
      const { data: payCfg } = await api.get('/api/payments/public-config')
      if (payCfg.razorpayEnabled && booking.activeRazorpayOrderId && payCfg.razorpayKeyId) {
        await openRazorpayCheckout({
          key: payCfg.razorpayKeyId,
          orderId: booking.activeRazorpayOrderId,
          description: `Deposit · booking #${booking.id}`,
          prefill: { email: user.email, name: user.name },
          handler: async (rzpRes) => {
            await api.post(`/api/bookings/${booking.id}/payments/verify`, {
              orderId: rzpRes.razorpay_order_id,
              paymentId: rzpRes.razorpay_payment_id,
              signature: rzpRes.razorpay_signature,
            })
            navigate('/bookings', { state: { highlightBookingId: booking.id } })
          },
        })
        return
      }
      navigate('/bookings', { state: { highlightBookingId: booking.id } })
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        (err.response?.data?.fieldErrors && Object.values(err.response.data.fieldErrors).join(' ')) ||
        'Could not create booking.'
      setError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  if (!service && !error) {
    return <div className="px-4 py-20 text-center text-slate-500">Loading…</div>
  }

  if (error && !service) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center">
        <p className="text-red-300">{error}</p>
        <Link to="/services" className="mt-6 inline-block text-zivro-blue hover:underline">
          Back to catalog
        </Link>
      </div>
    )
  }

  const icon = getServiceIcon(service?.iconKey)

  return (
    <div className="mx-auto max-w-lg px-4 py-16">
      <Link to="/services" className="text-sm text-zivro-blue hover:underline">
        ← Services
      </Link>
      <div className="mt-4 flex items-center gap-3">
        <span className="text-3xl">{icon.emoji}</span>
        <div>
          <h1 className="text-3xl font-bold text-white">Book {service?.name}</h1>
          <p className="mt-1 text-sm text-slate-400">{service?.description}</p>
        </div>
      </div>
      <motion.form
        onSubmit={handleSubmit}
        className="mt-10 space-y-5 rounded-3xl border border-white/10 bg-white/[0.03] p-8"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
      >
        {error && (
          <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
            {error}
          </div>
        )}

        <label className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Urgency</span>
          <select
            value={urgency}
            onChange={(e) => setUrgency(e.target.value)}
            className="mt-2 w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
          >
            {URGENCIES.map((u) => (
              <option key={u.value} value={u.value}>
                {u.label}
              </option>
            ))}
          </select>
        </label>

        {quote && (
          <div className="rounded-xl border border-zivro-green/30 bg-zivro-green/10 px-4 py-3 text-sm text-zivro-green">
            Estimated price: <strong>{formatInr(quote.quotedPrice)}</strong> ({quote.currency})
            {aiAnalysis?.estimatedMinutes && (
              <span className="mt-1 block text-xs text-zivro-green/80">
                AI time estimate: ~{aiAnalysis.estimatedMinutes} minutes
              </span>
            )}
          </div>
        )}

        <div className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
            Reference photo (required)
          </span>
          <p className="mt-1 text-xs text-slate-500">
            Upload or capture — AI detects room, utensils, appliances and estimates quantity & time.
          </p>
          <div className="mt-3 flex flex-wrap gap-2">
            <label className="cursor-pointer rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-white hover:bg-white/10">
              Upload photo
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                capture="environment"
                className="hidden"
                onChange={(e) => handleFileChange(e.target.files?.[0] ?? null)}
              />
            </label>
            <label className="cursor-pointer rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-white hover:bg-white/10">
              Use camera
              <input
                ref={cameraRef}
                type="file"
                accept="image/*"
                capture="environment"
                className="hidden"
                onChange={(e) => handleFileChange(e.target.files?.[0] ?? null)}
              />
            </label>
          </div>
          {previewUrl && (
            <img src={previewUrl} alt="" className="mt-3 h-40 w-full rounded-xl border border-white/10 object-cover" />
          )}
          <AiAnalysisCard analysis={aiAnalysis} loading={aiLoading} error={aiError} />
        </div>

        <div className="block">
          <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Service location</span>
          <p className="mt-1 text-xs text-slate-500">Workers see this before accepting your job.</p>
          <div className="mt-3">
            <LocationPicker value={location} onChange={setLocation} error={locationError} />
          </div>
        </div>

        <div className="block">
          <div className="flex items-center justify-between gap-3">
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Schedule (optional)</span>
              <p className="mt-1 text-xs text-slate-500">Pick a date and a preferred 1-hour slot.</p>
            </div>
            <button
              type="button"
              onClick={() => setScheduleOpen(true)}
              className="rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-white hover:bg-white/10"
            >
              Choose
            </button>
          </div>
          <div className="mt-3 rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-sm text-slate-300">
            {scheduleDate && timeSlot ? (
              <span>
                Scheduled: <span className="text-white">{scheduleDate}</span> ·{' '}
                <span className="text-white">{TIME_SLOTS.find((s) => s.value === timeSlot)?.label}</span>
              </span>
            ) : (
              <span className="text-slate-500">No schedule selected.</span>
            )}
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green py-3.5 text-sm font-semibold text-zivro-ink disabled:opacity-60"
        >
          {user ? (submitting ? 'Booking…' : 'Confirm booking') : 'Sign in to book'}
        </button>
      </motion.form>

      {scheduleOpen && (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/60 p-4 sm:items-center">
          <div className="w-full max-w-lg rounded-3xl border border-white/10 bg-zivro-ink p-6 shadow-2xl">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-lg font-semibold text-white">Choose schedule</h2>
                <p className="mt-1 text-sm text-slate-500">Select a date and a 1-hour time slot.</p>
              </div>
              <button
                type="button"
                onClick={() => setScheduleOpen(false)}
                className="rounded-xl px-3 py-2 text-sm text-slate-400 hover:bg-white/5 hover:text-white"
              >
                Close
              </button>
            </div>
            <label className="mt-5 block">
              <span className="text-xs font-medium uppercase tracking-wide text-slate-500">Date</span>
              <input
                type="date"
                value={scheduleDate}
                onChange={(e) => setScheduleDate(e.target.value)}
                className="mt-2 w-full rounded-xl border border-white/10 bg-black/20 px-4 py-3 text-white outline-none ring-zivro-blue/40 focus:ring-2"
              />
            </label>
            <div className="mt-5">
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">Time slot</p>
              <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2">
                {TIME_SLOTS.map((s) => (
                  <button
                    key={s.value}
                    type="button"
                    onClick={() => setTimeSlot(s.value)}
                    className={`rounded-2xl border px-4 py-3 text-left text-sm transition ${
                      timeSlot === s.value
                        ? 'border-zivro-blue/60 bg-zivro-blue/10 text-white'
                        : 'border-white/10 bg-white/[0.03] text-slate-300 hover:bg-white/5'
                    }`}
                  >
                    {s.label}
                  </button>
                ))}
              </div>
            </div>
            <div className="mt-6 flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => {
                  setScheduleDate('')
                  setTimeSlot('')
                  setScheduleOpen(false)
                }}
                className="rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-white/10"
              >
                Clear
              </button>
              <button
                type="button"
                onClick={() => setScheduleOpen(false)}
                className="ml-auto rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green px-5 py-2 text-sm font-semibold text-zivro-ink"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
