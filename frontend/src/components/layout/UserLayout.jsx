import { useState, useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import Navbar  from './Navbar'
import Sidebar from './Sidebar'
import { fetchUnreadCount } from '../../features/notifications/notificationSlice'

/**
 * UserLayout — shell for all authenticated user pages.
 * Renders: Navbar (top) + Sidebar (left) + page content (Outlet).
 * Sidebar auto-closes on route change on mobile.
 */
export default function UserLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()
  const dispatch = useDispatch()

  // Close sidebar on route change (mobile)
  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  // Poll unread notification count every 60s
  useEffect(() => {
    dispatch(fetchUnreadCount())
    const interval = setInterval(() => dispatch(fetchUnreadCount()), 60_000)
    return () => clearInterval(interval)
  }, [dispatch])

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col">
      <Navbar onMenuToggle={() => setSidebarOpen(o => !o)} />

      <div className="flex flex-1 relative">
        <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        {/* Main content — offset by sidebar width on desktop */}
        <main className="flex-1 lg:ml-64 min-h-[calc(100vh-4rem)] overflow-x-hidden">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
