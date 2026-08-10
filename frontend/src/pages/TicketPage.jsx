import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import {
  addTicketComment,
  getTicket,
  getTicketComments,
} from '../api/api'

import { useAuth } from '../auth/AuthContext'
import UserBlock from '../components/UserBlock'

export default function TicketPage() {
  const { ticketId } = useParams()
  const navigate = useNavigate()

  const { currentUser } = useAuth()

  const [ticket, setTicket] = useState(null)
  const [comments, setComments] = useState([])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [commentText, setCommentText] = useState('')
  const [commentSubmitting, setCommentSubmitting] = useState(false)
  const [commentError, setCommentError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')

    Promise.all([
      getTicket(ticketId),
      getTicketComments(ticketId),
    ])
    .then(([ticketResponse, commentsResponse]) => {
      setTicket(ticketResponse)
      setComments(commentsResponse)
    })
    .catch((error) => {
      console.error(
          'Failed to load ticket',
          error,
      )

      if (error.status === 403) {
        setError(
            'У вас нет доступа к этому тикету',
        )
      } else if (error.status === 404) {
        setError('Тикет не найден')
      } else {
        setError(
            'Не удалось загрузить тикет',
        )
      }
    })
    .finally(() => {
      setLoading(false)
    })
  }, [ticketId])

  async function handleAddComment(event) {
    event.preventDefault()

    const text = commentText.trim()

    if (!text || !currentUser) {
      return
    }

    setCommentSubmitting(true)
    setCommentError('')

    try {
      const newComment =
          await addTicketComment(
              ticketId,
              text,
              currentUser.id,
          )

      setComments((currentComments) => [
        ...currentComments,
        newComment,
      ])

      setCommentText('')
    } catch (error) {
      console.error(
          'Failed to add comment',
          error,
      )

      if (error.status === 403) {
        setCommentError(
            'У вас нет прав на добавление комментария',
        )
      } else {
        setCommentError(
            'Не удалось добавить комментарий',
        )
      }
    } finally {
      setCommentSubmitting(false)
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
              type="button"
              className="back-button"
              onClick={() => navigate(-1)}
          >
            ← Назад
          </button>

          {loading && (
              <div className="content-card">
                Загрузка тикета...
              </div>
          )}

          {error && (
              <div className="login-error">
                {error}
              </div>
          )}

          {!loading && !error && ticket && (
              <>
                <section className="ticket-details-card">
                  <div className="ticket-details-header">
                    <div>
                      <div className="ticket-number">
                        Тикет #{ticket.id}
                      </div>

                      <h2>
                        {ticket.title}
                      </h2>
                    </div>

                    <span
                        className={`ticket-status ticket-status-${ticket.status?.toLowerCase()}`}
                    >
                  {ticket.status}
                </span>
                  </div>

                  {ticket.description && (
                      <p className="ticket-details-description">
                        {ticket.description}
                      </p>
                  )}

                  <div className="ticket-details-meta">
                    {ticket.creator && (
                        <div>
                          <span>Автор</span>

                          <strong>
                            {ticket.creator.displayName}
                          </strong>
                        </div>
                    )}

                    <div>
                      <span>Исполнитель</span>

                      <strong>
                        {ticket.assignee
                            ? ticket.assignee.displayName
                            : 'Не назначен'}
                      </strong>
                    </div>

                    {ticket.createdAt && (
                        <div>
                          <span>Создан</span>

                          <strong>
                            {new Date(
                                ticket.createdAt,
                            ).toLocaleString()}
                          </strong>
                        </div>
                    )}
                  </div>
                </section>

                <section className="comments-section">
                  <div className="section-heading">
                    <h3>Комментарии</h3>

                    <span>
                  {comments.length}
                </span>
                  </div>

                  <form
                      className="comment-form"
                      onSubmit={handleAddComment}
                  >
                <textarea
                    value={commentText}
                    onChange={(event) =>
                        setCommentText(
                            event.target.value,
                        )
                    }
                    placeholder="Добавить комментарий..."
                    rows={4}
                />

                    {commentError && (
                        <div className="login-error">
                          {commentError}
                        </div>
                    )}

                    <div className="comment-form-actions">
                      <button
                          type="submit"
                          disabled={
                              commentSubmitting ||
                              !commentText.trim()
                          }
                      >
                        {commentSubmitting
                            ? 'Отправляем...'
                            : 'Отправить'}
                      </button>
                    </div>
                  </form>

                  {comments.length === 0 && (
                      <div className="content-card comments-empty">
                        Комментариев пока нет
                      </div>
                  )}

                  {comments.length > 0 && (
                      <div className="comments-list">
                        {comments.map((comment) => (
                            <article
                                className="comment-card"
                                key={comment.id}
                            >
                              <div className="comment-header">
                                <strong>
                                  {comment.author
                                          ?.displayName ??
                                      'Пользователь'}
                                </strong>

                                {comment.createdAt && (
                                    <time>
                                      {new Date(
                                          comment.createdAt,
                                      ).toLocaleString()}
                                    </time>
                                )}
                              </div>

                              <p>
                                {comment.content}
                              </p>
                            </article>
                        ))}
                      </div>
                  )}
                </section>
              </>
          )}
        </main>
      </div>
  )
}