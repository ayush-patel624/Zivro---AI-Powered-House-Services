import { useCallback, useEffect, useState } from 'react'
import api from '../api/client'

export default function AdminWorkers() {
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const load = useCallback(() => {
    setError('')
    api
      .get('/api/admin/workers')
      .then((res) => setRows(res.data))
      .catch(() => setError('Could not load workers.'))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function setVerified(id, verified) {
    setBusyId(id)
    try {
      await api.patch(`/api/admin/workers/${id}/verification`, { verified })
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
      <div className="overflow-x-auto rounded-2xl border border-white/10">
        <table className="w-full min-w-[520px] text-left text-sm">
          <thead className="border-b border-white/10 bg-white/[0.04] text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">ID</th>
              <th className="px-4 py-3">Employee</th>
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Verified</th>
              <th className="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((w) => (
              <tr key={w.id} className="border-b border-white/5 text-slate-200">
                <td className="px-4 py-3 font-mono text-xs">{w.id}</td>
                <td className="px-4 py-3">{w.employeeId}</td>
                <td className="px-4 py-3">
                  <div className="font-medium text-white">{w.userName}</div>
                  <div className="text-xs text-slate-500">{w.userEmail}</div>
                </td>
                <td className="px-4 py-3">{w.verified ? 'Yes' : 'No'}</td>
                <td className="px-4 py-3">
                  <button
                    type="button"
                    disabled={busyId === w.id}
                    onClick={() => setVerified(w.id, !w.verified)}
                    className="rounded-lg border border-white/15 px-3 py-1.5 text-xs font-medium text-zivro-blue hover:bg-white/5 disabled:opacity-50"
                  >
                    {w.verified ? 'Revoke' : 'Verify'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
