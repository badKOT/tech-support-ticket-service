import {
  createContext,
  useContext,
  useEffect,
  useState,
} from 'react'

import {
  clearTokens,
  getCurrentUser,
  hasAccessToken,
  hasRefreshToken,
  login as loginRequest,
  logout as logoutRequest,
  setTokens,
  setUnauthorizedHandler,
} from '../api/api'

const AuthContext =
    createContext(null)

export function AuthProvider({
  children,
}) {
  const [
    currentUser,
    setCurrentUser,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)

  // =========================================================
  // GLOBAL 401 HANDLER
  // =========================================================

  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearTokens()
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
    /*
     * Если нет вообще никаких токенов,
     * даже не вызываем /me.
     */
    if (
        !hasAccessToken() &&
        !hasRefreshToken()
    ) {
      setCurrentUser(null)
      setLoading(false)

      return
    }

    /*
     * Если access token истёк,
     * request() сам сделает refresh,
     * повторит /me и вернёт пользователя.
     */
    getCurrentUser()
    .then((user) => {
      setCurrentUser(user)
    })
    .catch((error) => {
      if (error.status !== 401) {
        console.error(
            'Failed to load current user',
            error,
        )
      }

      clearTokens()
      setCurrentUser(null)
    })
    .finally(() => {
      setLoading(false)
    })
  }, [])

  // =========================================================
  // LOGIN
  // =========================================================

  async function login(
      username,
      password,
  ) {
    const response =
        await loginRequest(
            username,
            password,
        )

    if (
        !response?.accessToken ||
        !response?.refreshToken
    ) {
      throw new Error(
          'Login response does not contain tokens',
      )
    }

    setTokens(
        response.accessToken,
        response.refreshToken,
    )

    setCurrentUser(
        response.user,
    )

    return response.user
  }

  // =========================================================
  // LOGOUT
  // =========================================================

  async function logout() {
    try {
      await logoutRequest()
    } catch (error) {
      console.error(
          'Logout request failed',
          error,
      )
    } finally {
      /*
       * Даже если backend недоступен,
       * локальную авторизацию всё равно удаляем.
       */
      clearTokens()

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
  const context =
      useContext(AuthContext)

  if (!context) {
    throw new Error(
        'useAuth must be used inside AuthProvider',
    )
  }

  return context
}