let unauthorizedHandler = null

const ACCESS_TOKEN_KEY = 'tech-support-access-token'

const REFRESH_TOKEN_KEY = 'tech-support-refresh-token'

let refreshPromise = null

// =========================================================
// TOKENS
// =========================================================

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY,)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY,)
}

export function setTokens(accessToken, refreshToken,) {
  if (accessToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken,)
  }

  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken,)
  }
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY,)

  localStorage.removeItem(REFRESH_TOKEN_KEY,)
}

export function hasAccessToken() {
  return Boolean(getAccessToken())
}

export function hasRefreshToken() {
  return Boolean(getRefreshToken())
}

// =========================================================
// GLOBAL 401 HANDLER
// =========================================================

export function setUnauthorizedHandler(handler,) {
  unauthorizedHandler = handler
}

// =========================================================
// REFRESH
// =========================================================

async function refreshTokens() {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    throw new Error('Refresh token is missing',)
  }

  const response = await fetch('/api/auth/refresh', {
    method: 'POST',

    headers: {
      'Content-Type': 'application/json',
    },

    body: JSON.stringify({
      refreshToken,
    }),
  },)

  if (!response.ok) {
    const error = new Error(`HTTP ${response.status}`,)

    error.status = response.status

    throw error
  }

  const result = await response.json()

  if (!result.accessToken || !result.refreshToken) {
    throw new Error('Refresh response does not contain tokens',)
  }

  setTokens(result.accessToken, result.refreshToken,)

  return result.accessToken
}

async function getFreshAccessToken() {
  /*
   * Если сразу несколько запросов получили 401,
   * выполняем только один refresh.
   *
   * Остальные запросы ждут тот же Promise.
   */
  if (!refreshPromise) {
    refreshPromise = refreshTokens()
    .finally(() => {
      refreshPromise = null
    })
  }

  return refreshPromise
}

// =========================================================
// REQUEST
// =========================================================

async function request(path, options = {}, requestOptions = {},) {
  const {
    ignoreUnauthorized = false, skipRefresh = false, retried = false,
  } = requestOptions

  const accessToken = getAccessToken()

  const headers = {
    ...(options.body ? {
      'Content-Type': 'application/json',
    } : {}),

    ...(accessToken ? {
      Authorization: `Bearer ${accessToken}`,
    } : {}),

    ...options.headers,
  }

  const response = await fetch(path, {
    ...options, headers,
  },)

  // =======================================================
  // ACCESS TOKEN EXPIRED
  // =======================================================

  if (response.status === 401 && !ignoreUnauthorized && !skipRefresh && !retried
      && hasRefreshToken()) {
    try {
      await getFreshAccessToken()

      /*
       * Повторяем исходный запрос один раз,
       * уже с новым access token.
       */
      return request(path, options, {
        ...requestOptions, retried: true,
      },)
    } catch (refreshError) {
      clearTokens()

      if (unauthorizedHandler) {
        unauthorizedHandler()
      }

      throw refreshError
    }
  }

  // =======================================================
  // ERROR
  // =======================================================

  if (!response.ok) {
    const error = new Error(`HTTP ${response.status}`,)

    error.status = response.status

    if (response.status === 401 && !ignoreUnauthorized) {
      clearTokens()

      if (unauthorizedHandler) {
        unauthorizedHandler()
      }
    }

    throw error
  }

  // =======================================================
  // RESPONSE
  // =======================================================

  if (response.status === 204) {
    return null
  }

  const text = await response.text()

  if (!text) {
    return null
  }

  return JSON.parse(text)
}

// =========================================================
// AUTH
// =========================================================

export function login(username, password,) {
  return request('/api/auth/login', {
    method: 'POST',

    body: JSON.stringify({
      username, password,
    }),
  }, {
    ignoreUnauthorized: true, skipRefresh: true,
  },)
}

export function getCurrentUser() {
  return request('/api/auth/me',)
}

export async function logout() {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    return null
  }

  return request('/api/auth/logout', {
    method: 'POST',

    body: JSON.stringify({
      refreshToken,
    }),
  }, {
    /*
     * Logout не должен пытаться делать refresh.
     */
    skipRefresh: true, ignoreUnauthorized: true,
  },)
}

// =========================================================
// PROJECTS
// =========================================================

export function getProjects() {
  return request('/api/projects')
}

export function getProjectTickets(projectId,) {
  return request(`/api/projects/${projectId}/tickets`,)
}

export function getProjectAnalytics(projectId,) {
  return request(`/api/projects/${projectId}/analytics`,)
}

// =========================================================
// TICKETS
// =========================================================

export function getTicket(ticketId) {
  return request(`/api/tickets/${ticketId}`,)
}

export function getTicketComments(ticketId,) {
  return request(`/api/tickets/${ticketId}/comments`,)
}

export function addTicketComment(ticketId, content, authorId,) {
  return request(`/api/tickets/${ticketId}/comments`, {
    method: 'POST',

    body: JSON.stringify({
      content, authorId,
    }),
  },)
}

export function updateTicket(ticketId, updates,) {
  return request(`/api/tickets/${ticketId}`, {
    method: 'PUT',

    body: JSON.stringify(updates,),
  },)
}

export function changeTicketStatus(ticketId, status,) {
  return request(`/api/tickets/${ticketId}/status`, {
    method: 'PATCH',

    body: JSON.stringify({
      status,
    }),
  },)
}

export function assignTicket(ticketId, assigneeId,) {
  return request(`/api/tickets/${ticketId}/assignee`, {
    method: 'PATCH',

    body: JSON.stringify({
      assigneeId,
    }),
  },)
}

export function deleteTicket(ticketId,) {
  return request(`/api/tickets/${ticketId}`, {
    method: 'DELETE',
  },)
}

export function getAvailableAssignees() {
  return request('/api/ticket-assignees',)
}

export function createTicket(projectId, payload,) {
  return request(`/api/projects/${projectId}/tickets`, {
    method: 'POST',

    body: JSON.stringify(payload,),
  },)
}

// =========================================================
// ADMIN USERS
// =========================================================

export function getAdminUsers() {
  return request('/api/admin/users',)
}

export function createAdminUser(payload,) {
  return request('/api/admin/users', {
    method: 'POST',

    body: JSON.stringify(payload,),
  },)
}

export function updateAdminUser(userId, payload,) {
  return request(`/api/admin/users/${userId}`, {
    method: 'PUT',

    body: JSON.stringify(payload,),
  },)
}

export function deleteAdminUser(userId,) {
  return request(`/api/admin/users/${userId}`, {
    method: 'DELETE',
  },)
}

// =========================================================
// ADMIN PROJECTS
// =========================================================

export function getAdminProjects() {
  return request('/api/admin/projects',)
}

export function createAdminProject(payload,) {
  return request('/api/admin/projects', {
    method: 'POST',

    body: JSON.stringify(payload,),
  },)
}

export function updateAdminProject(projectId, payload,) {
  return request(`/api/admin/projects/${projectId}`, {
    method: 'PUT',

    body: JSON.stringify(payload,),
  },)
}

export function deleteAdminProject(projectId,) {
  return request(`/api/admin/projects/${projectId}`, {
    method: 'DELETE',
  },)
}