import { useEffect, useState, useCallback } from 'react'
import clsx from 'clsx'
import api           from '../../../lib/axios'
import Button        from '../../../components/ui/Button'
import Input         from '../../../components/ui/Input'
import Modal         from '../../../components/ui/Modal'
import Pagination    from '../../../components/ui/Pagination'
import Spinner       from '../../../components/ui/Spinner'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import ProblemForm   from '../components/ProblemForm'
import { useToast }     from '../../../hooks/useToast'
import { useDebounce }  from '../../../hooks/useDebounce'
import { usePagination } from '../../../hooks/usePagination'
import { DIFFICULTY_OPTIONS } from '../../../lib/constants'
import { formatDate } from '../../../lib/formatters'

export default function AdminProblemsPage() {
  const toast = useToast()
  const { page, size, goToPage, reset } = usePagination(20)

  const [problems,    setProblems]    = useState([])
  const [total,       setTotal]       = useState(0)
  const [totalPages,  setTotalPages]  = useState(0)
  const [loading,     setLoading]     = useState(true)
  const [search,      setSearch]      = useState('')
  const [difficulty,  setDifficulty]  = useState('')
  const [activeFilter,setActiveFilter]= useState('')
  const [formOpen,    setFormOpen]    = useState(false)
  const [editTarget,  setEditTarget]  = useState(null)
  const [deleteTarget,setDeleteTarget]= useState(null)
  const [deleting,    setDeleting]    = useState(false)

  const debouncedSearch = useDebounce(search, 400)

  const loadProblems = useCallback(async () => {
    setLoading(true)
    try {
      const params = {
        page, size,
        search:     debouncedSearch || undefined,
        difficulty: difficulty      || undefined,
        active:     activeFilter !== '' ? activeFilter : undefined,
        sortBy: 'createdAt', direction: 'desc',
      }
      const res = await api.get('/admin/problems', { params })
      const d   = res.data.data
      setProblems(d.content); setTotal(d.totalElements); setTotalPages(d.totalPages)
    } catch { toast.error('Failed to load problems') }
    finally { setLoading(false) }
  }, [page, size, debouncedSearch, difficulty, activeFilter])

  useEffect(() => { loadProblems() }, [loadProblems])

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await api.delete(`/admin/problems/${deleteTarget.id}`)
      toast.success('Problem deleted')
      setDeleteTarget(null)
      loadProblems()
    } catch (e) { toast.error(e?.response?.data?.message || 'Delete failed') }
    finally { setDeleting(false) }
  }

  const openCreate = () => { setEditTarget(null); setFormOpen(true) }
  const openEdit   = (p)  => { setEditTarget(p);   setFormOpen(true) }

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Problem Management</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">{total} problems total</p>
        </div>
        <Button onClick={openCreate} leftIcon={<span>➕</span>}>Add problem</Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <Input placeholder="Search problems…" value={search}
            onChange={e => { setSearch(e.target.value); reset() }} leftIcon={<span>🔍</span>} />
        </div>
        <select value={difficulty} onChange={e => { setDifficulty(e.target.value); reset() }} className="input w-full sm:w-44">
          {DIFFICULTY_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <select value={activeFilter} onChange={e => { setActiveFilter(e.target.value); reset() }} className="input w-full sm:w-36">
          <option value="">All statuses</option>
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : problems.length === 0 ? (
          <div className="text-center py-16 text-slate-400">No problems found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 dark:bg-slate-800/80">
                <tr>
                  {['Title','Topic','Difficulty','Platform','Status','Created','Actions'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                {problems.map(p => (
                  <tr key={p.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="px-4 py-3 max-w-xs">
                      <p className="font-medium text-slate-800 dark:text-slate-200 truncate">{p.title}</p>
                      {p.externalUrl && (
                        <a href={p.externalUrl} target="_blank" rel="noreferrer"
                          className="text-xs text-brand-500 hover:underline truncate block">↗ Open</a>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span className="px-2 py-0.5 rounded-full text-xs bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300">{p.topic}</span>
                    </td>
                    <td className="px-4 py-3"><DifficultyBadge difficulty={p.difficulty} /></td>
                    <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400">{p.platform?.name ?? '—'}</td>
                    <td className="px-4 py-3">
                      <span className={clsx('px-2 py-0.5 rounded-full text-xs font-semibold',
                        p.isActive
                          ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                          : 'bg-slate-100  text-slate-500 dark:bg-slate-700   dark:text-slate-400')}>
                        {p.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-400 whitespace-nowrap">{formatDate(p.createdAt)}</td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1">
                        <Button variant="outline" size="xs" onClick={() => openEdit(p)}>✏️ Edit</Button>
                        <Button variant="danger"  size="xs" onClick={() => setDeleteTarget(p)}>🗑</Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Pagination page={page} totalPages={totalPages} totalElements={total} size={size} onPageChange={goToPage} />

      {/* Create/Edit form modal */}
      <ProblemForm
        isOpen={formOpen}
        onClose={() => setFormOpen(false)}
        onSuccess={loadProblems}
        problem={editTarget}
      />

      {/* Delete confirm */}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)}
        title="Delete problem" size="sm" showFooter
        confirmLabel="Delete" confirmVariant="danger"
        onConfirm={handleDelete} confirmLoading={deleting} cancelLabel="Cancel">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Permanently delete <strong className="text-slate-800 dark:text-slate-200">"{deleteTarget?.title}"</strong>?
          All progress and notes for this problem will also be deleted. This cannot be undone.
        </p>
      </Modal>
    </div>
  )
}
