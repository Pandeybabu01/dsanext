import { useSelector, useDispatch } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import {
  selectUser, selectToken, selectIsAuth, selectIsAdmin,
  selectAuthLoading, selectAuthError,
  loginUser, registerUser, logout, clearError,
} from '../features/auth/authSlice'

/**
 * Primary auth hook — exposes user state and auth actions.
 * Usage: const { user, isAdmin, login, logout } = useAuth()
 */
export function useAuth() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()

  const user      = useSelector(selectUser)
  const token     = useSelector(selectToken)
  const isAuth    = useSelector(selectIsAuth)
  const isAdmin   = useSelector(selectIsAdmin)
  const isLoading = useSelector(selectAuthLoading)
  const error     = useSelector(selectAuthError)

  const login = async (credentials) => {
    const result = await dispatch(loginUser(credentials))
    if (loginUser.fulfilled.match(result)) {
      const role = result.payload.user?.role
      navigate(role === 'ADMIN' ? '/admin' : '/dashboard', { replace: true })
      return true
    }
    return false
  }

  const register = async (userData) => {
    const result = await dispatch(registerUser(userData))
    if (registerUser.fulfilled.match(result)) {
      navigate('/dashboard', { replace: true })
      return true
    }
    return false
  }

  const logoutUser = () => {
    dispatch(logout())
    navigate('/login', { replace: true })
  }

  const dismissError = () => dispatch(clearError())

  return {
    user, token, isAuth, isAdmin,
    isLoading, error,
    login, register,
    logout: logoutUser,
    dismissError,
  }
}
