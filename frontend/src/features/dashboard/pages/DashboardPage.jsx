import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { selectUser } from '../../auth/authSlice'
import api from '../../../lib/axios'
import Spinner from '../../../components/ui/Spinner'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import { formatRelative } from '../../../lib/formatters'

export default function DashboardPage() {
  const user = useSelector(selectUser)
  const [analytics, setAnalytics] = useState(null)
  const [loading,   setLoading]   = useState(true)

  useEffect(() => {
    api.get('/analytics/me')
      .then(r => setAnalytics(r.data.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return (
    <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  )

  const stats = [
    { label: 'Solved',      value: analytics?.totalSolved     ?? 0, icon: '✅', color: 'text-green-600 dark:text-green-400',  bg: 'bg-green-50  dark:bg-green-900/20'  },
    { label: 'In Progress', value: analytics?.totalInProgress ?? 0, icon: '🔄', color: 'text-blue-600  dark:text-blue-400',   bg: 'bg-blue-50   dark:bg-blue-900/20'   },
    { label: 'Bookmarks',   value: analytics?.totalBookmarks  ?? 0, icon: '🔖', color: 'text-amber-600 dark:text-amber-400',  bg: 'bg-amber-50  dark:bg-amber-900/20'  },
    { label: 'Notes',       value: analytics?.totalNotes      ?? 0, icon: '📝', color: 'text-purple-600 dark:text-purple-400',bg: 'bg-purple-50 dark:bg-purple-900/20' },
  ]

  return (
    <div className="space-y-6">

      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
          Welcome back, {user?.fullName?.split(' ')[0]} 👋
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Here's your DSA progress at a glance.</p>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map(s => (
          <div key={s.label} className={`rounded-xl p-5 border border-slate-200 dark:border-slate-700 ${s.bg}`}>
            <div className="text-2xl mb-2">{s.icon}</div>
            <div className={`text-3xl font-bold ${s.color}`}>{s.value}</div>
            <div className="text-sm text-slate-500 dark:text-slate-400 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Difficulty breakdown */}
      {analytics?.solvedByDifficulty && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Solved by difficulty</h2>
          <div className="flex flex-wrap gap-6">
            {Object.entries(analytics.solvedByDifficulty).map(([diff, count]) => (
              <div key={diff} className="flex items-center gap-2">
                <DifficultyBadge difficulty={diff} />
                <span className="font-bold text-slate-700 dark:text-slate-300">{count}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recently solved */}
      {analytics?.recentlySolved?.length > 0 && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-slate-800 dark:text-slate-200">Recently solved</h2>
            <Link to="/progress" className="text-sm text-brand-600 dark:text-brand-400 hover:underline">View all →</Link>
          </div>
          <div className="space-y-3">
            {analytics.recentlySolved.map(p => (
              <div key={p.id} className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3 min-w-0">
                  <DifficultyBadge difficulty={p.problemDifficulty} size="sm" />
                  <Link
                    to={`/problems/${p.problemSlug}`}
                    className="text-sm text-slate-700 dark:text-slate-300 hover:text-brand-600 dark:hover:text-brand-400 truncate"
                  >
                    {p.problemTitle}
                  </Link>
                </div>
                <span className="text-xs text-slate-400 shrink-0">{formatRelative(p.solvedAt)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quick links */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
        {[
          { to: '/problems',  label: 'Browse Problems', icon: '💻' },
          { to: '/analytics', label: 'View Analytics',  icon: '📈' },
          { to: '/notes',     label: 'My Notes',        icon: '📝' },
        ].map(l => (
          <Link
            key={l.to}
            to={l.to}
            className="flex items-center gap-3 p-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-brand-300 dark:hover:border-brand-600 hover:shadow-card-hover transition-all"
          >
            <span className="text-xl">{l.icon}</span>
            <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{l.label}</span>
          </Link>
        ))}
      </div>

    </div>
  )
}
