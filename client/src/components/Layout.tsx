import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { DemoBanner } from './DemoBanner'

export function Layout() {
  const { email, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">◎</span> ClearWatch
        </div>
        <nav>
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Services
          </NavLink>
          <NavLink to="/incidents" className={({ isActive }) => (isActive ? 'active' : '')}>
            Incidents
          </NavLink>
          <NavLink to="/logs" className={({ isActive }) => (isActive ? 'active' : '')}>
            Logs
          </NavLink>
          <NavLink to="/alert-rules" className={({ isActive }) => (isActive ? 'active' : '')}>
            Alert Rules
          </NavLink>
        </nav>
        <div className="topbar-right">
          <span className="user-email">{email}</span>
          <button className="btn-ghost" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>
      <DemoBanner />
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
