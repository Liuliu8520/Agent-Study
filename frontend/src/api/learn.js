import { request } from './client'

export const learnApi = {
  createSession(studentName) {
    return request('/api/learn/sessions', {
      method: 'POST',
      body: { studentName }
    })
  },
  listSessions(params = {}) {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') query.set(key, value)
    })
    return request(`/api/learn/sessions?${query.toString()}`)
  },
  getSession(sessionId) {
    return request(`/api/learn/sessions/${sessionId}`)
  },
  generateDiagnosis(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/diagnosis/questions`, { method: 'POST' })
  },
  submitDiagnosis(sessionId, answers) {
    return request(`/api/learn/sessions/${sessionId}/diagnosis/submit`, {
      method: 'POST',
      body: { answers }
    })
  },
  generatePlan(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/plan`, { method: 'POST' })
  },
  generateLesson(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/lesson`, { method: 'POST' })
  },
  generateExercises(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/exercises`, { method: 'POST' })
  },
  submitExercises(sessionId, answers) {
    return request(`/api/learn/sessions/${sessionId}/exercises/submit`, {
      method: 'POST',
      body: { answers }
    })
  },
  listExerciseAttempts(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/exercise-attempts`)
  },
  generateReview(sessionId) {
    return request(`/api/learn/sessions/${sessionId}/review`, { method: 'POST' })
  }
}
