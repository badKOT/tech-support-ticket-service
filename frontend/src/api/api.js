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

  const text = await response.text()

  if (!text) {
    return null
  }
  return JSON.parse(text)
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

export async function updateTicket(ticketId, updates) {
  const csrf = await getCsrfToken()

  return request(`/api/tickets/${ticketId}`, {
    method: 'PUT',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(updates),
  })
}

export async function changeTicketStatus(ticketId, status) {
  const csrf = await getCsrfToken()

  return request(`/api/tickets/${ticketId}/status`, {
    method: 'PATCH',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      status,
    }),
  })
}

export async function assignTicket(ticketId, assigneeId) {
  const csrf = await getCsrfToken()

  return request(`/api/tickets/${ticketId}/assignee`, {
    method: 'PATCH',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      assigneeId,
    }),
  })
}

export async function deleteTicket(ticketId) {
  const csrf = await getCsrfToken()

  return request(`/api/tickets/${ticketId}`, {
    method: 'DELETE',
    headers: {
      [csrf.headerName]: csrf.token,
    },
  })
}

export function getAvailableAssignees() {
  return request('/api/ticket-assignees')
}

export async function createTicket(projectId, payload) {
  const csrf = await getCsrfToken()

  return request(`/api/projects/${projectId}/tickets`, {
    method: 'POST',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(payload),
  })
}

export function getAdminUsers() {
  return request('/api/admin/users')
}

export async function createAdminUser(payload) {
  const csrf = await getCsrfToken()

  return request('/api/admin/users', {
    method: 'POST',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(payload),
  })
}

export async function updateAdminUser(userId, payload) {
  const csrf = await getCsrfToken()

  return request(`/api/admin/users/${userId}`, {
    method: 'PUT',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(payload),
  })
}

export async function deleteAdminUser(userId) {
  const csrf = await getCsrfToken()

  return request(`/api/admin/users/${userId}`, {
    method: 'DELETE',
    headers: {
      [csrf.headerName]: csrf.token,
    },
  })
}