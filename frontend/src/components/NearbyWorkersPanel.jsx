import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import api from '../api/client'

export default function NearbyWorkersPanel({ bookingId, active, onAssigned }) {
  const [workers, setWorkers] = useState([])
  const [pulse, setPulse] = useState(0)

  useEffect(() => {
    if (!active || !bookingId) return undefined
    let cancelled = false

    async function poll() {
      try {
        const { data } = await api.get(`/api/bookings/${bookingId}/nearby-workers`)
        if (!cancelled) {
          setWorkers(data)
          setPulse((p) => p + 1)
        }
      } catch {
        /* ignore transient errors while searching */
      }
    }

    poll()
    const id = setInterval(poll, 3500)
    return () => {
      cancelled = true
      clearInterval(id)
    }
  }, [active, bookingId])

  useEffect(() => {
    if (!active) {
      setWorkers([])
    }
  }, [active])

  if (!active) return null

  return (
    <div className="mt-4 rounded-2xl border border-zivro-blue/20 bg-zivro-blue/5 p-4">
      <div className="flex items-center gap-2">
        <span className="relative flex h-2.5 w-2.5">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-zivro-blue opacity-60" />
          <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-zivro-blue" />
        </span>
        <p className="text-sm font-semibold text-white">Finding nearby workers…</p>
      </div>
      <p className="mt-1 text-xs text-slate-400">
        Verified pros near your location — first to accept gets your job.
      </p>
      <ul className="mt-4 space-y-2">
        <AnimatePresence mode="popLayout">
          {workers.map((w, i) => (
            <motion.li
              key={`${w.workerId}-${pulse}-${i}`}
              layout
              initial={{ opacity: 0, x: -8 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 8 }}
              transition={{ duration: 0.25 }}
              className="flex items-center justify-between gap-3 rounded-xl border border-white/10 bg-black/20 px-3 py-2.5"
            >
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-zivro-blue/30 to-zivro-green/20 text-sm font-bold text-white">
                  {w.name?.charAt(0) || 'Z'}
                </div>
                <div>
                  <p className="text-sm font-medium text-white">{w.name}</p>
                  <p className="text-xs text-slate-500">
                    {w.category} · {w.distanceKm} km · ETA {w.etaMinutes} min
                  </p>
                </div>
              </div>
              <span className="rounded-full bg-amber-500/15 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wide text-amber-200">
                Nearby
              </span>
            </motion.li>
          ))}
        </AnimatePresence>
      </ul>
      {workers.length === 0 && (
        <p className="mt-3 text-xs text-slate-500">Scanning your area for available workers…</p>
      )}
      <button
        type="button"
        onClick={onAssigned}
        className="mt-3 text-xs text-slate-500 underline-offset-2 hover:text-slate-300 hover:underline"
      >
        Refresh status
      </button>
    </div>
  )
}
