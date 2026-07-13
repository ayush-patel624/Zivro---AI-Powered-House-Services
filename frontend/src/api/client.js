import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

const existing = localStorage.getItem('zivro_token')
if (existing) {
  api.defaults.headers.common.Authorization = `Bearer ${existing}`
}

export default api
