import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './hooks/useAuth'
import { Layout } from './components/Layout'
import { Login } from './pages/Login'
import { ServiceOverview } from './pages/ServiceOverview'
import { ServiceDetail } from './pages/ServiceDetail'
import { Incidents } from './pages/Incidents'
import { IncidentDetail } from './pages/IncidentDetail'
import { Logs } from './pages/Logs'
import { AlertRules } from './pages/AlertRules'
import type { ReactNode } from 'react'

function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <>{children}</>
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/" element={<ServiceOverview />} />
        <Route path="/services/:id" element={<ServiceDetail />} />
        <Route path="/incidents" element={<Incidents />} />
        <Route path="/incidents/:id" element={<IncidentDetail />} />
        <Route path="/logs" element={<Logs />} />
        <Route path="/alert-rules" element={<AlertRules />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}
