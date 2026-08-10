import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/AuthContext'

export default function LoginPage() {
  const {
    currentUser,
    loading,
    login,
  } = useAuth()

  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (loading) {
    return <div>Loading...</div>
  }

  if (currentUser) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()

    setError('')
    setSubmitting(true)

    try {
      await login(username, password)
      navigate('/')
    } catch (error) {
      if (error.status === 401) {
        setError('Неверный логин или пароль')
      } else {
        setError('Не удалось выполнить вход')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
      <main className="login-page">
        <form
            className="login-card"
            onSubmit={handleSubmit}
        >
          <h1>Tech Support</h1>

          <p>Войдите в систему</p>

          <label>
            Логин

            <input
                type="text"
                value={username}
                onChange={(event) =>
                    setUsername(event.target.value)
                }
                autoComplete="username"
                required
            />
          </label>

          <label>
            Пароль

            <input
                type="password"
                value={password}
                onChange={(event) =>
                    setPassword(event.target.value)
                }
                autoComplete="current-password"
                required
            />
          </label>

          {error && (
              <div className="login-error">
                {error}
              </div>
          )}

          <button
              type="submit"
              disabled={submitting}
          >
            {submitting ? 'Входим...' : 'Войти'}
          </button>
        </form>
      </main>
  )
}