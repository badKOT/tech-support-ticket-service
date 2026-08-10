async function request(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options.body
          ? { 'Content-Type': 'application/json' }
          : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const error = new Error(`HTTP ${response.status}`)
    error.status = response.status
    throw error
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export function login(username, password) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({
      username,
      password,
    }),
  })
}

export function getCurrentUser() {
  return request('/api/auth/me')
}

export function getCsrfToken() {
  return request('/api/auth/csrf')
}

export async function logout() {
  const csrf = await getCsrfToken()

  return request('/api/auth/logout', {
    method: 'POST',
    headers: {
      [csrf.headerName]: csrf.token,
    },
  })
}

export function getProjects() {
  return request('/api/projects')
}

export function getProjectTickets(projectId) {
  return request(
      `/api/projects/${projectId}/tickets`
  )
}

export function getTicket(ticketId) {
  return request(`/api/tickets/${ticketId}`)
}

export function getTicketComments(ticketId) {
  return request(`/api/tickets/${ticketId}/comments`)
}

export async function addTicketComment(ticketId, content, authorId) {
  const csrf = await getCsrfToken()

  return request(`/api/tickets/${ticketId}/comments`, {
    method: 'POST',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      content,
      authorId,
    }),
  })
}