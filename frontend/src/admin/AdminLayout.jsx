import { NavLink, Outlet } from 'react-router-dom'

const tab = ({ isActive }) =>
  `rounded-lg px-3 py-2 text-sm font-medium ${
    isActive ? 'bg-white/10 text-white' : 'text-slate-400 hover:text-white'
  }`

export default function AdminLayout() {
  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <h1 className="text-2xl font-bold text-white">Admin</h1>
      <p className="mt-1 text-sm text-slate-500">Operations, disputes, and platform analytics.</p>
      <nav className="mt-6 flex flex-wrap gap-2 border-b border-white/10 pb-4">
        <NavLink to="/admin/dashboard" className={tab}>
          Dashboard
        </NavLink>
        <NavLink to="/admin/disputes" className={tab}>
          Disputes
        </NavLink>
        <NavLink to="/admin/workers" className={tab}>
          Workers
        </NavLink>
      </nav>
      <div className="mt-8">
        <Outlet />
      </div>
    </div>
  )
}
