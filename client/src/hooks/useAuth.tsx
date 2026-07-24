import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'
import { TOKEN_KEY } from '../api/client'
import { login as loginRequest } from '../api/endpoints'

interface AuthState {
  email: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(localStorage.getItem('clearwatch_email'))

  const login = useCallback(async (emailInput: string, password: string) => {
    const result = await loginRequest(emailInput, password)
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem('clearwatch_email', result.email)
    setEmail(result.email)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem('clearwatch_email')
    setEmail(null)
  }, [])

  return (
    <AuthContext.Provider value={{ email, isAuthenticated: !!email, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
