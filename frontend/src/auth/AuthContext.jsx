import {
  createContext,
  useContext,
  useEffect,
  useState,
} from 'react'

import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  setUnauthorizedHandler,
} from '../api/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] =
      useState(null)

  const [loading, setLoading] =
      useState(true)

  // =========================================================
  // GLOBAL 401 HANDLER
  // =========================================================

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setCurrentUser(null)
    })

    return () => {
      setUnauthorizedHandler(null)
    }
  }, [])

  // =========================================================
  // LOAD CURRENT USER
  // =========================================================

  useEffect(() => {
    getCurrentUser()
    .then((user) => {
      setCurrentUser(user)
    })
    .catch((error) => {
      if (error.status === 401) {
        setCurrentUser(null)
      } else {
        console.error(
            'Failed to load current user',
            error,
        )
      }
    })
    .finally(() => {
      setLoading(false)
    })
  }, [])

  // =========================================================
  // LOGIN
  // =========================================================

  async function login(username, password) {
    const user = await loginRequest(
        username,
        password,
    )

    setCurrentUser(user)

    return user
  }

  // =========================================================
  // LOGOUT
  // =========================================================

  async function logout() {
    try {
      await logoutRequest()
    } finally {
      /*
       * Даже если серверная сессия уже истекла
       * или logout вернул ошибку,
       * локальную авторизацию очищаем.
       */
      setCurrentUser(null)
    }
  }

  // =========================================================
  // PROVIDER
  // =========================================================

  return (
      <AuthContext.Provider
          value={{
            currentUser,
            loading,
            login,
            logout,
          }}
      >
        {children}
      </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error(
        'useAuth must be used inside AuthProvider',
    )
  }

  return context
}