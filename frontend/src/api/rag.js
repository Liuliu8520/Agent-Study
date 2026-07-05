import { request } from './client'

export const ragApi = {
  listChunks() {
    return request('/api/rag/chunks')
  },
  retrieve(keywords, limit = 4) {
    return request('/api/rag/retrieve', {
      method: 'POST',
      body: { keywords, limit }
    })
  }
}
