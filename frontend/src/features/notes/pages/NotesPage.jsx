import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import { fetchNotes, selectNotes, selectNotesLoading } from '../noteSlice'
import { usePagination } from '../../../hooks/usePagination'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import Pagination from '../../../components/ui/Pagination'
import Spinner from '../../../components/ui/Spinner'
import { formatRelative } from '../../../lib/formatters'

export default function NotesPage() {
  const dispatch = useDispatch()
  const notes    = useSelector(selectNotes)
  const loading  = useSelector(selectNotesLoading)
  const { page, size, goToPage } = usePagination(20)

  useEffect(() => { dispatch(fetchNotes({ page, size })) }, [dispatch, page, size])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">My Notes</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">Your notes across all problems</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : notes.length === 0 ? (
        <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
          <p className="text-4xl mb-3">📝</p>
          <p className="text-slate-500 dark:text-slate-400">No notes yet. Start solving problems and add notes!</p>
          <Link to="/problems" className="mt-4 inline-block text-brand-600 dark:text-brand-400 hover:underline text-sm">Browse problems →</Link>
        </div>
      ) : (
        <div className="grid gap-4">
          {notes.map(n => (
            <div key={n.id} className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5 hover:shadow-card-hover transition-all">
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-2 min-w-0">
                  <DifficultyBadge difficulty={n.problemDifficulty} size="sm" />
                  <Link to={`/problems/${n.problemSlug}`} className="font-semibold text-slate-800 dark:text-slate-200 hover:text-brand-600 dark:hover:text-brand-400 truncate">
                    {n.problemTitle}
                  </Link>
                </div>
                <span className="text-xs text-slate-400 shrink-0">{formatRelative(n.updatedAt)}</span>
              </div>
              <p className="mt-3 text-sm text-slate-600 dark:text-slate-400 line-clamp-3 font-mono leading-relaxed">{n.content}</p>
              <Link to={`/problems/${n.problemSlug}`} className="mt-3 text-xs text-brand-600 dark:text-brand-400 hover:underline inline-block">
                Edit note →
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
