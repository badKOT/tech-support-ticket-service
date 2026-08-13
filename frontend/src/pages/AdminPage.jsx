import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import UserBlock from '../components/UserBlock'
import AdminUsers from '../components/admin/AdminUsers'
import AdminProjects from '../components/admin/AdminProjects'

export default function AdminPage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('users')

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

          <div className="page-title">
            <div>
              <h2>Администрирование</h2>

              <p>
                Управление пользователями и проектами системы
              </p>
            </div>
          </div>

          <div className="admin-tabs">
            <button
                type="button"
                className={
                  activeTab === 'users'
                      ? 'admin-tab admin-tab-active'
                      : 'admin-tab'
                }
                onClick={() => setActiveTab('users')}
            >
              Пользователи
            </button>

            <button
                type="button"
                className={
                  activeTab === 'projects'
                      ? 'admin-tab admin-tab-active'
                      : 'admin-tab'
                }
                onClick={() => setActiveTab('projects')}
            >
              Проекты
            </button>
          </div>

          {activeTab === 'users' && (
              <AdminUsers />
          )}

          {activeTab === 'projects' && (
              <AdminProjects />
          )}
        </main>
      </div>
  )
}