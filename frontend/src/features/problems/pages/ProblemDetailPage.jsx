import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import clsx from 'clsx'
import { fetchProblemBySlug, clearCurrentProblem, selectCurrentProblem, selectProblemsLoading } from '../problemSlice'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import Button          from '../../../components/ui/Button'
import Spinner         from '../../../components/ui/Spinner'
import api             from '../../../lib/axios'
import { useToast }    from '../../../hooks/useToast'
import { PROGRESS_OPTIONS } from '../../../lib/constants'
import { getProgressBg, formatRelative } from '../../../lib/formatters'

export default function ProblemDetailPage() {
  const { slug }   = useParams()
  const dispatch   = useDispatch()
  const problem    = useSelector(selectCurrentProblem)
  const loading    = useSelector(selectProblemsLoading)
  const toast      = useToast()

  const [note,       setNote]       = useState('')
  const [noteLoaded, setNoteLoaded] = useState(false)
  const [savingNote, setSavingNote] = useState(false)
  const [status,     setStatus]     = useState('')
  const [bookmarked, setBookmarked] = useState(false)
  const [savingProg, setSavingProg] = useState(false)

  useEffect(() => {
    dispatch(fetchProblemBySlug(slug))
    return () => dispatch(clearCurrentProblem())
  }, [dispatch, slug])

  useEffect(() => {
    if (!problem) return
    setStatus(problem.userProgressStatus || '')
    setBookmarked(problem.isBookmarked   || false)

    // Load existing note
    api.get(`/notes/problem/${problem.id}`)
      .then(r => { setNote(r.data.data?.content || ''); setNoteLoaded(true) })
      .catch(() => setNoteLoaded(true))
  }, [problem?.id])

  const handleStatusChange = async (newStatus) => {
    setSavingProg(true)
    try {
      await api.put(`/progress/${problem.id}`, { status: newStatus })
      setStatus(newStatus)
      toast.success(`Marked as ${newStatus.replace(/_/g, ' ')}`)
    } catch {
      toast.error('Failed to update progress')
    } finally {
      setSavingProg(false)
    }
  }

  const handleBookmarkToggle = async () => {
    try {
      const res = await api.post(`/bookmarks/${problem.id}/toggle`)
      const isNow = res.data.data.bookmarked
      setBookmarked(isNow)
      toast.success(isNow ? 'Bookmarked!' : 'Bookmark removed')
    } catch {
      toast.error('Failed to update bookmark')
    }
  }

  const handleSaveNote = async () => {
    if (!note.trim()) return
    setSavingNote(true)
    try {
      await api.put(`/notes/problem/${problem.id}`, { content: note })
      toast.success('Note saved!')
    } catch {
      toast.error('Failed to save note')
    } finally {
      setSavingNote(false)
    }
  }

  const handleDeleteNote = async () => {
    try {
      await api.delete(`/notes/problem/${problem.id}`)
      setNote('')
      toast.success('Note deleted')
    } catch {
      toast.error('Failed to delete note')
    }
  }

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (!problem) return (
    <div className="text-center py-20">
      <p className="text-4xl mb-3">😕</p>
      <p className="text-slate-500 dark:text-slate-400">Problem not found.</p>
      <Link to="/problems" className="mt-4 inline-block text-brand-600 hover:underline text-sm">← Back to problems</Link>
    </div>
  )

  return (
    <div className="max-w-4xl space-y-6 animate-fade-in">

      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
        <Link to="/problems" className="hover:text-brand-600 dark:hover:text-brand-400 transition-colors">Problems</Link>
        <span>/</span>
        <span className="text-slate-700 dark:text-slate-300 truncate">{problem.title}</span>
      </nav>

      {/* Problem header */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100 leading-tight">
              {problem.title}
            </h1>
            <div className="flex flex-wrap items-center gap-2 mt-3">
              <DifficultyBadge difficulty={problem.difficulty} />
              <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400 font-medium">
                {problem.topic}
              </span>
              {problem.platform?.name && (
                <span className="text-xs text-slate-400 dark:text-slate-500">
                  via {problem.platform.name}
                </span>
              )}
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <Button
              variant={bookmarked ? 'secondary' : 'outline'}
              size="sm"
              onClick={handleBookmarkToggle}
              leftIcon={<span>{bookmarked ? '🔖' : '🔖'}</span>}
            >
              {bookmarked ? 'Saved' : 'Save'}
            </Button>
            {problem.externalUrl && (
              <a href={problem.externalUrl} target="_blank" rel="noreferrer">
                <Button size="sm" rightIcon={<span>↗</span>}>
                  Solve problem
                </Button>
              </a>
            )}
          </div>
        </div>

        {/* Description */}
        {problem.description && (
          <div className="mt-5 pt-5 border-t border-slate-100 dark:border-slate-700">
            <h2 className="text-sm font-semibold text-slate-700 dark:text-slate-300 mb-2 uppercase tracking-wide">
              Description
            </h2>
            <div className="prose prose-sm dark:prose-invert max-w-none">
              <p className="text-slate-600 dark:text-slate-400 whitespace-pre-wrap leading-relaxed">
                {problem.description}
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Progress Tracker */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200">Track your progress</h2>
          {savingProg && <Spinner size="sm" />}
        </div>

        <div className="flex flex-wrap gap-2">
          {PROGRESS_OPTIONS.filter(o => o.value).map(o => (
            <button
              key={o.value}
              onClick={() => handleStatusChange(o.value)}
              disabled={savingProg}
              className={clsx(
                'px-4 py-2 rounded-lg text-sm font-medium border transition-all duration-150 disabled:opacity-50',
                status === o.value
                  ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/20 text-brand-700 dark:text-brand-400 shadow-sm'
                  : 'border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-400 hover:border-brand-300 dark:hover:border-brand-700 hover:bg-slate-50 dark:hover:bg-slate-700/50'
              )}
            >
              {o.label}
            </button>
          ))}
        </div>

        {status && (
          <div className="mt-3 flex items-center gap-2">
            <span className="text-xs text-slate-500 dark:text-slate-400">Current status:</span>
            <span className={clsx('text-xs font-semibold px-2 py-0.5 rounded-full', getProgressBg(status))}>
              {status.replace(/_/g, ' ')}
            </span>
          </div>
        )}
      </div>

      {/* Notes Editor */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200">My notes</h2>
          {note && (
            <Button variant="ghost" size="xs" onClick={handleDeleteNote} className="text-red-500 hover:text-red-700">
              Delete
            </Button>
          )}
        </div>

        <textarea
          value={note}
          onChange={e => setNote(e.target.value)}
          placeholder={noteLoaded
            ? 'Write your approach, complexity analysis, or solution insights here…'
            : 'Loading…'}
          disabled={!noteLoaded}
          rows={8}
          className="input resize-none font-mono text-sm leading-relaxed"
        />

        <div className="flex items-center justify-between mt-3">
          <span className="text-xs text-slate-400 dark:text-slate-500">
            {note.length > 0 ? `${note.length} characters` : 'Supports plain text and code snippets'}
          </span>
          <Button size="sm" loading={savingNote} onClick={handleSaveNote} disabled={!note.trim() || !noteLoaded}>
            Save note
          </Button>
        </div>
      </div>

    </div>
  )
}
