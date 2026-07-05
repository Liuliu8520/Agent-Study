import { request } from './client'

export const agentApi = {
  listPrompts() {
    return request('/api/agent/prompts')
  },
  upsertPrompt(token, code, body) {
    return request(`/api/agent/prompts/${encodeURIComponent(code)}`, {
      method: 'PUT',
      token,
      body
    })
  },
  listCallLogs(params = {}) {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') query.set(key, value)
    })
    return request(`/api/agent/call-logs?${query.toString()}`)
  }
}
