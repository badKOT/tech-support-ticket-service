import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import {
  addTicketComment,
  assignTicket,
  changeTicketStatus,
  deleteTicket,
  getAvailableAssignees,
  getTicket,
  getTicketComments,
  updateTicket,
} from '../api/api'

import { useAuth } from '../auth/AuthContext'
import UserBlock from '../components/UserBlock'

const TICKET_STATUSES = [
  'OPEN',
  'IN_PROGRESS',
  'IN_REVIEW',
  'NEED_INFO',
  'RESOLVED',
  'CLOSED',
  'REOPENED',
]

export default function TicketPage() {
  const { ticketId } = useParams()
  const navigate = useNavigate()

  const { currentUser } = useAuth()

  const [ticket, setTicket] = useState(null)
  const [comments, setComments] = useState([])
  const [assignees, setAssignees] = useState([])

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [commentText, setCommentText] = useState('')
  const [commentSubmitting, setCommentSubmitting] = useState(false)
  const [commentError, setCommentError] = useState('')

  const [statusSubmitting, setStatusSubmitting] = useState(false)
  const [statusError, setStatusError] = useState('')

  const [assigneeSubmitting, setAssigneeSubmitting] = useState(false)
  const [assigneeError, setAssigneeError] = useState('')

  const [editing, setEditing] = useState(false)
  const [editTitle, setEditTitle] = useState('')
  const [editDescription, setEditDescription] = useState('')
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')

  const [deleteSubmitting, setDeleteSubmitting] = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const canManageAssignment =
      currentUser &&
      (
          currentUser.role === 'ADMIN' ||
          currentUser.role === 'TEAM_LEAD'
      )

  useEffect(() => {
    setLoading(true)
    setError('')

    const requests = [
      getTicket(ticketId),
      getTicketComments(ticketId),
    ]

    if (canManageAssignment) {
      requests.push(getAvailableAssignees())
    }

    Promise.all(requests)
    .then((responses) => {
      setTicket(responses[0])
      setComments(responses[1])

      if (canManageAssignment) {
        setAssignees(responses[2] ?? [])
      }
    })
    .catch((error) => {
      console.error('Failed to load ticket', error)

      if (error.status === 403) {
        setError('У вас нет доступа к этому тикету')
      } else if (error.status === 404) {
        setError('Тикет не найден')
      } else {
        setError('Не удалось загрузить тикет')
      }
    })
    .finally(() => {
      setLoading(false)
    })
  }, [ticketId, canManageAssignment])

  const canChangeStatus =
      currentUser &&
      ticket &&
      (
          currentUser.role === 'ADMIN' ||
          currentUser.role === 'TEAM_LEAD' ||
          (
              currentUser.role === 'SUPPORT_AGENT' &&
              ticket.assigneeId === currentUser.id
          )
      )

  const canEdit =
      currentUser &&
      ticket &&
      (
          currentUser.role === 'ADMIN' ||
          currentUser.role === 'TEAM_LEAD' ||
          (
              currentUser.role === 'REQUESTER' &&
              ticket.creatorId === currentUser.id &&
              ticket.status === 'OPEN'
          )
      )

  const canDelete =
      currentUser?.role === 'ADMIN'

  async function handleStatusChange(event) {
    const newStatus = event.target.value

    if (!ticket || newStatus === ticket.status) {
      return
    }

    setStatusSubmitting(true)
    setStatusError('')

    try {
      const updatedTicket = await changeTicketStatus(
          ticketId,
          newStatus,
      )

      setTicket(updatedTicket)
    } catch (error) {
      console.error('Failed to change ticket status', error)

      if (error.status === 403) {
        setStatusError(
            'У вас нет прав на изменение статуса',
        )
      } else {
        setStatusError(
            'Не удалось изменить статус',
        )
      }
    } finally {
      setStatusSubmitting(false)
    }
  }

  async function handleAssigneeChange(event) {
    const value = event.target.value

    const assigneeId =
        value === ''
            ? null
            : Number(value)

    if (assigneeId === ticket.assigneeId) {
      return
    }

    setAssigneeSubmitting(true)
    setAssigneeError('')

    try {
      const updatedTicket = await assignTicket(
          ticketId,
          assigneeId,
      )

      setTicket(updatedTicket)
    } catch (error) {
      console.error('Failed to assign ticket', error)

      if (error.status === 403) {
        setAssigneeError(
            'У вас нет прав на назначение исполнителя',
        )
      } else if (error.status === 400) {
        setAssigneeError(
            'Нельзя назначить выбранного пользователя',
        )
      } else {
        setAssigneeError(
            'Не удалось изменить исполнителя',
        )
      }
    } finally {
      setAssigneeSubmitting(false)
    }
  }

  function handleStartEdit() {
    setEditTitle(ticket.title ?? '')
    setEditDescription(ticket.description ?? '')
    setEditError('')
    setEditing(true)
  }

  function handleCancelEdit() {
    setEditing(false)
    setEditError('')
  }

  async function handleSaveEdit(event) {
    event.preventDefault()

    const title = editTitle.trim()
    const description = editDescription.trim()

    if (!title) {
      setEditError('Название тикета не может быть пустым')
      return
    }

    setEditSubmitting(true)
    setEditError('')

    try {
      const updatedTicket = await updateTicket(
          ticketId,
          {
            title,
            description,
            projectId: ticket.projectId,
          },
      )

      setTicket(updatedTicket)
      setEditing(false)
    } catch (error) {
      console.error('Failed to update ticket', error)

      if (error.status === 403) {
        setEditError(
            'У вас нет прав на редактирование тикета',
        )
      } else if (error.status === 400) {
        setEditError(
            'Проверьте введённые данные',
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

  async function handleDelete() {
    const confirmed = window.confirm(
        `Удалить тикет #${ticket.id} «${ticket.title}»?`,
    )

    if (!confirmed) {
      return
    }

    setDeleteSubmitting(true)
    setDeleteError('')

    try {
      await deleteTicket(ticketId)

      navigate(`/projects/${ticket.projectId}/tickets`)
    } catch (error) {
      console.error('Failed to delete ticket', error)

      if (error.status === 403) {
        setDeleteError(
            'У вас нет прав на удаление тикета',
        )
      } else {
        setDeleteError(
            'Не удалось удалить тикет',
        )
      }

      setDeleteSubmitting(false)
    }
  }

  async function handleAddComment(event) {
    event.preventDefault()

    const text = commentText.trim()

    if (!text || !currentUser) {
      return
    }

    setCommentSubmitting(true)
    setCommentError('')

    try {
      const newComment = await addTicketComment(
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
      console.error('Failed to add comment', error)

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

                      {!editing && (
                          <h2>
                            {ticket.title}
                          </h2>
                      )}
                    </div>

                    <div className="ticket-actions">
                      <div className="ticket-action-field">
                    <span className="ticket-action-label">
                      Статус
                    </span>

                        {canChangeStatus ? (
                            <select
                                value={ticket.status}
                                onChange={handleStatusChange}
                                disabled={statusSubmitting}
                            >
                              {TICKET_STATUSES.map((status) => (
                                  <option
                                      key={status}
                                      value={status}
                                  >
                                    {status}
                                  </option>
                              ))}
                            </select>
                        ) : (
                            <span
                                className={`ticket-status ticket-status-${ticket.status?.toLowerCase()}`}
                            >
                        {ticket.status}
                      </span>
                        )}
                      </div>

                      {canManageAssignment && (
                          <div className="ticket-action-field">
                      <span className="ticket-action-label">
                        Исполнитель
                      </span>

                            <select
                                value={ticket.assigneeId ?? ''}
                                onChange={handleAssigneeChange}
                                disabled={assigneeSubmitting}
                            >
                              <option value="">
                                Не назначен
                              </option>

                              {assignees.map((user) => (
                                  <option
                                      key={user.id}
                                      value={user.id}
                                  >
                                    {user.displayName}
                                    {' — '}
                                    {user.role}
                                  </option>
                              ))}
                            </select>
                          </div>
                      )}
                    </div>
                  </div>

                  {statusError && (
                      <div className="login-error ticket-action-error">
                        {statusError}
                      </div>
                  )}

                  {assigneeError && (
                      <div className="login-error ticket-action-error">
                        {assigneeError}
                      </div>
                  )}

                  {editing ? (
                      <form
                          className="ticket-edit-form"
                          onSubmit={handleSaveEdit}
                      >
                        <label className="ticket-edit-field">
                          <span>Название</span>

                          <input
                              type="text"
                              value={editTitle}
                              onChange={(event) =>
                                  setEditTitle(event.target.value)
                              }
                              disabled={editSubmitting}
                          />
                        </label>

                        <label className="ticket-edit-field">
                          <span>Описание</span>

                          <textarea
                              value={editDescription}
                              onChange={(event) =>
                                  setEditDescription(event.target.value)
                              }
                              rows={6}
                              disabled={editSubmitting}
                          />
                        </label>

                        {editError && (
                            <div className="login-error">
                              {editError}
                            </div>
                        )}

                        <div className="ticket-edit-actions">
                          <button
                              type="submit"
                              className="ticket-primary-button"
                              disabled={
                                  editSubmitting ||
                                  !editTitle.trim()
                              }
                          >
                            {editSubmitting
                                ? 'Сохраняем...'
                                : 'Сохранить'}
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
                        {ticket.description && (
                            <p className="ticket-details-description">
                              {ticket.description}
                            </p>
                        )}

                        <div className="ticket-content-actions">
                          {canEdit && (
                              <button
                                  type="button"
                                  className="ticket-secondary-button"
                                  onClick={handleStartEdit}
                              >
                                Редактировать
                              </button>
                          )}

                          {canDelete && (
                              <button
                                  type="button"
                                  className="ticket-delete-button"
                                  onClick={handleDelete}
                                  disabled={deleteSubmitting}
                              >
                                {deleteSubmitting
                                    ? 'Удаляем...'
                                    : 'Удалить тикет'}
                              </button>
                          )}
                        </div>
                      </>
                  )}

                  {deleteError && (
                      <div className="login-error ticket-action-error">
                        {deleteError}
                      </div>
                  )}

                  <div className="ticket-details-meta">
                    {ticket.creatorName && (
                        <div>
                          <span>Автор</span>

                          <strong>
                            {ticket.creatorName}
                          </strong>
                        </div>
                    )}

                    <div>
                      <span>Исполнитель</span>

                      <strong>
                        {ticket.assigneeName ?? 'Не назначен'}
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
                        setCommentText(event.target.value)
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
                                  {comment.author?.displayName ??
                                      comment.authorName ??
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