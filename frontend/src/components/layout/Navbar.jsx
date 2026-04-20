import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { selectUser, selectIsAdmin, logout } from '../../features/auth/authSlice'
import { selectUnreadCount } from '../../features/notifications/notificationSlice'
import { useTheme } from '../../hooks/useTheme'

export default function Navbar({ onMenuToggle }) {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const user      = useSelector(selectUser)
  const isAdmin   = useSelector(selectIsAdmin)
  const unread    = useSelector(selectUnreadCount)
  const { isDark, toggleTheme } = useTheme()
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login', { replace: true })
  }

  return (
    <header className="h-16 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-700 flex items-center px-4 lg:px-6 gap-4 sticky top-0 z-30">

      {/* Mobile menu toggle */}
      <button
        onClick={onMenuToggle}
        className="lg:hidden p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800"
        aria-label="Toggle menu"
      >
        ☰
      </button>

      {/* Logo */}
      <Link to="/dashboard" className="flex items-center gap-2 font-bold text-lg text-brand-600 dark:text-brand-400 shrink-0">
        <span className="text-2xl">⚡</span>
        <span className="hidden sm:block">DSANext</span>
      </Link>

      <div className="flex-1" />

      {/* Right section */}
      <div className="flex items-center gap-2">

        {/* Admin badge */}
        {isAdmin && (
          <Link to="/admin" className="hidden sm:flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-400 hover:bg-brand-200 transition-colors">
            🛡 Admin
          </Link>
        )}

        {/* Theme toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label="Toggle theme"
        >
          {isDark ? '☀️' : '🌙'}
        </button>

        {/* Notifications */}
        <Link
          to="/notifications"
          className="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label={`Notifications${unread > 0 ? ` (${unread} unread)` : ''}`}
        >
          🔔
          {unread > 0 && (
            <span className="absolute top-1 right-1 min-w-[16px] h-4 px-1 bg-red-500 text-white text-xs font-bold rounded-full flex items-center justify-center">
              {unread > 99 ? '99+' : unread}
            </span>
          )}
        </Link>

        {/* User menu */}
        <div className="relative">
          <button
            onClick={() => setUserMenuOpen(o => !o)}
            className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <div className="w-8 h-8 rounded-full bg-brand-600 text-white flex items-center justify-center text-sm font-semibold">
              {user?.fullName?.[0]?.toUpperCase() ?? 'U'}
            </div>
            <span className="hidden md:block text-sm font-medium text-slate-700 dark:text-slate-300 max-w-[120px] truncate">
              {user?.fullName}
            </span>
            <span className="text-slate-400 text-xs">▾</span>
          </button>

          {userMenuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setUserMenuOpen(false)} />
              <div className="absolute right-0 top-full mt-2 w-52 bg-white dark:bg-slate-800 rounded-xl shadow-dropdown border border-slate-200 dark:border-slate-700 z-20 py-1 animate-slide-down">
                <div className="px-4 py-2 border-b border-slate-100 dark:border-slate-700">
                  <p className="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate">{user?.fullName}</p>
                  <p className="text-xs text-slate-500 dark:text-slate-400 truncate">{user?.email}</p>
                </div>
                {[
                  { to: '/dashboard', label: '📊 Dashboard' },
                  { to: '/analytics', label: '📈 Analytics' },
                  { to: '/settings',  label: '⚙️ Settings' },
                ].map(item => (
                  <Link
                    key={item.to}
                    to={item.to}
                    onClick={() => setUserMenuOpen(false)}
                    className="flex items-center px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
                  >
                    {item.label}
                  </Link>
                ))}
                <div className="border-t border-slate-100 dark:border-slate-700 mt-1 pt-1">
                  <button
                    onClick={handleLogout}
                    className="w-full text-left flex items-center px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                  >
                    🚪 Sign out
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
