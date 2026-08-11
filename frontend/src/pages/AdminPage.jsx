import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  createAdminUser,
  deleteAdminUser,
  getAdminUsers,
  updateAdminUser,
} from '../api/api'

import UserBlock from '../components/UserBlock'

const USER_ROLES = [
  'REQUESTER',
  'SUPPORT_AGENT',
  'TEAM_LEAD',
  'ADMIN',
]

export default function AdminPage() {
  const navigate = useNavigate()

  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // CREATE
  const [creating, setCreating] = useState(false)
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createError, setCreateError] = useState('')

  const [username, setUsername] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('REQUESTER')

  // EDIT
  const [editingUserId, setEditingUserId] = useState(null)
  const [editUsername, setEditUsername] = useState('')
  const [editDisplayName, setEditDisplayName] = useState('')
  const [editEmail, setEditEmail] = useState('')
  const [editPassword, setEditPassword] = useState('')
  const [editRole, setEditRole] = useState('REQUESTER')
  const [editEnabled, setEditEnabled] = useState(true)

  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')
  const [deletingUserId, setDeletingUserId] = useState(null)
  const [deleteError, setDeleteError] = useState('')
  const [showDisabledUsers, setShowDisabledUsers] = useState(false)

  useEffect(() => {
    loadUsers()
  }, [])

  async function loadUsers() {
    setLoading(true)
    setError('')

    try {
      const data = await getAdminUsers()
      setUsers(data)
    } catch (error) {
      console.error('Failed to load users', error)

      if (error.status === 403) {
        setError(
            'У вас нет прав на просмотр пользователей',
        )
      } else {
        setError(
            'Не удалось загрузить пользователей',
        )
      }
    } finally {
      setLoading(false)
    }
  }

  // =========================================================
  // CREATE
  // =========================================================

  function handleStartCreate() {
    setEditingUserId(null)

    setUsername('')
    setDisplayName('')
    setEmail('')
    setPassword('')
    setRole('REQUESTER')

    setCreateError('')
    setCreating(true)
  }

  function handleCancelCreate() {
    setCreating(false)
    setCreateError('')
  }

  async function handleCreateUser(event) {
    event.preventDefault()

    const trimmedUsername = username.trim()
    const trimmedDisplayName = displayName.trim()
    const trimmedEmail = email.trim()

    if (!trimmedUsername) {
      setCreateError('Введите username')
      return
    }

    if (!trimmedDisplayName) {
      setCreateError('Введите имя пользователя')
      return
    }

    if (!password) {
      setCreateError('Введите пароль')
      return
    }

    if (password.length < 8) {
      setCreateError(
          'Пароль должен содержать минимум 8 символов',
      )
      return
    }

    setCreateSubmitting(true)
    setCreateError('')

    try {
      const newUser = await createAdminUser({
        username: trimmedUsername,
        displayName: trimmedDisplayName,
        email: trimmedEmail || null,
        password,
        role,
      })

      setUsers((currentUsers) => [
        ...currentUsers,
        newUser,
      ])

      setCreating(false)

      setUsername('')
      setDisplayName('')
      setEmail('')
      setPassword('')
      setRole('REQUESTER')
    } catch (error) {
      console.error(
          'Failed to create user',
          error,
      )

      if (error.status === 409) {
        setCreateError(
            'Пользователь с таким username уже существует',
        )
      } else if (error.status === 400) {
        setCreateError(
            'Проверьте введённые данные',
        )
      } else if (error.status === 403) {
        setCreateError(
            'У вас нет прав на создание пользователей',
        )
      } else {
        setCreateError(
            'Не удалось создать пользователя',
        )
      }
    } finally {
      setCreateSubmitting(false)
    }
  }

  // =========================================================
  // EDIT
  // =========================================================

  function handleStartEdit(user) {
    setCreating(false)

    setEditingUserId(user.id)

    setEditUsername(user.username ?? '')
    setEditDisplayName(user.displayName ?? '')
    setEditEmail(user.email ?? '')
    setEditPassword('')
    setEditRole(user.role)
    setEditEnabled(user.enabled)

    setEditError('')
  }

  function handleCancelEdit() {
    setEditingUserId(null)
    setEditPassword('')
    setEditError('')
  }

  async function handleUpdateUser(event, userId) {
    event.preventDefault()

    const trimmedUsername = editUsername.trim()
    const trimmedDisplayName = editDisplayName.trim()
    const trimmedEmail = editEmail.trim()

    if (!trimmedUsername) {
      setEditError('Username не может быть пустым')
      return
    }

    if (!trimmedDisplayName) {
      setEditError('Имя не может быть пустым')
      return
    }

    if (
        editPassword &&
        editPassword.length < 8
    ) {
      setEditError(
          'Новый пароль должен содержать минимум 8 символов',
      )
      return
    }

    setEditSubmitting(true)
    setEditError('')

    try {
      const updatedUser = await updateAdminUser(
          userId,
          {
            username: trimmedUsername,
            displayName: trimmedDisplayName,
            email: trimmedEmail || null,
            password: editPassword || null,
            role: editRole,
            enabled: editEnabled,
          },
      )

      setUsers((currentUsers) =>
          currentUsers.map((user) =>
              user.id === updatedUser.id
                  ? updatedUser
                  : user,
          ),
      )

      setEditingUserId(null)
      setEditPassword('')
    } catch (error) {
      console.error(
          'Failed to update user',
          error,
      )

      if (error.status === 400) {
        setEditError(
            'Проверьте введённые данные. Возможно, username уже занят.',
        )
      } else if (error.status === 403) {
        setEditError(
            'У вас нет прав на изменение пользователя',
        )
      } else if (error.status === 404) {
        setEditError(
            'Пользователь не найден',
        )
      } else {
        setEditError(
            'Не удалось сохранить изменения',
        )
      }
    } finally {
      setEditSubmitting(false)
    }
  }

  async function handleDeleteUser(user) {
    const confirmed = window.confirm(
        `Отключить пользователя "${user.displayName}" (@${user.username})?`,
    )

    if (!confirmed) {
      return
    }

    setDeletingUserId(user.id)
    setDeleteError('')

    try {
      await deleteAdminUser(user.id)

      setUsers((currentUsers) =>
          currentUsers.map((currentUser) =>
              currentUser.id === user.id
                  ? {
                    ...currentUser,
                    enabled: false,
                  }
                  : currentUser,
          ),
      )

      if (editingUserId === user.id) {
        setEditingUserId(null)
      }
    } catch (error) {
      console.error(
          'Failed to disable user',
          error,
      )

      if (error.status === 404) {
        setDeleteError(
            'Пользователь не найден',
        )
      } else if (error.status === 403) {
        setDeleteError(
            'У вас нет прав на отключение пользователя',
        )
      } else if (error.status === 409) {
        setDeleteError(
            'Невозможно отключить пользователя',
        )
      } else {
        setDeleteError(
            'Не удалось отключить пользователя',
        )
      }
    } finally {
      setDeletingUserId(null)
    }
  }

  const activeUsers = users.filter((user) => user.enabled)
  const disabledUsers = users.filter((user) => !user.enabled)

  function renderUserCard(user) {
    const isEditing = editingUserId === user.id

    return (
        <div
            className={`admin-user-card ${
                !user.enabled ? 'admin-user-card-disabled' : ''
            }`}
            key={user.id}
        >
          {isEditing ? (
              <form
                  className="admin-user-edit-form"
                  onSubmit={(event) =>
                      handleUpdateUser(event, user.id)
                  }
              >
                <div className="admin-edit-heading">
                  <h3>Редактирование пользователя</h3>
                  <span>ID: {user.id}</span>
                </div>

                <div className="admin-create-grid">
                  <label className="ticket-edit-field">
                    <span>Username</span>
                    <input
                        type="text"
                        value={editUsername}
                        onChange={(event) =>
                            setEditUsername(event.target.value)
                        }
                        disabled={editSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Имя</span>
                    <input
                        type="text"
                        value={editDisplayName}
                        onChange={(event) =>
                            setEditDisplayName(event.target.value)
                        }
                        disabled={editSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Email</span>
                    <input
                        type="email"
                        value={editEmail}
                        onChange={(event) =>
                            setEditEmail(event.target.value)
                        }
                        disabled={editSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Новый пароль</span>
                    <input
                        type="password"
                        value={editPassword}
                        onChange={(event) =>
                            setEditPassword(event.target.value)
                        }
                        placeholder="Оставьте пустым, чтобы не менять"
                        autoComplete="new-password"
                        disabled={editSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Роль</span>
                    <select
                        value={editRole}
                        onChange={(event) =>
                            setEditRole(event.target.value)
                        }
                        disabled={editSubmitting}
                    >
                      {USER_ROLES.map((userRole) => (
                          <option key={userRole} value={userRole}>
                            {userRole}
                          </option>
                      ))}
                    </select>
                  </label>

                  <label className="admin-enabled-field">
                    <input
                        type="checkbox"
                        checked={editEnabled}
                        onChange={(event) =>
                            setEditEnabled(event.target.checked)
                        }
                        disabled={editSubmitting}
                    />
                    <span>Пользователь активен</span>
                  </label>
                </div>

                {editError && (
                    <div className="login-error">{editError}</div>
                )}

                <div className="ticket-edit-actions">
                  <button
                      type="submit"
                      className="ticket-primary-button"
                      disabled={
                          editSubmitting ||
                          !editUsername.trim() ||
                          !editDisplayName.trim()
                      }
                  >
                    {editSubmitting ? 'Сохраняем...' : 'Сохранить'}
                  </button>

                  <button
                      type="button"
                      className="ticket-secondary-button"
                      onClick={handleCancelEdit}
                      disabled={editSubmitting}
                  >
                    Отмена
                  </button>
                </div>
              </form>
          ) : (
              <>
                <div className="admin-user-main">
                  <div className="admin-user-avatar">
                    {user.displayName?.charAt(0).toUpperCase()}
                  </div>

                  <div>
                    <h3>{user.displayName}</h3>
                    <span className="admin-username">
                  @{user.username}
                </span>
                  </div>
                </div>

                <div className="admin-user-info">
              <span className="admin-user-role">
                {user.role}
              </span>

                  <span
                      className={
                        user.enabled
                            ? 'admin-user-enabled'
                            : 'admin-user-disabled'
                      }
                  >
                {user.enabled ? 'Активен' : 'Отключён'}
              </span>

                  <div className="admin-user-actions">
                    <button
                        type="button"
                        className="ticket-secondary-button"
                        onClick={() => handleStartEdit(user)}
                        disabled={deletingUserId === user.id}
                    >
                      Редактировать
                    </button>

                    {user.enabled && (
                        <button
                            type="button"
                            className="admin-delete-button"
                            onClick={() => handleDeleteUser(user)}
                            disabled={deletingUserId === user.id}
                        >
                          {deletingUserId === user.id
                              ? 'Отключаем...'
                              : 'Отключить'}
                        </button>
                    )}
                  </div>
                </div>

                {user.email && (
                    <div className="admin-user-email">
                      {user.email}
                    </div>
                )}
              </>
          )}
        </div>
    )
  }

  return (
      <div className="app-page">
        <header className="app-header">
          <div className="app-brand">
            <div className="app-logo">
              TS
            </div>

            <h1>Tech Support</h1>
          </div>

          <UserBlock />
        </header>

        <main className="page-content">
          <button
              type="button"
              className="back-button"
              onClick={() => navigate('/')}
          >
            ← Назад к проектам
          </button>

          <div className="page-title admin-page-title">
            <div>
              <h2>Пользователи</h2>

              <p>
                Управление пользователями системы
              </p>
            </div>

            {!creating && (
                <button
                    type="button"
                    className="ticket-primary-button"
                    onClick={handleStartCreate}
                >
                  + Создать пользователя
                </button>
            )}
          </div>

          {creating && (
              <form
                  className="admin-create-form content-card"
                  onSubmit={handleCreateUser}
              >
                <div className="ticket-create-heading">
                  <div>
                    <h3>Новый пользователь</h3>

                    <p>
                      Создание учётной записи
                    </p>
                  </div>
                </div>

                <div className="admin-create-grid">
                  <label className="ticket-edit-field">
                    <span>Username</span>

                    <input
                        type="text"
                        value={username}
                        onChange={(event) =>
                            setUsername(event.target.value)
                        }
                        placeholder="john"
                        disabled={createSubmitting}
                        autoFocus
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Имя</span>

                    <input
                        type="text"
                        value={displayName}
                        onChange={(event) =>
                            setDisplayName(event.target.value)
                        }
                        placeholder="John Smith"
                        disabled={createSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Email</span>

                    <input
                        type="email"
                        value={email}
                        onChange={(event) =>
                            setEmail(event.target.value)
                        }
                        placeholder="john@example.com"
                        disabled={createSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Пароль</span>

                    <input
                        type="password"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                        placeholder="Минимум 8 символов"
                        disabled={createSubmitting}
                    />
                  </label>

                  <label className="ticket-edit-field">
                    <span>Роль</span>

                    <select
                        value={role}
                        onChange={(event) =>
                            setRole(event.target.value)
                        }
                        disabled={createSubmitting}
                    >
                      {USER_ROLES.map((userRole) => (
                          <option
                              key={userRole}
                              value={userRole}
                          >
                            {userRole}
                          </option>
                      ))}
                    </select>
                  </label>
                </div>

                {createError && (
                    <div className="login-error">
                      {createError}
                    </div>
                )}

                <div className="ticket-edit-actions">
                  <button
                      type="submit"
                      className="ticket-primary-button"
                      disabled={
                          createSubmitting ||
                          !username.trim() ||
                          !displayName.trim() ||
                          !password
                      }
                  >
                    {createSubmitting
                        ? 'Создаём...'
                        : 'Создать'}
                  </button>

                  <button
                      type="button"
                      className="ticket-secondary-button"
                      onClick={handleCancelCreate}
                      disabled={createSubmitting}
                  >
                    Отмена
                  </button>
                </div>
              </form>
          )}

          {loading && (
              <div className="content-card">
                Загрузка пользователей...
              </div>
          )}

          {error && (
              <div className="login-error">
                {error}
              </div>
          )}

          {deleteError && (
              <div className="login-error">
                {deleteError}
              </div>
          )}

          {!loading &&
              !error &&
              users.length === 0 && (
                  <div className="content-card">
                    Пользователей пока нет
                  </div>
              )}

          {!loading && !error && users.length > 0 && (
              <div className="admin-users-container">
                <section className="admin-users-section">
                  <div className="admin-section-heading">
                    <div>
                      <h3>Активные пользователи</h3>
                      <p>Пользователи с доступом к системе</p>
                    </div>

                    <span className="admin-section-count">
                    {activeUsers.length}
                  </span>
                  </div>

                  {activeUsers.length === 0 ? (
                      <div className="content-card">
                        Активных пользователей нет
                      </div>
                  ) : (
                      <div className="admin-users-list">
                        {activeUsers.map(renderUserCard)}
                      </div>
                  )}
                </section>

                <section className="admin-users-section disabled-users-section">
                  <button
                      type="button"
                      className="admin-disabled-toggle"
                      onClick={() =>
                          setShowDisabledUsers((current) => !current)
                      }
                  >
                    <div className="admin-section-heading admin-section-heading-toggle">
                      <div>
                        <h3>Отключённые пользователи</h3>
                        <p>Пользователи без доступа к системе</p>
                      </div>

                      <div className="admin-disabled-heading-right">
                      <span className="admin-section-count">
                        {disabledUsers.length}
                      </span>

                        <span className="admin-disabled-arrow">
                        {showDisabledUsers ? '▲' : '▼'}
                      </span>
                      </div>
                    </div>
                  </button>

                  {showDisabledUsers && (
                      <>
                        {disabledUsers.length === 0 ? (
                            <div className="content-card">
                              Отключённых пользователей нет
                            </div>
                        ) : (
                            <div className="admin-users-list">
                              {disabledUsers.map(renderUserCard)}
                            </div>
                        )}
                      </>
                  )}
                </section>
              </div>
          )}
        </main>
      </div>
  )
}