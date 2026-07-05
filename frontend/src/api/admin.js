import { request } from './client'

export const adminApi = {
  login(username, password) {
    return request('/api/admin/auth/login', {
      method: 'POST',
      body: { username, password }
    })
  },
  me(token) {
    return request('/api/admin/me', { token })
  },
  listOperationLogs(token, params = {}) {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') query.set(key, value)
    })
    return request(`/api/admin/operation-logs?${query.toString()}`, { token })
  },
  listPromptVersions(token, code) {
    return request(`/api/admin/prompts/${encodeURIComponent(code)}/versions`, { token })
  },
  activatePromptVersion(token, code, versionId) {
    return request(`/api/admin/prompts/${encodeURIComponent(code)}/versions/${versionId}/activate`, {
      method: 'POST',
      token
    })
  },
  createChunk(token, body) {
    return request('/api/admin/rag/chunks', { method: 'POST', token, body })
  },
  updateChunk(token, chunkId, body) {
    return request(`/api/admin/rag/chunks/${encodeURIComponent(chunkId)}`, {
      method: 'PUT',
      token,
      body
    })
  },
  deleteChunk(token, chunkId) {
    return request(`/api/admin/rag/chunks/${encodeURIComponent(chunkId)}`, {
      method: 'DELETE',
      token
    })
  },
  rebuildEmbedding(token, chunkId) {
    return request(`/api/admin/rag/chunks/${encodeURIComponent(chunkId)}/embedding`, {
      method: 'POST',
      token
    })
  }
}
