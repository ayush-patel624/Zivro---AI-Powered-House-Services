import { useCallback, useEffect, useState } from 'react'

const EMPTY = { address: '', label: 'Current location', latitude: null, longitude: null }

export default function LocationPicker({ value, onChange, error }) {
  const [mode, setMode] = useState('current')
  const [loading, setLoading] = useState(false)
  const [manualAddress, setManualAddress] = useState(value?.address || '')
  const [locError, setLocError] = useState('')

  const apply = useCallback(
    (next) => {
      onChange(next)
    },
    [onChange],
  )

  const detectCurrent = useCallback(() => {
    if (!navigator.geolocation) {
      setLocError('Geolocation is not supported on this device.')
      return
    }
    setLoading(true)
    setLocError('')
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords
        apply({
          address: `Current location (${latitude.toFixed(5)}, ${longitude.toFixed(5)})`,
          label: 'Current location',
          latitude,
          longitude,
        })
        setLoading(false)
      },
      () => {
        setLocError('Could not access your location. Switch to “Other location” and enter your address.')
        setLoading(false)
      },
      { enableHighAccuracy: true, timeout: 12000 },
    )
  }, [apply])

  useEffect(() => {
    if (mode === 'current' && value?.latitude == null && !loading) {
      detectCurrent()
    }
  }, [mode, value?.latitude, loading, detectCurrent])

  function useManualLocation() {
    if (!manualAddress.trim()) {
      setLocError('Enter an address or landmark.')
      return
    }
    setLocError('')
    const jitter = (Math.random() - 0.5) * 0.02
    apply({
      address: manualAddress.trim(),
      label: 'Other location',
      latitude: 12.9716 + jitter,
      longitude: 77.5946 + jitter,
    })
  }

  return (
    <div className="space-y-3 rounded-2xl border border-white/10 bg-black/20 p-4">
      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => {
            setMode('current')
            detectCurrent()
          }}
          className={`rounded-xl px-4 py-2 text-sm font-medium ${
            mode === 'current'
              ? 'bg-zivro-blue/20 text-zivro-blue'
              : 'border border-white/10 text-slate-300 hover:bg-white/5'
          }`}
        >
          📍 Current location
        </button>
        <button
          type="button"
          onClick={() => {
            setMode('other')
            apply(EMPTY)
          }}
          className={`rounded-xl px-4 py-2 text-sm font-medium ${
            mode === 'other'
              ? 'bg-zivro-blue/20 text-zivro-blue'
              : 'border border-white/10 text-slate-300 hover:bg-white/5'
          }`}
        >
          🗺️ Other location
        </button>
      </div>

      {mode === 'current' && (
        <div className="text-sm text-slate-300">
          {loading && <p className="text-slate-500">Detecting your location…</p>}
          {!loading && value?.latitude != null && (
            <p>
              <span className="font-medium text-white">{value.label}</span>
              <span className="mt-1 block text-xs text-slate-500">{value.address}</span>
            </p>
          )}
          {!loading && value?.latitude == null && (
            <button
              type="button"
              onClick={detectCurrent}
              className="rounded-lg border border-white/10 px-3 py-1.5 text-xs text-zivro-blue hover:bg-white/5"
            >
              Retry location
            </button>
          )}
        </div>
      )}

      {mode === 'other' && (
        <div className="space-y-2">
          <input
            type="text"
            value={manualAddress}
            onChange={(e) => setManualAddress(e.target.value)}
            placeholder="Flat 402, MG Road, Bengaluru…"
            className="w-full rounded-xl border border-white/10 bg-zivro-ink px-4 py-3 text-sm text-white outline-none ring-zivro-blue/40 focus:ring-2"
          />
          <button
            type="button"
            onClick={useManualLocation}
            className="rounded-xl border border-white/10 bg-white/5 px-4 py-2 text-sm font-medium text-white hover:bg-white/10"
          >
            Use this address
          </button>
        </div>
      )}

      {(locError || error) && <p className="text-xs text-red-300">{locError || error}</p>}
    </div>
  )
}
