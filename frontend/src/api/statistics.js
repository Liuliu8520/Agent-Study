import { request } from './client'

export const statisticsApi = {
  dashboard() {
    return request('/api/statistics/dashboard')
  }
}
