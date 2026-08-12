import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem('zivro_token'))
  const [loading, setLoading] = useState(!!localStorage.getItem('zivro_token'))

  const applyToken = useCallback((t) => {
    if (t) {
      localStorage.setItem('zivro_token', t)
      api.defaults.headers.common.Authorization = `Bearer ${t}`
      setToken(t)
    } else {
      localStorage.removeItem('zivro_token')
      delete api.defaults.headers.common.Authorization
      setToken(null)
      setUser(null)
    }
  }, [])

  useEffect(() => {
    if (!token) {
      setLoading(false)
      return
    }
    api.defaults.headers.common.Authorization = `Bearer ${token}`
    let cancelled = false
    api
      .get('/api/auth/me')
      .then((res) => {
        if (!cancelled) setUser(res.data)
      })
      .catch(() => {
        if (!cancelled) applyToken(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [token, applyToken])

  const login = useCallback(
    async (email, password) => {
      const { data } = await api.post('/api/auth/login', { email, password })
      applyToken(data.accessToken)
      setUser(data.user)
      return data
    },
    [applyToken],
  )

  const register = useCallback(
    async (payload) => {
      const { data } = await api.post('/api/auth/register', payload)
      applyToken(data.accessToken)
      setUser(data.user)
      return data
    },
    [applyToken],
  )

  const loginWithGoogle = useCallback(
    async (credential, additionalData = {}) => {
      const { data } = await api.post('/api/auth/google', { credential, ...additionalData })
      applyToken(data.accessToken)
      setUser(data.user)
      return data
    },
    [applyToken],
  )

  const logout = useCallback(() => {
    applyToken(null)
  }, [applyToken])

  const value = useMemo(
    () => ({ user, token, loading, login, register, loginWithGoogle, logout }),
    [user, token, loading, login, register, loginWithGoogle, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
