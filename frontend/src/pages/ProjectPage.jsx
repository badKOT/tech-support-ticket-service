import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { getProjectTickets } from '../api/api'
import UserBlock from '../components/UserBlock'

export default function ProjectPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()

  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

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

          <div className="page-title">
            <h2>Обращения</h2>
          </div>

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

          {!loading && !error && tickets.length === 0 && (
              <div className="content-card">
                В этом проекте пока нет обращений
              </div>
          )}

          {!loading && !error && tickets.length > 0 && (
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

                          {ticket.creator && (
                              <span>
                        Автор: {ticket.creator.displayName}
                      </span>
                          )}

                          {ticket.assignee && (
                              <span>
                        Исполнитель: {ticket.assignee.displayName}
                      </span>
                          )}
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