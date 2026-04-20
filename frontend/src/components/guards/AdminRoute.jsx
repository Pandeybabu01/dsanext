import { Navigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { selectIsAuth, selectIsAdmin } from '../../features/auth/authSlice'

/**
 * Guards routes that require the ADMIN role.
 * - Unauthenticated → /login
 * - Authenticated but not admin → /dashboard (403-like redirect)
 */
export default function AdminRoute({ children }) {
  const isAuth  = useSelector(selectIsAuth)
  const isAdmin = useSelector(selectIsAdmin)

  if (!isAuth)  return <Navigate to="/login"     replace />
  if (!isAdmin) return <Navigate to="/dashboard" replace />

  return children
}
