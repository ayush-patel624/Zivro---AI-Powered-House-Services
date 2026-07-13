import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import api from '../api/client'
import { formatInr } from '../lib/format'
import { getServiceIcon } from '../lib/serviceIcons'

export default function Services() {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('All')

  useEffect(() => {
    api
      .get('/api/services')
      .then((res) => setItems(res.data))
      .catch(() => setError('Could not load services. Is the API running?'))
      .finally(() => setLoading(false))
  }, [])

  const categories = useMemo(() => {
    const set = new Set(items.map((s) => s.category).filter(Boolean))
    return ['All', ...Array.from(set)]
  }, [items])

  const visible = filter === 'All' ? items : items.filter((s) => s.category === filter)

  return (
    <div className="mx-auto max-w-6xl px-4 py-16">
      <h1 className="text-3xl font-bold text-white sm:text-4xl">Service catalog</h1>
      <p className="mt-2 max-w-2xl text-slate-400">
        Pick a service, upload a photo for AI sizing, share your location, and get matched with nearby workers.
      </p>

      <div className="mt-8 flex flex-wrap gap-2">
        {categories.map((cat) => (
          <button
            key={cat}
            type="button"
            onClick={() => setFilter(cat)}
            className={`rounded-full px-4 py-2 text-sm font-medium ${
              filter === cat
                ? 'bg-zivro-blue/20 text-zivro-blue'
                : 'border border-white/10 text-slate-400 hover:bg-white/5'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {loading && <p className="mt-10 text-slate-500">Loading…</p>}
      {error && (
        <p className="mt-10 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {error}
        </p>
      )}
      <ul className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {visible.map((s, i) => {
          const icon = getServiceIcon(s.iconKey)
          return (
            <motion.li
              key={s.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.03 }}
              className="flex flex-col rounded-3xl border border-white/10 bg-white/[0.03] p-5 backdrop-blur"
            >
              <div className="flex items-start gap-3">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-zivro-blue/20 to-zivro-green/10 text-2xl">
                  {icon.emoji}
                </div>
                <div className="min-w-0">
                  <p className="text-[10px] font-semibold uppercase tracking-wide text-zivro-blue">
                    {s.category || icon.label}
                  </p>
                  <h2 className="text-base font-semibold text-white">{s.name}</h2>
                </div>
              </div>
              <p className="mt-3 flex-1 text-sm text-slate-400 line-clamp-3">{s.description}</p>
              <p className="mt-4 text-sm text-slate-500">
                From <span className="font-semibold text-zivro-green">{formatInr(s.basePrice)}</span>
              </p>
              <Link
                to={`/book/${s.id}`}
                className="mt-5 inline-flex justify-center rounded-2xl bg-gradient-to-r from-zivro-blue to-zivro-green py-3 text-sm font-semibold text-zivro-ink"
              >
                Book
              </Link>
            </motion.li>
          )
        })}
      </ul>
    </div>
  )
}
