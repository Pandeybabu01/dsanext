import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchUnreadCount, selectUnreadCount } from '../notificationSlice'

/**
 * Compact notification bell used inside Navbar.
 * Polls unread count every 60s and shows badge for unread count.
 */
export default function NotificationBell() {
  const dispatch = useDispatch()
  const unread   = useSelector(selectUnreadCount)

  useEffect(() => {
    dispatch(fetchUnreadCount())
    const id = setInterval(() => dispatch(fetchUnreadCount()), 60_000)
    return () => clearInterval(id)
  }, [dispatch])

  return (
    <Link
      to="/notifications"
      className="relative p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
      aria-label={`Notifications${unread > 0 ? ` — ${unread} unread` : ''}`}
    >
      🔔
      {unread > 0 && (
        <span className="absolute top-1 right-1 min-w-[16px] h-4 px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center leading-none">
          {unread > 99 ? '99+' : unread}
        </span>
      )}
    </Link>
  )
}
