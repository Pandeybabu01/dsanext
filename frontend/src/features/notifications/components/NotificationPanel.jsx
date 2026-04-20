import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import clsx from 'clsx'
import {
  fetchNotifications, markNotificationRead, markAllNotificationsRead,
  clearReadNotifications, fetchUnreadCount,
  selectNotifications, selectUnreadCount, selectNotificationsLoading,
} from '../notificationSlice'
import { usePagination } from '../../../hooks/usePagination'
import Spinner   from '../../../components/ui/Spinner'
import Button    from '../../../components/ui/Button'
import Pagination from '../../../components/ui/Pagination'
import { formatRelative } from '../../../lib/formatters'

const typeIcon = { INFO: 'ℹ️', SUCCESS: '✅', WARNING: '⚠️', SYSTEM: '🔧' }

export default function NotificationPanel() {
  const dispatch    = useDispatch()
  const items       = useSelector(selectNotifications)
  const unread      = useSelector(selectUnreadCount)
  const loading     = useSelector(selectNotificationsLoading)
  const { page, size, goToPage } = usePagination(20)

  useEffect(() => {
    dispatch(fetchNotifications({ page, size }))
    dispatch(fetchUnreadCount())
  }, [dispatch, page, size])

  const handleMarkRead    = (id) => dispatch(markNotificationRead(id))
  const handleMarkAllRead = ()   => dispatch(markAllNotificationsRead())
  const handleClearRead   = ()   => dispatch(clearReadNotifications())

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Notifications</h1>
          {unread > 0 && (
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">
              {unread} unread notification{unread !== 1 ? 's' : ''}
            </p>
          )}
        </div>
        <div className="flex gap-2">
          {unread > 0 && (
            <Button variant="outline" size="sm" onClick={handleMarkAllRead}>
              Mark all read
            </Button>
          )}
          <Button variant="ghost" size="sm" onClick={handleClearRead} className="text-red-500">
            Clear read
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : items.length === 0 ? (
        <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
          <p className="text-4xl mb-3">🔔</p>
          <p className="text-slate-500 dark:text-slate-400">You're all caught up!</p>
        </div>
      ) : (
        <div className="space-y-2">
          {items.map(n => (
            <div
              key={n.id}
              className={clsx(
                'flex items-start gap-4 p-4 rounded-xl border transition-all',
                n.isRead
                  ? 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 opacity-70'
                  : 'bg-brand-50 dark:bg-brand-900/10 border-brand-200 dark:border-brand-800'
              )}
            >
              <span className="text-xl shrink-0 mt-0.5">{typeIcon[n.type] ?? 'ℹ️'}</span>
              <div className="flex-1 min-w-0">
                <p className={clsx('font-semibold text-sm', n.isRead ? 'text-slate-600 dark:text-slate-400' : 'text-slate-900 dark:text-slate-100')}>
                  {n.title}
                </p>
                <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">{n.message}</p>
                <p className="text-xs text-slate-400 dark:text-slate-500 mt-1">{formatRelative(n.createdAt)}</p>
              </div>
              {!n.isRead && (
                <button
                  onClick={() => handleMarkRead(n.id)}
                  className="shrink-0 text-xs text-brand-600 dark:text-brand-400 hover:underline"
                >
                  Mark read
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
