import { Navigate, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { selectIsAuth, selectToken } from '../../features/auth/authSlice'

/**
 * Guards any route that requires authentication.
 * Unauthenticated users are redirected to /login with the original
 * destination stored in location.state so they can be sent back after login.
 */
export default function ProtectedRoute({ children }) {
  const isAuth   = useSelector(selectIsAuth)
  const location = useLocation()

  if (!isAuth) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}
