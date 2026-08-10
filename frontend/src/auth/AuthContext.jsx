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
} from '../api/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)

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

  async function login(username, password) {
    const user = await loginRequest(
        username,
        password,
    )

    setCurrentUser(user)

    return user
  }

  async function logout() {
    await logoutRequest()
    setCurrentUser(null)
  }

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