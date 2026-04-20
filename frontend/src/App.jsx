import { useEffect } from 'react'
import { useSelector } from 'react-redux'
import AppRouter from './routes/AppRouter'
import { selectTheme } from './features/auth/authSlice'

/**
 * Root application component.
 * Manages the dark/light theme class on <html> based on Redux state.
 * All routing is delegated to AppRouter.
 */
export default function App() {
  const theme = useSelector(selectTheme)

  // Apply theme class to <html> element
  useEffect(() => {
    const root = document.documentElement
    if (theme === 'dark') {
      root.classList.add('dark')
    } else if (theme === 'light') {
      root.classList.remove('dark')
    } else {
      // 'system' — follow OS preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      root.classList.toggle('dark', prefersDark)
    }
  }, [theme])

  return <AppRouter />
}
