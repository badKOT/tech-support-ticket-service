import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

import {
  createTicket,
  getProjectTickets,
} from '../api/api'

import UserBlock from '../components/UserBlock'

export default function ProjectPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()
  const { currentUser } = useAuth()

  const canViewAnalytics =
      currentUser?.role === 'TEAM_LEAD' ||
      currentUser?.role === 'ADMIN'

  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [creating, setCreating] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createError, setCreateError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')

    getProjectTickets(projectId)
    .then((data) => {
      setTickets(data)
    })
    .catch((error) => {
      console.error('Failed to load tickets', error)

      if (error.status === 403) {
        setError('У вас нет доступа к этому проекту')
      } else if (error.status === 404) {
        setError('Проект не найден')
      } else {
        setError('Не удалось загрузить тикеты')
      }
    })
    .finally(() => {
      setLoading(false)
    })
  }, [projectId])

  function handleStartCreate() {
    setTitle('')
    setDescription('')
    setCreateError('')
    setCreating(true)
  }

  function handleCancelCreate() {
    setCreating(false)
    setTitle('')
    setDescription('')
    setCreateError('')
  }

  async function handleCreateTicket(event) {
    event.preventDefault()

    const trimmedTitle = title.trim()
    const trimmedDescription = description.trim()

    if (!trimmedTitle) {
      setCreateError('Введите название обращения')
      return
    }

    setCreateSubmitting(true)
    setCreateError('')

    try {
      const newTicket = await createTicket(
          projectId,
          {
            title: trimmedTitle,
            description: trimmedDescription,
          },
      )

      setTickets((currentTickets) => [
        newTicket,
        ...currentTickets,
      ])

      setTitle('')
      setDescription('')
      setCreating(false)
    } catch (error) {
      console.error(
          'Failed to create ticket',
          error,
      )

      if (error.status === 403) {
        setCreateError(
            'У вас нет прав на создание обращения в этом проекте',
        )
      } else if (error.status === 404) {
        setCreateError(
            'Проект не найден',
        )
      } else if (error.status === 400) {
        setCreateError(
            'Проверьте введённые данные',
        )
      } else {
        setCreateError(
            'Не удалось создать обращение',
        )
      }
    } finally {
      setCreateSubmitting(false)
    }
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
              className="back-button"
              type="button"
              onClick={() => navigate('/')}
          >
            ← Назад к проектам
          </button>

          <div className="page-title project-page-title">
            <div>
              <h2>Обращения</h2>

              <p>
                Просмотр и управление обращениями проекта
              </p>
            </div>

            <div className="project-page-actions">
              {canViewAnalytics && (
                  <button
                      type="button"
                      className="ticket-secondary-button"
                      onClick={() =>
                          navigate(
                              `/projects/${projectId}/analytics`,
                          )
                      }
                  >
                    Аналитика
                  </button>
              )}

              {!creating && (
                  <button
                      type="button"
                      className="ticket-primary-button"
                      onClick={handleStartCreate}
                  >
                    + Создать обращение
                  </button>
              )}
            </div>
          </div>

          {creating && (
              <form
                  className="ticket-create-form content-card"
                  onSubmit={handleCreateTicket}
              >
                <div className="ticket-create-heading">
                  <div>
                    <h3>
                      Новое обращение
                    </h3>

                    <p>
                      Опишите проблему или запрос
                    </p>
                  </div>
                </div>

                <label className="ticket-edit-field">
                  <span>Название</span>

                  <input
                      type="text"
                      value={title}
                      onChange={(event) =>
                          setTitle(event.target.value)
                      }
                      placeholder="Например: Не работает корпоративная почта"
                      disabled={createSubmitting}
                      autoFocus
                  />
                </label>

                <label className="ticket-edit-field">
                  <span>Описание</span>

                  <textarea
                      value={description}
                      onChange={(event) =>
                          setDescription(event.target.value)
                      }
                      placeholder="Опишите проблему подробнее..."
                      rows={6}
                      disabled={createSubmitting}
                  />
                </label>

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
                          !title.trim()
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
                Загрузка тикетов...
              </div>
          )}

          {error && (
              <div className="login-error">
                {error}
              </div>
          )}

          {!loading &&
              !error &&
              tickets.length === 0 && (
                  <div className="content-card">
                    В этом проекте пока нет обращений
                  </div>
              )}

          {!loading &&
              !error &&
              tickets.length > 0 && (
                  <div className="tickets-list">
                    {tickets.map((ticket) => (
                        <button
                            className="ticket-card ticket-card-button"
                            key={ticket.id}
                            type="button"
                            onClick={() =>
                                navigate(`/tickets/${ticket.id}`)
                            }
                        >
                          <div className="ticket-main">
                            <div className="ticket-title-row">
                              <h3>
                                {ticket.title}
                              </h3>

                              <span
                                  className={`ticket-status ticket-status-${ticket.status?.toLowerCase()}`}
                              >
                        {ticket.status}
                      </span>
                            </div>

                            {ticket.description && (
                                <p className="ticket-description">
                                  {ticket.description}
                                </p>
                            )}

                            <div className="ticket-meta">
                      <span>
                        ID: {ticket.id}
                      </span>

                              {ticket.creatorName && (
                                  <span>
                          Автор: {ticket.creatorName}
                        </span>
                              )}

                              <span>
                        Исполнитель:{' '}
                                {ticket.assigneeName ?? 'Не назначен'}
                      </span>
                            </div>
                          </div>
                        </button>
                    ))}
                  </div>
              )}
        </main>
      </div>
  )
}