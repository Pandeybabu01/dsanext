import { useDispatch, useSelector } from 'react-redux'
import { setTheme, selectTheme } from '../features/auth/authSlice'

/**
 * Theme management hook.
 * Usage: const { theme, toggleTheme, setTheme } = useTheme()
 */
export function useTheme() {
  const dispatch    = useDispatch()
  const currentTheme = useSelector(selectTheme)

  const changeTheme = (newTheme) => {
    dispatch(setTheme(newTheme))
  }

  const toggleTheme = () => {
    changeTheme(currentTheme === 'dark' ? 'light' : 'dark')
  }

  const isDark = currentTheme === 'dark' ||
    (currentTheme === 'system' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches)

  return { theme: currentTheme, isDark, toggleTheme, setTheme: changeTheme }
}
