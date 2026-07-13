import { Link, NavLink, Outlet } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../auth/AuthContext'
import InstallPwaBanner from '../components/InstallPwaBanner'

const linkClass = ({ isActive }) =>
  `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-white/10 text-white' : 'text-slate-400 hover:text-white'
  }`

export default function RootLayout() {
  const { user, logout } = useAuth()

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-white/10 bg-zivro-ink/80 backdrop-blur-md sticky top-0 z-20">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4">
          <Link to="/" className="flex items-center gap-2">
            <img src="/favicon.svg" alt="" className="h-9 w-9 rounded-xl" />
            <span className="bg-gradient-to-r from-zivro-blue to-zivro-green bg-clip-text text-xl font-semibold text-transparent">
              Zivro
            </span>
          </Link>
          <nav className="flex flex-wrap items-center justify-end gap-1 sm:gap-2">
            <NavLink to="/" className={linkClass} end>
              Home
            </NavLink>
            <NavLink to="/services" className={linkClass}>
              Services
            </NavLink>
            {user && (
              <>
                <NavLink to="/bookings" className={linkClass}>
                  My bookings
                </NavLink>
                {user.role === 'WORKER' && (
                  <NavLink to="/worker/jobs" className={linkClass}>
                    Jobs
                  </NavLink>
                )}
                {user.role === 'ADMIN' && (
                  <NavLink to="/admin/dashboard" className={linkClass}>
                    Admin
                  </NavLink>
                )}
              </>
            )}
            {user ? (
              <>
                <NavLink to="/dashboard" className={linkClass}>
                  Account
                </NavLink>
                <button
                  type="button"
                  onClick={logout}
                  className="rounded-lg px-3 py-2 text-sm font-medium text-slate-400 hover:bg-white/5 hover:text-white"
                >
                  Sign out
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={linkClass}>
                  Sign in
                </NavLink>
                <NavLink
                  to="/register"
                  className="rounded-lg bg-gradient-to-r from-zivro-blue to-zivro-green px-4 py-2 text-sm font-semibold text-zivro-ink"
                >
                  Get started
                </NavLink>
              </>
            )}
          </nav>
        </div>
      </header>
      <motion.main
        className="flex-1"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
      >
        <Outlet />
      </motion.main>
      <footer className="border-t border-white/10 py-8 text-center text-xs text-slate-500">
        Zivro — your home services, scheduled in minutes.
      </footer>
      <InstallPwaBanner />
    </div>
  )
}
