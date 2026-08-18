import { request } from './client'

export const statisticsApi = {
  dashboard(token) {
    return request('/api/statistics/dashboard', { token })
  }
}
