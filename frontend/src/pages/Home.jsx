import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'

export default function Home() {
  return (
    <div className="mx-auto max-w-6xl px-4 py-16 sm:py-24">
      <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
        <div>
          <p className="mb-3 text-sm font-medium uppercase tracking-widest text-zivro-blue">
            Home services, reimagined
          </p>
          <h1 className="text-4xl font-bold leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">
            Book trusted pros with{' '}
            <span className="bg-gradient-to-r from-zivro-blue to-zivro-green bg-clip-text text-transparent">
              transparent
            </span>{' '}
            pricing.
          </h1>
          <p className="mt-6 max-w-xl text-lg text-slate-400">
            Zivro makes it effortless to schedule home services with verified professionals, clear pricing, and
            real-time booking updates.
          </p>
          <div className="mt-10 flex flex-wrap gap-4">
            <Link
              to="/services"
              className="inline-flex items-center justify-center rounded-2xl bg-gradient-to-r from-zivro-blue to-zivro-green px-8 py-3.5 text-base font-semibold text-zivro-ink shadow-lg shadow-zivro-blue/20 transition hover:opacity-95"
            >
              Browse services
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center justify-center rounded-2xl border border-white/15 bg-white/5 px-8 py-3.5 text-base font-semibold text-white backdrop-blur transition hover:bg-white/10"
            >
              Create account
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center justify-center rounded-2xl border border-white/15 bg-white/5 px-8 py-3.5 text-base font-semibold text-white backdrop-blur transition hover:bg-white/10"
            >
              Sign in
            </Link>
          </div>
        </div>
        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="relative"
        >
          <div className="absolute -inset-4 rounded-[2rem] bg-gradient-to-br from-zivro-blue/30 to-zivro-green/20 blur-3xl" />
          <div className="relative overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-slate-900 to-slate-950 p-8 shadow-2xl">
            <div className="flex items-center justify-between border-b border-white/10 pb-4">
              <span className="text-sm font-medium text-slate-300">Next booking</span>
              <span className="rounded-full bg-zivro-green/20 px-3 py-1 text-xs font-semibold text-zivro-green">
                PENDING
              </span>
            </div>
            <div className="mt-6 space-y-4 text-sm text-slate-400">
              <div className="flex justify-between">
                <span>Deep clean · 2 BR</span>
                <span className="font-semibold text-white">₹1,840</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-white/10">
                <motion.div
                  className="h-full rounded-full bg-gradient-to-r from-zivro-blue to-zivro-green"
                  initial={{ width: 0 }}
                  animate={{ width: '62%' }}
                  transition={{ duration: 1.2, delay: 0.4 }}
                />
              </div>
              <p className="text-xs text-slate-500">
                Clean, fast bookings — from request to doorstep.
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
