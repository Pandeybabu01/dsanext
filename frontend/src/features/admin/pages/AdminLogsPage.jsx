import { useEffect, useState, useCallback } from 'react'
import api        from '../../../lib/axios'
import Button     from '../../../components/ui/Button'
import Input      from '../../../components/ui/Input'
import Modal      from '../../../components/ui/Modal'
import Pagination from '../../../components/ui/Pagination'
import Spinner    from '../../../components/ui/Spinner'
import { useToast }     from '../../../hooks/useToast'
import { useDebounce }  from '../../../hooks/useDebounce'
import { usePagination } from '../../../hooks/usePagination'
import { formatDateTime } from '../../../lib/formatters'

const ACTION_COLORS = {
  USER_LOGIN:       'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  USER_REGISTERED:  'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  USER_DELETED:     'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  USER_BLOCKED:     'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  USER_UNBLOCKED:   'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  PROBLEM_CREATED:  'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
  PROBLEM_UPDATED:  'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
  PROBLEM_DELETED:  'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  PROGRESS_UPDATED: 'bg-teal-100 text-teal-700 dark:bg-teal-900/30 dark:text-teal-400',
  NOTE_SAVED:       'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300',
  BOOKMARK_ADDED:   'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
}

export default function AdminLogsPage() {
  const toast = useToast()
  const { page, size, goToPage, reset } = usePagination(50)

  const [logs,         setLogs]         = useState([])
  const [total,        setTotal]        = useState(0)
  const [totalPages,   setTotalPages]   = useState(0)
  const [loading,      setLoading]      = useState(true)
  const [actionFilter, setActionFilter] = useState('')
  const [entityFilter, setEntityFilter] = useState('')
  const [purgeModal,   setPurgeModal]   = useState(false)
  const [purging,      setPurging]      = useState(false)

  const debouncedAction = useDebounce(actionFilter, 400)
  const debouncedEntity = useDebounce(entityFilter, 400)

  const loadLogs = useCallback(async () => {
    setLoading(true)
    try {
      const params = {
        page, size,
        action:     debouncedAction || undefined,
        entityType: debouncedEntity || undefined,
      }
      const res = await api.get('/admin/logs', { params })
//          const res = await api.get('/logs/admin', { params })
      const d   = res.data.data
      setLogs(d.content); setTotal(d.totalElements); setTotalPages(d.totalPages)
    } catch { toast.error('Failed to load logs') }
    finally { setLoading(false) }
  }, [page, size, debouncedAction, debouncedEntity])

  useEffect(() => { loadLogs() }, [loadLogs])

  const handlePurge = async () => {
    setPurging(true)
    try {
        const res = await api.delete('/admin/logs/purge', { params: { retentionDays: 90 } })
//          const res = await api.delete('/logs/admin/purge', { params: { retentionDays: 90 } })
      toast.success(`Purged ${res.data.data?.deletedCount ?? 0} old log entries`)
      setPurgeModal(false); loadLogs()
    } catch { toast.error('Purge failed') }
    finally { setPurging(false) }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Activity Logs</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">{total.toLocaleString()} log entries</p>
        </div>
        <Button variant="danger" size="sm" onClick={() => setPurgeModal(true)}>
          🗑 Purge old logs
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <Input placeholder="Filter by action (e.g. USER_LOGIN)…" value={actionFilter}
            onChange={e => { setActionFilter(e.target.value); reset() }} leftIcon={<span>🔍</span>} />
        </div>
        <div className="sm:w-48">
          <Input placeholder="Entity type (e.g. USER)…" value={entityFilter}
            onChange={e => { setEntityFilter(e.target.value); reset() }} />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : logs.length === 0 ? (
          <div className="text-center py-16 text-slate-400">No logs found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 dark:bg-slate-800/80">
                <tr>
                  {['Timestamp','User','Action','Entity','IP Address'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                {logs.map(log => (
                  <tr key={log.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">
                      {formatDateTime(log.createdAt)}
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-600 dark:text-slate-400 max-w-[160px] truncate">
                      {log.userEmail ?? <span className="text-slate-400">system</span>}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${ACTION_COLORS[log.action] ?? 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300'}`}>
                        {log.action}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400">
                      {log.entityType
                        ? <span>{log.entityType}<span className="text-slate-400 ml-1">{log.entityId?.slice(0,8)}…</span></span>
                        : '—'}
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-400 dark:text-slate-500 font-mono">
                      {log.ipAddress ?? '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Pagination page={page} totalPages={totalPages} totalElements={total} size={size} onPageChange={goToPage} />

      <Modal isOpen={purgeModal} onClose={() => setPurgeModal(false)}
        title="Purge old logs" size="sm" showFooter
        confirmLabel="Purge logs" confirmVariant="danger"
        onConfirm={handlePurge} confirmLoading={purging} cancelLabel="Cancel">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Delete all activity logs older than <strong>90 days</strong>? This action cannot be undone.
        </p>
      </Modal>
    </div>
  )
}





//
// import { useEffect, useState, useCallback } from 'react'
// import api        from '../../../lib/axios'
// import Button     from '../../../components/ui/Button'
// import Input      from '../../../components/ui/Input'
// import Modal      from '../../../components/ui/Modal'
// import Pagination from '../../../components/ui/Pagination'
// import Spinner    from '../../../components/ui/Spinner'
// import { useToast }     from '../../../hooks/useToast'
// import { useDebounce }  from '../../../hooks/useDebounce'
// import { usePagination } from '../../../hooks/usePagination'
// import { formatDateTime } from '../../../lib/formatters'
//
// const ACTION_COLORS = {
//   USER_LOGIN:       'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
//   USER_REGISTERED:  'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
//   USER_DELETED:     'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
//   USER_BLOCKED:     'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
//   USER_UNBLOCKED:   'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
//   PROBLEM_CREATED:  'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
//   PROBLEM_UPDATED:  'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
//   PROBLEM_DELETED:  'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
//   PROGRESS_UPDATED: 'bg-teal-100 text-teal-700 dark:bg-teal-900/30 dark:text-teal-400',
//   NOTE_SAVED:       'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300',
//   BOOKMARK_ADDED:   'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
// }
//
// export default function AdminLogsPage() {
//   const toast = useToast()
//   const { page, size, goToPage, reset } = usePagination(50)
//
//   const [logs, setLogs] = useState([])
//   const [total, setTotal] = useState(0)
//   const [totalPages, setTotalPages] = useState(0)
//   const [loading, setLoading] = useState(true)
//
//   const [actionFilter, setActionFilter] = useState('')
//   const [entityFilter, setEntityFilter] = useState('')
//
//   const [purgeModal, setPurgeModal] = useState(false)
//   const [purging, setPurging] = useState(false)
//
//   const debouncedAction = useDebounce(actionFilter, 400)
//   const debouncedEntity = useDebounce(entityFilter, 400)
//
//   // Reset page when filters change (debounced)
//   useEffect(() => {
//     reset()
//   }, [debouncedAction, debouncedEntity, reset])
//
//   const loadLogs = useCallback(async () => {
//     setLoading(true)
//
//     try {
//       const params = {
//         page,
//         size,
//         action: debouncedAction || undefined,
//         entityType: debouncedEntity || undefined,
//       }
//
//       const res = await api.get('/admin/logs', { params })
//
//       const d = res.data.data
//       setLogs(d.content)
//       setTotal(d.totalElements)
//       setTotalPages(d.totalPages)
//
//     } catch (err) {
//       toast.error('Failed to load logs')
//     } finally {
//       setLoading(false)
//     }
//   }, [page, size, debouncedAction, debouncedEntity, toast])
//
//   useEffect(() => {
//     loadLogs()
//   }, [loadLogs])
//
//   const handlePurge = async () => {
//     setPurging(true)
//
//     try {
//       const res = await api.delete('/admin/logs/purge', {
//         params: { retentionDays: 90 }
//       })
//
//       toast.success(
//         `Purged ${res.data.data?.deletedCount ?? 0} old log entries`
//       )
//
//       setPurgeModal(false)
//       loadLogs()
//
//     } catch {
//       toast.error('Purge failed')
//     } finally {
//       setPurging(false)
//     }
//   }
//
//   return (
//     <div className="space-y-5">
//
//       {/* Header */}
//       <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
//         <div>
//           <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
//             Activity Logs
//           </h1>
//           <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
//             {total.toLocaleString()} log entries
//           </p>
//         </div>
//
//         <Button
//           variant="danger"
//           size="sm"
//           onClick={() => setPurgeModal(true)}
//         >
//           🗑 Purge old logs
//         </Button>
//       </div>
//
//       {/* Filters */}
//       <div className="flex flex-col sm:flex-row gap-3">
//         <div className="flex-1">
//           <Input
//             placeholder="Filter by action (e.g. USER_LOGIN)…"
//             value={actionFilter}
//             onChange={e => setActionFilter(e.target.value)}
//             leftIcon={<span>🔍</span>}
//           />
//         </div>
//
//         <div className="sm:w-48">
//           <Input
//             placeholder="Entity type (e.g. USER)…"
//             value={entityFilter}
//             onChange={e => setEntityFilter(e.target.value)}
//           />
//         </div>
//       </div>
//
//       {/* Table */}
//       <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
//
//         {loading ? (
//           <div className="flex justify-center py-16">
//             <Spinner size="lg" />
//           </div>
//         ) : logs.length === 0 ? (
//           <div className="text-center py-16 text-slate-400">
//             No logs found.
//           </div>
//         ) : (
//           <div className="overflow-x-auto">
//             <table className="w-full text-sm">
//
//               <thead className="bg-slate-50 dark:bg-slate-800/80">
//                 <tr>
//                   {['Timestamp','User','Action','Entity','IP Address'].map(h => (
//                     <th
//                       key={h}
//                       className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider whitespace-nowrap"
//                     >
//                       {h}
//                     </th>
//                   ))}
//                 </tr>
//               </thead>
//
//               <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
//                 {logs.map(log => (
//                   <tr
//                     key={log.id}
//                     className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors"
//                   >
//                     <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">
//                       {formatDateTime(log.createdAt)}
//                     </td>
//
//                     <td className="px-4 py-3 text-xs text-slate-600 dark:text-slate-400 max-w-[160px] truncate">
//                       {log.userEmail ?? <span className="text-slate-400">system</span>}
//                     </td>
//
//                     <td className="px-4 py-3">
//                       <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
//                         ACTION_COLORS[log.action] ??
//                         'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300'
//                       }`}>
//                         {log.action}
//                       </span>
//                     </td>
//
//                     <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400">
//                       {log.entityType
//                         ? (
//                           <span>
//                             {log.entityType}
//                             <span className="text-slate-400 ml-1">
//                               {log.entityId?.slice(0, 8)}…
//                             </span>
//                           </span>
//                         )
//                         : '—'}
//                     </td>
//
//                     <td className="px-4 py-3 text-xs text-slate-400 dark:text-slate-500 font-mono">
//                       {log.ipAddress ?? '—'}
//                     </td>
//                   </tr>
//                 ))}
//               </tbody>
//
//             </table>
//           </div>
//         )}
//       </div>
//
//       {/* Pagination */}
//       <Pagination
//         page={page}
//         totalPages={totalPages}
//         totalElements={total}
//         size={size}
//         onPageChange={goToPage}
//       />
//
//       {/* Purge Modal */}
//       <Modal
//         isOpen={purgeModal}
//         onClose={() => setPurgeModal(false)}
//         title="Purge old logs"
//         size="sm"
//         showFooter
//         confirmLabel="Purge logs"
//         confirmVariant="danger"
//         onConfirm={handlePurge}
//         confirmLoading={purging}
//         cancelLabel="Cancel"
//       >
//         <p className="text-sm text-slate-600 dark:text-slate-400">
//           Delete all activity logs older than <strong>90 days</strong>? This action cannot be undone.
//         </p>
//       </Modal>
//
//     </div>
//   )
// }