import { useEffect, useState } from 'react'

import {
  createAdminProject,
  getAdminProjects,
  updateAdminProject,
} from '../../api/api'

export default function AdminProjects() {
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // CREATE
  const [creating, setCreating] = useState(false)
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createError, setCreateError] = useState('')

  const [name, setName] = useState('')
  const [projectKey, setProjectKey] = useState('')
  const [description, setDescription] = useState('')

  // EDIT
  const [editingProjectId, setEditingProjectId] = useState(null)
  const [editName, setEditName] = useState('')
  const [editKey, setEditKey] = useState('')
  const [editDescription, setEditDescription] = useState('')

  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')

  useEffect(() => {
    loadProjects()
  }, [])

  async function loadProjects() {
    setLoading(true)
    setError('')

    try {
      const data = await getAdminProjects()
      setProjects(data)
    } catch (error) {
      console.error('Failed to load projects', error)

      if (error.status === 403) {
        setError(
            'У вас нет прав на просмотр проектов',
        )
      } else {
        setError(
            'Не удалось загрузить проекты',
        )
      }
    } finally {
      setLoading(false)
    }
  }

  function handleStartCreate() {
    setEditingProjectId(null)

    setName('')
    setProjectKey('')
    setDescription('')

    setCreateError('')
    setCreating(true)
  }

  function handleCancelCreate() {
    setCreating(false)
    setCreateError('')
  }

  async function handleCreateProject(event) {
    event.preventDefault()

    const trimmedName = name.trim()
    const trimmedKey = projectKey.trim().toUpperCase()
    const trimmedDescription = description.trim()

    if (!trimmedName) {
      setCreateError(
          'Введите название проекта',
      )
      return
    }

    if (!trimmedKey) {
      setCreateError(
          'Введите ключ проекта',
      )
      return
    }

    setCreateSubmitting(true)
    setCreateError('')

    try {
      const newProject = await createAdminProject({
        name: trimmedName,
        key: trimmedKey,
        description: trimmedDescription || null,
      })

      setProjects((currentProjects) => [
        ...currentProjects,
        newProject,
      ])

      setCreating(false)

      setName('')
      setProjectKey('')
      setDescription('')
    } catch (error) {
      console.error(
          'Failed to create project',
          error,
      )

      if (error.status === 400) {
        setCreateError(
            'Проверьте введённые данные',
        )
      } else if (error.status === 409) {
        setCreateError(
            'Проект с таким ключом уже существует',
        )
      } else if (error.status === 403) {
        setCreateError(
            'У вас нет прав на создание проектов',
        )
      } else {
        setCreateError(
            'Не удалось создать проект',
        )
      }
    } finally {
      setCreateSubmitting(false)
    }
  }

  function handleStartEdit(project) {
    setCreating(false)

    setEditingProjectId(project.id)

    setEditName(project.name ?? '')
    setEditKey(project.key ?? '')
    setEditDescription(project.description ?? '')

    setEditError('')
  }

  function handleCancelEdit() {
    setEditingProjectId(null)
    setEditError('')
  }

  async function handleUpdateProject(
      event,
      projectId,
  ) {
    event.preventDefault()

    const trimmedName = editName.trim()
    const trimmedKey = editKey.trim().toUpperCase()
    const trimmedDescription = editDescription.trim()

    if (!trimmedName) {
      setEditError(
          'Название проекта не может быть пустым',
      )
      return
    }

    if (!trimmedKey) {
      setEditError(
          'Ключ проекта не может быть пустым',
      )
      return
    }

    setEditSubmitting(true)
    setEditError('')

    try {
      const updatedProject = await updateAdminProject(
          projectId,
          {
            name: trimmedName,
            key: trimmedKey,
            description: trimmedDescription || null,
          },
      )

      setProjects((currentProjects) =>
          currentProjects.map((project) =>
              project.id === updatedProject.id
                  ? updatedProject
                  : project,
          ),
      )

      setEditingProjectId(null)
    } catch (error) {
      console.error(
          'Failed to update project',
          error,
      )

      if (error.status === 400) {
        setEditError(
            'Проверьте введённые данные',
        )
      } else if (error.status === 409) {
        setEditError(
            'Проект с таким ключом уже существует',
        )
      } else if (error.status === 403) {
        setEditError(
            'У вас нет прав на изменение проекта',
        )
      } else if (error.status === 404) {
        setEditError(
            'Проект не найден',
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

  return (
      <>
        <div className="page-title admin-page-title">
          <div>
            <h2>Проекты</h2>

            <p>
              Управление проектами системы
            </p>
          </div>

          {!creating && (
              <button
                  type="button"
                  className="ticket-primary-button"
                  onClick={handleStartCreate}
              >
                + Создать проект
              </button>
          )}
        </div>

        {creating && (
            <form
                className="admin-create-form content-card"
                onSubmit={handleCreateProject}
            >
              <div className="ticket-create-heading">
                <div>
                  <h3>Новый проект</h3>

                  <p>
                    Создание проекта технической поддержки
                  </p>
                </div>
              </div>

              <div className="admin-create-grid">
                <label className="ticket-edit-field">
                  <span>Название</span>

                  <input
                      type="text"
                      value={name}
                      onChange={(event) =>
                          setName(event.target.value)
                      }
                      placeholder="Tech Support"
                      disabled={createSubmitting}
                      autoFocus
                  />
                </label>

                <label className="ticket-edit-field">
                  <span>Ключ</span>

                  <input
                      type="text"
                      value={projectKey}
                      onChange={(event) =>
                          setProjectKey(
                              event.target.value.toUpperCase(),
                          )
                      }
                      placeholder="TS"
                      disabled={createSubmitting}
                  />
                </label>
              </div>

              <label className="ticket-edit-field">
                <span>Описание</span>

                <textarea
                    value={description}
                    onChange={(event) =>
                        setDescription(event.target.value)
                    }
                    placeholder="Описание проекта"
                    rows={4}
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
                        !name.trim() ||
                        !projectKey.trim()
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
              Загрузка проектов...
            </div>
        )}

        {error && (
            <div className="login-error">
              {error}
            </div>
        )}

        {!loading &&
            !error &&
            projects.length === 0 && (
                <div className="content-card">
                  Проектов пока нет
                </div>
            )}

        {!loading &&
            !error &&
            projects.length > 0 && (
                <div className="admin-users-list">
                  {projects.map((project) => {
                    const isEditing =
                        editingProjectId === project.id

                    return (
                        <div
                            className="admin-user-card admin-project-card"
                            key={project.id}
                        >
                          {isEditing ? (
                              <form
                                  className="admin-user-edit-form"
                                  onSubmit={(event) =>
                                      handleUpdateProject(
                                          event,
                                          project.id,
                                      )
                                  }
                              >
                                <div className="admin-edit-heading">
                                  <h3>
                                    Редактирование проекта
                                  </h3>

                                  <span>
                          ID: {project.id}
                        </span>
                                </div>

                                <div className="admin-create-grid">
                                  <label className="ticket-edit-field">
                                    <span>Название</span>

                                    <input
                                        type="text"
                                        value={editName}
                                        onChange={(event) =>
                                            setEditName(
                                                event.target.value,
                                            )
                                        }
                                        disabled={editSubmitting}
                                    />
                                  </label>

                                  <label className="ticket-edit-field">
                                    <span>Ключ</span>

                                    <input
                                        type="text"
                                        value={editKey}
                                        onChange={(event) =>
                                            setEditKey(
                                                event.target.value
                                                .toUpperCase(),
                                            )
                                        }
                                        disabled={editSubmitting}
                                    />
                                  </label>
                                </div>

                                <label className="ticket-edit-field">
                                  <span>Описание</span>

                                  <textarea
                                      value={editDescription}
                                      onChange={(event) =>
                                          setEditDescription(
                                              event.target.value,
                                          )
                                      }
                                      rows={4}
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
                                          !editName.trim() ||
                                          !editKey.trim()
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
                                <div className="admin-user-main">
                                  <div className="admin-user-avatar admin-project-key">
                                    {project.key}
                                  </div>

                                  <div>
                                    <h3>
                                      {project.name}
                                    </h3>

                                    <span className="admin-username">
                            {project.key}
                          </span>
                                  </div>
                                </div>

                                <div className="admin-user-info">
                                  <button
                                      type="button"
                                      className="ticket-secondary-button"
                                      onClick={() =>
                                          handleStartEdit(project)
                                      }
                                  >
                                    Редактировать
                                  </button>
                                </div>

                                {project.description && (
                                    <div className="admin-user-email">
                                      {project.description}
                                    </div>
                                )}
                              </>
                          )}
                        </div>
                    )
                  })}
                </div>
            )}
      </>
  )
}