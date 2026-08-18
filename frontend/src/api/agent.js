import { request } from './client'

export const agentApi = {
  listPrompts(token) {
    return request('/api/agent/prompts', { token })
  },
  upsertPrompt(token, code, body) {
    return request(`/api/agent/prompts/${encodeURIComponent(code)}`, {
      method: 'PUT',
      token,
      body
    })
  },
  listCallLogs(token, params = {}) {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') query.set(key, value)
    })
    const queryString = query.toString()
    return request(queryString ? `/api/agent/call-logs?${queryString}` : '/api/agent/call-logs', { token })
  }
}
