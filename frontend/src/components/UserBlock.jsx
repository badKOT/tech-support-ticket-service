import { useNavigate } from 'react-router-dom'

import { useAuth } from '../auth/AuthContext'

export default function UserBlock() {
  const {
    currentUser,
    logout,
  } = useAuth()

  const navigate = useNavigate()

  if (!currentUser) {
    return null
  }

  async function handleLogout() {
    try {
      await logout()
      navigate('/login')
    } catch (error) {
      console.error('Logout failed', error)
    }
  }

  return (
      <div className="user-block">
        <div className="user-details">
          <strong>
            {currentUser.displayName}
          </strong>

          <span>
          {currentUser.role}
        </span>
        </div>

        <button
            type="button"
            onClick={handleLogout}
        >
          Выйти
        </button>
      </div>
  )
}