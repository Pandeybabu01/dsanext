import { useState, useEffect } from 'react'
import { Outlet, NavLink, useLocation, Link } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import clsx from 'clsx'
import Navbar from './Navbar'
import { fetchUnreadCount } from '../../features/notifications/notificationSlice'

const adminNavItems = [
  { to: '/admin',             label: 'Dashboard',  icon: '📊', end: true },
  { to: '/admin/users',       label: 'Users',      icon: '👥' },
  { to: '/admin/problems',    label: 'Problems',   icon: '💻' },
  { to: '/admin/platforms',   label: 'Platforms',  icon: '🌐' },
  { to: '/admin/settings',    label: 'Settings',   icon: '⚙️' },
  { to: '/admin/logs',        label: 'Logs',       icon: '📋' },
]

function AdminSidebar({ isOpen, onClose }) {
  return (
    <>
      {isOpen && (
        <div className="fixed inset-0 bg-black/40 z-20 lg:hidden" onClick={onClose} />
      )}
      <aside className={clsx(
        'fixed top-16 left-0 bottom-0 w-64 z-20',
        'bg-white dark:bg-slate-900',
        'border-r border-slate-200 dark:border-slate-700',
        'flex flex-col transition-transform duration-300 lg:translate-x-0',
        isOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        {/* Admin badge */}
        <div className="px-4 py-3 border-b border-slate-100 dark:border-slate-700">
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-400">
            🛡 Admin Panel
          </span>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {adminNavItems.map(({ to, label, icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={onClose}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                isActive
                  ? 'bg-brand-50 dark:bg-brand-900/20 text-brand-700 dark:text-brand-400'
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100'
              )}
            >
              <span className="text-base w-5 flex justify-center">{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Back to user view */}
        <div className="px-3 py-3 border-t border-slate-100 dark:border-slate-700">
          <Link
            to="/dashboard"
            className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            ← Back to User View
          </Link>
        </div>
      </aside>
    </>
  )
}

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()
  const dispatch = useDispatch()

  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  useEffect(() => {
    dispatch(fetchUnreadCount())
  }, [dispatch])

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col">
      <Navbar onMenuToggle={() => setSidebarOpen(o => !o)} />

      <div className="flex flex-1 relative">
        <AdminSidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        <main className="flex-1 lg:ml-64 min-h-[calc(100vh-4rem)] overflow-x-hidden">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
