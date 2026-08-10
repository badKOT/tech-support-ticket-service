import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getProjects } from '../api/api'
import UserBlock from '../components/UserBlock'

export default function HomePage() {
  const navigate = useNavigate()

  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getProjects()
    .then(setProjects)
    .catch((error) => {
      console.error('Failed to load projects', error)
      setError('Не удалось загрузить проекты')
    })
    .finally(() => {
      setLoading(false)
    })
  }, [])

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
          <div className="page-title">
            <h2>Проекты</h2>
          </div>

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

          {!loading && !error && projects.length === 0 && (
              <div className="content-card">
                Проектов пока нет
              </div>
          )}

          {!loading && !error && projects.length > 0 && (
              <div className="projects-grid">
                {projects.map((project) => (
                    <button
                        key={project.id}
                        type="button"
                        className="project-card"
                        onClick={() =>
                            navigate(`/projects/${project.id}`)
                        }
                    >
                      <div className="project-icon">
                        {project.name
                        ?.charAt(0)
                        .toUpperCase()}
                      </div>

                      <div className="project-info">
                        <h3>{project.name}</h3>

                        {project.description && (
                            <p>{project.description}</p>
                        )}
                      </div>

                      <span className="project-arrow">
                  →
                </span>
                    </button>
                ))}
              </div>
          )}
        </main>
      </div>
  )
}