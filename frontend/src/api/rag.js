import { request } from './client'

export const ragApi = {
  listChunks(token) {
    return request('/api/rag/chunks', { token })
  },
  retrieve(token, keywords, limit = 4) {
    return request('/api/rag/retrieve', {
      method: 'POST',
      token,
      body: { keywords, limit }
    })
  }
}
