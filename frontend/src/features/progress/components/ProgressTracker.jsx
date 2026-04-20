import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import clsx from 'clsx'
import { fetchProgress, selectProgress, selectProgressLoading } from '../progressSlice'
import { usePagination } from '../../../hooks/usePagination'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import Pagination      from '../../../components/ui/Pagination'
import Spinner         from '../../../components/ui/Spinner'
import { getProgressBg, formatRelative } from '../../../lib/formatters'
import { PROGRESS_OPTIONS } from '../../../lib/constants'
import { useState } from 'react'

export default function ProgressTracker() {
  const dispatch  = useDispatch()
  const items     = useSelector(selectProgress)
  const loading   = useSelector(selectProgressLoading)
  const [statusFilter, setStatusFilter] = useState('')
  const { page, size, goToPage } = usePagination(20)

  useEffect(() => {
    dispatch(fetchProgress({ status: statusFilter || undefined, page, size }))
  }, [dispatch, statusFilter, page, size])

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">My Progress</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm mt-0.5">Track your DSA journey</p>
        </div>
        <select
          value={statusFilter}
          onChange={e => { setStatusFilter(e.target.value); goToPage(0) }}
          className="input w-full sm:w-44 cursor-pointer self-start"
        >
          {PROGRESS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : items.length === 0 ? (
        <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
          <p className="text-4xl mb-3">📈</p>
          <p className="text-slate-600 dark:text-slate-400 font-medium">No progress tracked yet</p>
          <Link to="/problems" className="mt-3 inline-block text-brand-600 dark:text-brand-400 hover:underline text-sm">
            Start solving problems →
          </Link>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/80">
              <tr>
                {['Problem','Topic','Difficulty','Status','Attempts','Last Activity'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
              {items.map(item => (
                <tr key={item.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <td className="px-4 py-3">
                    <Link to={`/problems/${item.problemSlug}`}
                      className="font-medium text-slate-800 dark:text-slate-200 hover:text-brand-600 dark:hover:text-brand-400 transition-colors">
                      {item.problemTitle}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-slate-500 dark:text-slate-400 text-xs">{item.problemTopic}</td>
                  <td className="px-4 py-3"><DifficultyBadge difficulty={item.problemDifficulty} size="sm" /></td>
                  <td className="px-4 py-3">
                    <span className={clsx('px-2 py-0.5 rounded-full text-xs font-medium', getProgressBg(item.status))}>
                      {item.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-500 dark:text-slate-400 text-center">{item.attemptCount}</td>
                  <td className="px-4 py-3 text-slate-400 dark:text-slate-500 text-xs">{formatRelative(item.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
