import { useEffect, useState } from 'react'
import {
  useNavigate,
  useParams,
} from 'react-router-dom'

import {
  getProjectAnalytics,
} from '../api/api'

import UserBlock from '../components/UserBlock'

export default function ProjectAnalyticsPage() {
  const { projectId } = useParams()
  const navigate = useNavigate()

  const [analytics, setAnalytics] =
      useState(null)

  const [loading, setLoading] =
      useState(true)

  const [error, setError] =
      useState('')

  useEffect(() => {
    loadAnalytics()
  }, [projectId])

  async function loadAnalytics() {
    setLoading(true)
    setError('')

    try {
      const data =
          await getProjectAnalytics(projectId)

      setAnalytics(data)
    } catch (error) {
      console.error(
          'Failed to load project analytics',
          error,
      )

      if (error.status === 403) {
        setError(
            'У вас нет прав на просмотр аналитики',
        )
      } else if (error.status === 404) {
        setError(
            'Проект не найден',
        )
      } else {
        setError(
            'Не удалось загрузить аналитику',
        )
      }
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
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
            <div className="content-card">
              Загрузка аналитики...
            </div>
          </main>
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
              className="back-button"
              type="button"
              onClick={() =>
                  navigate(
                      `/projects/${projectId}`,
                  )
              }
          >
            ← Назад к обращениям
          </button>

          <div className="page-title">
            <div>
              <h2>
                Аналитика проекта
              </h2>

              <p>
                Статистика обращений и работы команды
              </p>
            </div>
          </div>

          {error && (
              <div className="login-error">
                {error}
              </div>
          )}

          {!error && analytics && (
              <AnalyticsContent
                  analytics={analytics}
              />
          )}
        </main>
      </div>
  )
}

function AnalyticsContent({ analytics }) {
  const statusCounts =
      analytics.statusCounts ?? {}

  const dailyStats =
      analytics.createdVsResolved ?? []

  const assigneeStats =
      analytics.avgResolutionTimes ?? []

  const totalTickets =
      Object.values(statusCounts)
      .reduce(
          (sum, count) =>
              sum + Number(count ?? 0),
          0,
      )

  const maxStatusCount =
      Math.max(
          ...Object.values(statusCounts).map(
              (value) => Number(value ?? 0),
          ),
          1,
      )

  const maxDailyValue =
      Math.max(
          ...dailyStats.flatMap((day) => [
            Number(day.created ?? 0),
            Number(day.resolved ?? 0),
          ]),
          1,
      )

  return (
      <div className="analytics-content">

        <section className="analytics-summary">
          <div className="analytics-summary-card analytics-summary-total">
          <span>
            Всего обращений
          </span>

            <strong>
              {totalTickets}
            </strong>
          </div>

          {Object.entries(statusCounts)
          .map(([status, count]) => (
              <div
                  className="analytics-summary-card"
                  key={status}
              >
              <span>
                {status}
              </span>

                <strong>
                  {count}
                </strong>
              </div>
          ))}
        </section>

        <section className="content-card analytics-section">
          <div className="analytics-section-heading">
            <div>
              <h3>
                Обращения по статусам
              </h3>

              <p>
                Распределение текущих обращений
              </p>
            </div>
          </div>

          <div className="analytics-status-list">
            {Object.entries(statusCounts)
            .sort(
                ([, firstCount], [, secondCount]) =>
                    Number(secondCount) -
                    Number(firstCount),
            )
            .map(([status, count]) => {
              const numericCount =
                  Number(count ?? 0)

              const percentage =
                  totalTickets > 0
                      ? Math.round(
                          numericCount /
                          totalTickets *
                          100,
                      )
                      : 0

              const width =
                  numericCount /
                  maxStatusCount *
                  100

              return (
                  <div
                      className="analytics-status-row"
                      key={status}
                  >
                    <div className="analytics-status-info">
                    <span>
                      {status}
                    </span>

                      <div>
                        <strong>
                          {numericCount}
                        </strong>

                        <small>
                          {percentage}%
                        </small>
                      </div>
                    </div>

                    <div className="analytics-status-track">
                      <div
                          className="analytics-status-bar"
                          style={{
                            width: `${width}%`,
                          }}
                      />
                    </div>
                  </div>
              )
            })}
          </div>
        </section>

        <section className="content-card analytics-section">
          <div className="analytics-section-heading">
            <div>
              <h3>
                Динамика обращений
              </h3>

              <p>
                Созданные и решённые обращения за последние 14 дней
              </p>
            </div>

            <div className="analytics-chart-legend">
            <span>
              <i className="analytics-legend-created" />
              Создано
            </span>

              <span>
              <i className="analytics-legend-resolved" />
              Решено
            </span>
            </div>
          </div>

          {dailyStats.length === 0 ? (
              <p className="analytics-empty">
                Данных пока нет
              </p>
          ) : (
              <div className="analytics-chart">
                {dailyStats.map((day) => {
                  const created =
                      Number(day.created ?? 0)

                  const resolved =
                      Number(day.resolved ?? 0)

                  const createdHeight =
                      created /
                      maxDailyValue *
                      100

                  const resolvedHeight =
                      resolved /
                      maxDailyValue *
                      100

                  return (
                      <div
                          className="analytics-chart-column"
                          key={day.date}
                      >
                        <div className="analytics-chart-bars">
                          <div
                              className="analytics-chart-bar analytics-chart-bar-created"
                              style={{
                                height: `${createdHeight}%`,
                              }}
                              title={`Создано: ${created}`}
                          >
                            {created > 0 && (
                                <span>
                          {created}
                        </span>
                            )}
                          </div>

                          <div
                              className="analytics-chart-bar analytics-chart-bar-resolved"
                              style={{
                                height: `${resolvedHeight}%`,
                              }}
                              title={`Решено: ${resolved}`}
                          >
                            {resolved > 0 && (
                                <span>
                          {resolved}
                        </span>
                            )}
                          </div>
                        </div>

                        <span className="analytics-chart-date">
                    {formatShortDate(day.date)}
                  </span>
                      </div>
                  )
                })}
              </div>
          )}
        </section>

        <section className="content-card analytics-section">
          <div className="analytics-section-heading">
            <div>
              <h3>
                Работа исполнителей
              </h3>

              <p>
                Среднее время решения обращений
              </p>
            </div>
          </div>

          {assigneeStats.length === 0 ? (
              <p className="analytics-empty">
                Данных пока нет
              </p>
          ) : (
              <div className="analytics-assignees">
                {assigneeStats
                .slice()
                .sort(
                    (first, second) =>
                        Number(first.avgHours ?? 0) -
                        Number(second.avgHours ?? 0),
                )
                .map(
                    (assignee, index) => (
                        <div
                            className="analytics-assignee-row"
                            key={`${assignee.assigneeName}-${index}`}
                        >
                          <div>
                            <strong>
                              {assignee.assigneeName ??
                                  'Неизвестный исполнитель'}
                            </strong>

                            <span>
                        Среднее время решения
                      </span>
                          </div>

                          <strong>
                            {formatHours(
                                assignee.avgHours,
                            )}
                          </strong>
                        </div>
                    ),
                )}
              </div>
          )}
        </section>
      </div>
  )
}

function formatHours(value) {
  if (
      value === null ||
      value === undefined
  ) {
    return '—'
  }

  const number = Number(value)

  if (Number.isNaN(number)) {
    return '—'
  }

  return `${number.toFixed(1)} ч`
}

function formatShortDate(value) {
  if (!value) {
    return ''
  }

  const parts = value.split('-')

  if (parts.length !== 3) {
    return value
  }

  return `${parts[2]}.${parts[1]}`
}