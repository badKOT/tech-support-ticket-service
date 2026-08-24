import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'

import { useAuth } from './auth/AuthContext'

import AdminPage from './pages/AdminPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import ProjectPage from './pages/ProjectPage'
import TicketPage from './pages/TicketPage'
import ProjectAnalyticsPage from './pages/ProjectAnalyticsPage'
import OidcCallbackPage from './pages/OidcCallbackPage'

function ProtectedRoute({ children }) {
  const { currentUser, loading } = useAuth()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!currentUser) {
    return (
        <Navigate
            to="/login"
            replace
        />
    )
  }

  return children
}

function AdminRoute({ children }) {
  const { currentUser, loading } = useAuth()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!currentUser) {
    return (
        <Navigate
            to="/login"
            replace
        />
    )
  }

  if (currentUser.role !== 'ADMIN') {
    return (
        <Navigate
            to="/"
            replace
        />
    )
  }

  return children
}

function AnalyticsRoute({ children }) {
  const { currentUser, loading } = useAuth()

  if (loading) {
    return <div>Loading...</div>
  }

  if (!currentUser) {
    return (
        <Navigate
            to="/login"
            replace
        />
    )
  }

  const hasAccess =
      currentUser.role === 'TEAM_LEAD' ||
      currentUser.role === 'ADMIN'

  if (!hasAccess) {
    return (
        <Navigate
            to="/"
            replace
        />
    )
  }

  return children
}

export default function App() {
  return (
      <Routes>
        <Route
            path="/login"
            element={<LoginPage />}
        />

        <Route
            path="/oidc/callback"
            element={<OidcCallbackPage />}
        />

        <Route
            path="/"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
        />

        <Route
            path="/projects/:projectId"
            element={
              <ProtectedRoute>
                <ProjectPage />
              </ProtectedRoute>
            }
        />

        <Route
            path="/projects/:projectId/analytics"
            element={
              <AnalyticsRoute>
                <ProjectAnalyticsPage />
              </AnalyticsRoute>
            }
        />

        <Route
            path="/tickets/:ticketId"
            element={
              <ProtectedRoute>
                <TicketPage />
              </ProtectedRoute>
            }
        />

        <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminPage />
              </AdminRoute>
            }
        />

        <Route
            path="*"
            element={
              <Navigate
                  to="/"
                  replace
              />
            }
        />
      </Routes>
  )
}