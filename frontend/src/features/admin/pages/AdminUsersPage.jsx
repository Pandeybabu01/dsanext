import { useEffect, useState, useCallback } from 'react'
import clsx from 'clsx'
import api        from '../../../lib/axios'
import Button     from '../../../components/ui/Button'
import Input      from '../../../components/ui/Input'
import Modal      from '../../../components/ui/Modal'
import Pagination from '../../../components/ui/Pagination'
import Spinner    from '../../../components/ui/Spinner'
import { useToast }    from '../../../hooks/useToast'
import { useDebounce } from '../../../hooks/useDebounce'
import { usePagination } from '../../../hooks/usePagination'
import { formatDate } from '../../../lib/formatters'

const roleBadge = (role) => role === 'ADMIN'
  ? 'bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-400'
  : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300'

export default function AdminUsersPage() {
  const toast   = useToast()
  const { page, size, goToPage, reset } = usePagination(20)

  const [users,   setUsers]   = useState([])
  const [total,   setTotal]   = useState(0)
  const [pages,   setPages]   = useState(0)
  const [loading, setLoading] = useState(true)
  const [search,  setSearch]  = useState('')
  const [roleFilter,   setRoleFilter]   = useState('')
  const [activeFilter, setActiveFilter] = useState('')
  const [confirm, setConfirm] = useState(null)  // { type, user }
  const [acting,  setActing]  = useState(false)
  const debouncedSearch = useDebounce(search, 400)

  const loadUsers = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size, search: debouncedSearch || undefined, role: roleFilter || undefined, active: activeFilter !== '' ? activeFilter : undefined }
      const res = await api.get('/admin/users', { params })
      const d   = res.data.data
      setUsers(d.content); setTotal(d.totalElements); setPages(d.totalPages)
    } catch { toast.error('Failed to load users') }
    finally  { setLoading(false) }
  }, [page, size, debouncedSearch, roleFilter, activeFilter])

  useEffect(() => { loadUsers() }, [loadUsers])

  const handleBlock = async (user) => {
    setActing(true)
    try {
      await api.patch(`/admin/users/${user.id}/${user.isActive ? 'block' : 'unblock'}`)
      toast.success(`User ${user.isActive ? 'blocked' : 'unblocked'}`)
      loadUsers()
    } catch (e) { toast.error(e?.response?.data?.message || 'Action failed') }
    finally { setActing(false); setConfirm(null) }
  }

  const handleRoleChange = async (user, newRole) => {
    setActing(true)
    try {
      await api.patch(`/admin/users/${user.id}/role`, null, { params: { role: newRole } })
      toast.success(`Role changed to ${newRole}`)
      loadUsers()
    } catch (e) { toast.error(e?.response?.data?.message || 'Action failed') }
    finally { setActing(false); setConfirm(null) }
  }

  const handleDelete = async (user) => {
    setActing(true)
    try {
      await api.delete(`/admin/users/${user.id}`)
      toast.success('User deleted')
      loadUsers()
    } catch (e) { toast.error(e?.response?.data?.message || 'Delete failed') }
    finally { setActing(false); setConfirm(null) }
  }

  const execConfirm = () => {
    if (!confirm) return
    if (confirm.type === 'delete') handleDelete(confirm.user)
    else if (confirm.type === 'block') handleBlock(confirm.user)
    else if (confirm.type === 'role') handleRoleChange(confirm.user, confirm.newRole)
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">User Management</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">{total} total users</p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <Input placeholder="Search by name, email or username…" value={search}
            onChange={e => { setSearch(e.target.value); reset() }} leftIcon={<span>🔍</span>} />
        </div>
        <select value={roleFilter} onChange={e => { setRoleFilter(e.target.value); reset() }} className="input w-full sm:w-36">
          <option value="">All roles</option>
          <option value="USER">User</option>
          <option value="ADMIN">Admin</option>
        </select>
        <select value={activeFilter} onChange={e => { setActiveFilter(e.target.value); reset() }} className="input w-full sm:w-36">
          <option value="">All statuses</option>
          <option value="true">Active</option>
          <option value="false">Blocked</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : users.length === 0 ? (
          <div className="text-center py-16 text-slate-400">No users found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 dark:bg-slate-800/80">
                <tr>
                  {['User','Role','Status','Joined','Actions'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                {users.map(u => (
                  <tr key={u.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-brand-600 text-white flex items-center justify-center text-sm font-semibold shrink-0">
                          {u.fullName?.[0]?.toUpperCase()}
                        </div>
                        <div>
                          <p className="font-medium text-slate-800 dark:text-slate-200">{u.fullName}</p>
                          <p className="text-xs text-slate-500 dark:text-slate-400">{u.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <span className={clsx('px-2 py-0.5 rounded-full text-xs font-semibold', roleBadge(u.role))}>
                        {u.role}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className={clsx('px-2 py-0.5 rounded-full text-xs font-semibold',
                        u.isActive
                          ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                          : 'bg-red-100   text-red-700   dark:bg-red-900/30   dark:text-red-400')}>
                        {u.isActive ? 'Active' : 'Blocked'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">
                      {formatDate(u.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1">
                        {/* Block/Unblock */}
                        <Button variant="outline" size="xs"
                          onClick={() => setConfirm({ type: 'block', user: u })}>
                          {u.isActive ? '🚫 Block' : '✅ Unblock'}
                        </Button>
                        {/* Role toggle */}
                        <Button variant="outline" size="xs"
                          onClick={() => setConfirm({ type: 'role', user: u, newRole: u.role === 'ADMIN' ? 'USER' : 'ADMIN' })}>
                          {u.role === 'ADMIN' ? '👤 Make User' : '🛡 Make Admin'}
                        </Button>
                        {/* Delete */}
                        {u.role !== 'ADMIN' && (
                          <Button variant="danger" size="xs"
                            onClick={() => setConfirm({ type: 'delete', user: u })}>
                            🗑
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Pagination page={page} totalPages={pages} totalElements={total} size={size} onPageChange={goToPage} />

      {/* Confirm modal */}
      <Modal
        isOpen={!!confirm}
        onClose={() => setConfirm(null)}
        title={
          confirm?.type === 'delete' ? 'Delete user'
          : confirm?.type === 'block' ? (confirm?.user?.isActive ? 'Block user' : 'Unblock user')
          : 'Change role'
        }
        size="sm"
        showFooter
        confirmLabel="Confirm"
        confirmVariant={confirm?.type === 'delete' ? 'danger' : 'primary'}
        onConfirm={execConfirm}
        confirmLoading={acting}
        cancelLabel="Cancel"
      >
        <p className="text-sm text-slate-600 dark:text-slate-400">
          {confirm?.type === 'delete' && `Are you sure you want to permanently delete ${confirm?.user?.fullName}? This cannot be undone.`}
          {confirm?.type === 'block' && (confirm?.user?.isActive
            ? `Block ${confirm?.user?.fullName}? They will not be able to log in.`
            : `Unblock ${confirm?.user?.fullName}? They will regain access.`)}
          {confirm?.type === 'role' && `Change ${confirm?.user?.fullName}'s role to ${confirm?.newRole}?`}
        </p>
      </Modal>
    </div>
  )
}
