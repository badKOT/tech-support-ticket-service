import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  exchangeOidcSession,
  setTokens,
} from '../api/api'

let oidcExchangePromise = null

function exchangeOidcOnce() {
  if (!oidcExchangePromise) {
    oidcExchangePromise =
        exchangeOidcSession()
  }

  return oidcExchangePromise
}

export default function OidcCallbackPage() {
  const navigate = useNavigate()

  const [error, setError] =
      useState(false)

  useEffect(() => {
    async function finishOidcLogin() {
      try {
        const response =
            await exchangeOidcOnce()

        if (
            !response?.accessToken ||
            !response?.refreshToken
        ) {
          throw new Error(
              'OIDC response does not contain tokens'
          )
        }

        setTokens(
            response.accessToken,
            response.refreshToken
        )

        window.location.replace('/')
      } catch (error) {
        console.error(
            'OIDC login failed',
            error
        )

        setError(true)
      }
    }

    finishOidcLogin()
  }, [])

  if (error) {
    return (
        <main>
          <h1>Ошибка входа</h1>

          <button
              type="button"
              onClick={() =>
                  navigate('/login', {
                    replace: true,
                  })
              }
          >
            Вернуться к входу
          </button>
        </main>
    )
  }

  return <div>Завершаем вход...</div>
}