import axios from 'axios'
import toast from 'react-hot-toast'

/**
 * DSANext Axios instance.
 *
 * Features:
 *  - Base URL from environment variable
 *  - Auto-attach JWT Bearer token from localStorage
 *  - 401 handler: clears auth + redirects to login
 *  - 503 handler: shows maintenance mode message
 *  - Standard error message extraction from ApiResponse envelope
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ── Request Interceptor ───────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('dsanext_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ── Response Interceptor ──────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error

    if (!response) {
      toast.error('Network error. Please check your connection.')
      return Promise.reject(error)
    }

    const status  = response.status
    const message = response.data?.message || 'An unexpected error occurred'

    switch (status) {
      case 401:
        // Clear auth state and redirect to login
        localStorage.removeItem('dsanext_token')
        localStorage.removeItem('dsanext_user')
        // Avoid redirect loop on login page
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login'
        }
        break

      case 403:
        toast.error('You do not have permission to perform this action.')
        break

      case 503:
        // Maintenance mode — handled at app level
        if (response.data?.maintenance) {
          toast.error(message, { duration: 8000 })
        } else {
          toast.error('Service temporarily unavailable.')
        }
        break

      case 429:
        toast.error('Too many requests. Please slow down.')
        break

      case 500:
        toast.error('Server error. Our team has been notified.')
        break

      default:
        // Don't auto-toast for 400/404/409 — let components handle these contextually
        break
    }

    return Promise.reject(error)
  },
)

export default api

// ── Helper: extract error message from ApiResponse ────────
export const getErrorMessage = (error) => {
  if (error?.response?.data?.message) {
    return error.response.data.message
  }
  if (error?.response?.data?.error) {
    return error.response.data.error
  }
  if (error?.message) {
    return error.message
  }
  return 'An unexpected error occurred'
}

// ── Helper: extract field validation errors ───────────────
export const getValidationErrors = (error) => {
  return error?.response?.data?.error || {}
}
