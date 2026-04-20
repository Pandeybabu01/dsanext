import { useDispatch, useSelector } from 'react-redux'
import { setFilters, clearFilters, selectProblemFilters } from '../problemSlice'
import Input  from '../../../components/ui/Input'
import Button from '../../../components/ui/Button'
import { DIFFICULTY_OPTIONS } from '../../../lib/constants'

/**
 * ProblemFilter — filter bar rendered above the problem list table.
 * Syncs all filter state into Redux; parent reads from Redux to trigger fetches.
 */
export default function ProblemFilter({ topics = [], platforms = [], onReset }) {
  const dispatch = useDispatch()
  const filters  = useSelector(selectProblemFilters)

  const update = (key, value) => dispatch(setFilters({ [key]: value }))

  const handleReset = () => {
    dispatch(clearFilters())
    onReset?.()
  }

  const hasActiveFilters =
    filters.search || filters.difficulty || filters.topic || filters.platformId

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-4">
      <div className="flex flex-col sm:flex-row gap-3">

        {/* Search */}
        <div className="flex-1">
          <Input
            placeholder="Search by title or topic…"
            value={filters.search}
            onChange={e => update('search', e.target.value)}
            leftIcon={<span className="text-slate-400">🔍</span>}
          />
        </div>

        {/* Difficulty */}
        <select
          value={filters.difficulty}
          onChange={e => update('difficulty', e.target.value)}
          className="input w-full sm:w-44 cursor-pointer"
        >
          {DIFFICULTY_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        {/* Topic */}
        <select
          value={filters.topic}
          onChange={e => update('topic', e.target.value)}
          className="input w-full sm:w-44 cursor-pointer"
        >
          <option value="">All Topics</option>
          {topics.map(t => <option key={t} value={t}>{t}</option>)}
        </select>

        {/* Platform */}
        {platforms.length > 0 && (
          <select
            value={filters.platformId}
            onChange={e => update('platformId', e.target.value)}
            className="input w-full sm:w-44 cursor-pointer"
          >
            <option value="">All Platforms</option>
            {platforms.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        )}

        {/* Reset */}
        {hasActiveFilters && (
          <Button variant="ghost" size="md" onClick={handleReset} className="shrink-0">
            ✕ Clear
          </Button>
        )}
      </div>
    </div>
  )
}
