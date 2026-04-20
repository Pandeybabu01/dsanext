import { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import clsx from 'clsx'
import {
  fetchProblems, fetchTopics, setFilters, clearFilters,
  selectProblems, selectProblemsMeta, selectTopics,
  selectProblemsLoading, selectProblemFilters,
} from '../problemSlice'
import { useDebounce }   from '../../../hooks/useDebounce'
import { usePagination } from '../../../hooks/usePagination'
import ProblemFilter     from '../components/ProblemFilter'
import ProblemCard       from '../components/ProblemCard'
import DifficultyBadge   from '../../../components/ui/DifficultyBadge'
import Pagination        from '../../../components/ui/Pagination'
import Spinner           from '../../../components/ui/Spinner'
import Button            from '../../../components/ui/Button'
import { getProgressBg } from '../../../lib/formatters'
import api               from '../../../lib/axios'

export default function ProblemListPage() {
  const dispatch  = useDispatch()
  const problems  = useSelector(selectProblems)
  const meta      = useSelector(selectProblemsMeta)
  const topics    = useSelector(selectTopics)
  const loading   = useSelector(selectProblemsLoading)
  const filters   = useSelector(selectProblemFilters)
  const { page, size, goToPage, reset } = usePagination(20)
  const [platforms, setPlatforms] = useState([])
  const [viewMode,  setViewMode]  = useState('table')
  const debouncedSearch = useDebounce(filters.search, 400)

  useEffect(() => {
    dispatch(fetchTopics())
    api.get('/platforms').then(r => setPlatforms(r.data.data ?? [])).catch(() => {})
  }, [dispatch])

  useEffect(() => {
    dispatch(fetchProblems({
      search:     debouncedSearch    || undefined,
      difficulty: filters.difficulty || undefined,
      topic:      filters.topic      || undefined,
      platformId: filters.platformId || undefined,
      page, size,
    }))
  }, [dispatch, debouncedSearch, filters.difficulty, filters.topic, filters.platformId, page, size])

  const handleReset = useCallback(() => {
    dispatch(clearFilters()); reset()
  }, [dispatch, reset])

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Problems</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm mt-0.5">
            {loading ? 'Loading…' : `${meta.totalElements.toLocaleString()} problems available`}
          </p>
        </div>
        {/* View toggle */}
        <div className="flex items-center gap-1 p-1 bg-slate-100 dark:bg-slate-800 rounded-lg self-start">
          {['table','grid'].map(v => (
            <button key={v} onClick={() => setViewMode(v)}
              className={clsx('px-3 py-1.5 rounded-md text-sm font-medium transition-all capitalize',
                viewMode === v
                  ? 'bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 shadow-sm'
                  : 'text-slate-500 hover:text-slate-700 dark:text-slate-400')}>
              {v === 'table' ? '☰ Table' : '⊞ Grid'}
            </button>
          ))}
        </div>
      </div>

      <ProblemFilter topics={topics} platforms={platforms} onReset={handleReset} />

      {loading && <div className="flex justify-center py-16"><Spinner size="lg" /></div>}

      {!loading && problems.length === 0 && (
        <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
          <p className="text-4xl mb-3">🔍</p>
          <p className="text-slate-600 dark:text-slate-400 font-medium">No problems found</p>
          <p className="text-slate-400 text-sm mt-1">Try adjusting your filters</p>
          <Button variant="outline" size="sm" className="mt-4" onClick={handleReset}>Clear all filters</Button>
        </div>
      )}

      {/* Table view */}
      {!loading && problems.length > 0 && viewMode === 'table' && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/80">
              <tr>
                {['Title','Topic','Difficulty','Status','Platform'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
              {problems.map(p => (
                <tr key={p.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors group">
                  <td className="px-4 py-3">
                    <Link to={`/problems/${p.slug}`}
                      className="font-medium text-slate-800 dark:text-slate-200 group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
                      {p.title}
                    </Link>
                    <div className="flex gap-1 mt-0.5">
                      {p.isBookmarked && <span className="text-xs">🔖</span>}
                      {p.hasNote      && <span className="text-xs">📝</span>}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded-full text-xs bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400">{p.topic}</span>
                  </td>
                  <td className="px-4 py-3"><DifficultyBadge difficulty={p.difficulty} /></td>
                  <td className="px-4 py-3">
                    {p.userProgressStatus
                      ? <span className={clsx('px-2 py-0.5 rounded-full text-xs font-medium', getProgressBg(p.userProgressStatus))}>{p.userProgressStatus.replace(/_/g,' ')}</span>
                      : <span className="text-slate-400 text-xs">—</span>}
                  </td>
                  <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400">{p.platform?.name ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Grid view */}
      {!loading && problems.length > 0 && viewMode === 'grid' && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {problems.map(p => <ProblemCard key={p.id} problem={p} />)}
        </div>
      )}

      {!loading && problems.length > 0 && (
        <Pagination page={meta.currentPage} totalPages={meta.totalPages} totalElements={meta.totalElements} size={size} onPageChange={goToPage} />
      )}
    </div>
  )
}
