import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import { fetchBookmarks, removeBookmark, selectBookmarks, selectBookmarksLoading } from '../bookmarkSlice'
import { usePagination } from '../../../hooks/usePagination'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import Pagination from '../../../components/ui/Pagination'
import Button from '../../../components/ui/Button'
import Spinner from '../../../components/ui/Spinner'
import { useToast } from '../../../hooks/useToast'

export default function BookmarksPage() {
  const dispatch   = useDispatch()
  const bookmarks  = useSelector(selectBookmarks)
  const loading    = useSelector(selectBookmarksLoading)
  const toast      = useToast()
  const { page, size, goToPage } = usePagination(20)

  useEffect(() => { dispatch(fetchBookmarks({ page, size })) }, [dispatch, page, size])

  const handleRemove = async (problemId) => {
    await dispatch(removeBookmark(problemId))
    toast.success('Bookmark removed')
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Bookmarks</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">Problems you've saved for later</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : bookmarks.length === 0 ? (
        <div className="text-center py-16 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
          <p className="text-4xl mb-3">🔖</p>
          <p className="text-slate-500 dark:text-slate-400">No bookmarks yet.</p>
          <Link to="/problems" className="mt-4 inline-block text-brand-600 dark:text-brand-400 hover:underline text-sm">Browse problems →</Link>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/60">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Problem</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Topic</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Difficulty</th>
                <th className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">Platform</th>
                <th className="px-4 py-3 text-right text-xs font-semibold text-slate-500 uppercase tracking-wider">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
              {bookmarks.map(b => (
                <tr key={b.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <td className="px-4 py-3">
                    <Link to={`/problems/${b.problem?.slug}`} className="font-medium text-slate-800 dark:text-slate-200 hover:text-brand-600 dark:hover:text-brand-400">
                      {b.problem?.title}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{b.problem?.topic}</td>
                  <td className="px-4 py-3"><DifficultyBadge difficulty={b.problem?.difficulty} size="sm" /></td>
                  <td className="px-4 py-3 text-slate-500 dark:text-slate-400 text-xs">{b.problem?.platform?.name ?? '—'}</td>
                  <td className="px-4 py-3 text-right">
                    <Button variant="ghost" size="xs" onClick={() => handleRemove(b.problem?.id)}>Remove</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
