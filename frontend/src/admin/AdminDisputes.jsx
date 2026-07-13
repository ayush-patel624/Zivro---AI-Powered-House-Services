import { useCallback, useEffect, useState } from 'react'
import api from '../api/client'

const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED']

export default function AdminDisputes() {
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const load = useCallback(() => {
    setError('')
    api
      .get('/api/admin/disputes')
      .then((res) => setRows(res.data))
      .catch(() => setError('Could not load disputes.'))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function patchDispute(id, status, resolutionNotes) {
    setBusyId(id)
    setError('')
    try {
      await api.patch(`/api/admin/disputes/${id}`, { status, resolutionNotes: resolutionNotes || undefined })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Update failed.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-4">
      {error && <p className="text-sm text-red-300">{error}</p>}
      <ul className="space-y-3">
        {rows.map((d) => (
          <li key={d.id} className="rounded-2xl border border-white/10 bg-white/[0.03] p-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-sm text-slate-500">
                  #{d.id} · booking {d.bookingId}
                </p>
                <p className="mt-1 font-medium text-white">{d.status}</p>
                <p className="mt-2 text-sm text-slate-300">{d.reason}</p>
                {d.resolutionNotes && (
                  <p className="mt-2 text-xs text-slate-500">Resolution: {d.resolutionNotes}</p>
                )}
              </div>
              <DisputeActions dispute={d} busy={busyId === d.id} onSave={patchDispute} />
            </div>
          </li>
        ))}
      </ul>
      {rows.length === 0 && !error && <p className="text-slate-500">No disputes.</p>}
    </div>
  )
}

function DisputeActions({ dispute, busy, onSave }) {
  const [status, setStatus] = useState(dispute.status)
  const [notes, setNotes] = useState(dispute.resolutionNotes || '')

  return (
    <div className="min-w-[200px] space-y-2">
      <select
        value={status}
        onChange={(e) => setStatus(e.target.value)}
        className="w-full rounded-lg border border-white/10 bg-zivro-ink px-2 py-2 text-sm text-white"
      >
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
      <textarea
        placeholder="Resolution notes"
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        rows={2}
        className="w-full rounded-lg border border-white/10 bg-zivro-ink px-2 py-2 text-xs text-white"
      />
      <button
        type="button"
        disabled={busy}
        onClick={() => onSave(dispute.id, status, notes)}
        className="w-full rounded-lg bg-zivro-blue/20 py-2 text-sm font-medium text-zivro-blue disabled:opacity-50"
      >
        {busy ? 'Saving…' : 'Save'}
      </button>
    </div>
  )
}
