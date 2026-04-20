import { useEffect, useState, useCallback } from 'react'
import clsx from 'clsx'
import api        from '../../../lib/axios'
import Button     from '../../../components/ui/Button'
import Input      from '../../../components/ui/Input'
import Modal      from '../../../components/ui/Modal'
import Spinner    from '../../../components/ui/Spinner'
import { useToast } from '../../../hooks/useToast'

const emptyForm = { name: '', baseUrl: '', iconUrl: '', isActive: true }

export default function AdminPlatformsPage() {
  const toast = useToast()
  const [platforms,    setPlatforms]    = useState([])
  const [loading,      setLoading]      = useState(true)
  const [formOpen,     setFormOpen]     = useState(false)
  const [editTarget,   setEditTarget]   = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [form,         setForm]         = useState(emptyForm)
  const [errors,       setErrors]       = useState({})
  const [saving,       setSaving]       = useState(false)
  const [deleting,     setDeleting]     = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
//       const res = await api.get('/admin/platforms', { params: { size: 100 } })
         const res = await api.get('/platforms/admin', { params: { size: 100 } })
      setPlatforms(res.data.data?.content ?? [])
    } catch { toast.error('Failed to load platforms') }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const openCreate = () => { setEditTarget(null); setForm(emptyForm); setErrors({}); setFormOpen(true) }
  const openEdit   = (p)  => { setEditTarget(p); setForm({ name: p.name, baseUrl: p.baseUrl, iconUrl: p.iconUrl ?? '', isActive: p.isActive }); setErrors({}); setFormOpen(true) }

  const set = (field) => (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm(f => ({ ...f, [field]: val }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.name.trim())    e.name    = 'Name is required'
    if (!form.baseUrl.trim()) e.baseUrl = 'Base URL is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      if (editTarget) { await api.put(`/platforms/${editTarget.id}`, form) }
      else            { await api.post('/platforms', form) }
      toast.success(editTarget ? 'Platform updated' : 'Platform created')
      setFormOpen(false); load()
    } catch (e) { toast.error(e?.response?.data?.message || 'Save failed') }
    finally { setSaving(false) }
  }

  const handleDelete = async () => {
    setDeleting(true)
    try {
      await api.delete(`/platforms/${deleteTarget.id}`)
      toast.success('Platform deleted'); setDeleteTarget(null); load()
    } catch (e) { toast.error(e?.response?.data?.message || 'Delete failed') }
    finally { setDeleting(false) }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Platform Management</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">{platforms.length} platforms configured</p>
        </div>
        <Button onClick={openCreate} leftIcon={<span>➕</span>}>Add platform</Button>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><Spinner size="lg" /></div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {platforms.map(p => (
            <div key={p.id} className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-3">
                  {p.iconUrl && (
                    <img src={p.iconUrl} alt={p.name} className="w-8 h-8 rounded object-contain bg-slate-100 p-1" onError={e => { e.target.style.display='none' }} />
                  )}
                  <div>
                    <p className="font-semibold text-slate-800 dark:text-slate-200">{p.name}</p>
                    <a href={p.baseUrl} target="_blank" rel="noreferrer"
                      className="text-xs text-brand-500 hover:underline truncate block max-w-[160px]">
                      {p.baseUrl}
                    </a>
                  </div>
                </div>
                <span className={clsx('shrink-0 px-2 py-0.5 rounded-full text-xs font-semibold',
                  p.isActive
                    ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                    : 'bg-slate-100  text-slate-500 dark:bg-slate-700   dark:text-slate-400')}>
                  {p.isActive ? 'Active' : 'Inactive'}
                </span>
              </div>
              <div className="flex gap-2 pt-3 border-t border-slate-100 dark:border-slate-700">
                <Button variant="outline" size="xs" onClick={() => openEdit(p)} className="flex-1">✏️ Edit</Button>
                <Button variant="danger"  size="xs" onClick={() => setDeleteTarget(p)}>🗑</Button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create/Edit modal */}
      <Modal isOpen={formOpen} onClose={() => setFormOpen(false)}
        title={editTarget ? 'Edit platform' : 'Add platform'} size="md"
        showFooter confirmLabel={editTarget ? 'Save' : 'Create'} onConfirm={handleSave}
        confirmLoading={saving} cancelLabel="Cancel">
        <div className="space-y-4">
          <Input label="Platform name" value={form.name} onChange={set('name')} error={errors.name} required placeholder="e.g. LeetCode" />
          <Input label="Base URL"      value={form.baseUrl} onChange={set('baseUrl')} error={errors.baseUrl} required placeholder="https://leetcode.com/problems/" />
          <Input label="Icon URL"      value={form.iconUrl} onChange={set('iconUrl')} placeholder="https://…/favicon.ico (optional)" />
          <label className="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" checked={form.isActive} onChange={set('isActive')}
              className="w-4 h-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500" />
            <span className="text-sm text-slate-700 dark:text-slate-300">Active</span>
          </label>
        </div>
      </Modal>

      {/* Delete confirm */}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)}
        title="Delete platform" size="sm" showFooter
        confirmLabel="Delete" confirmVariant="danger"
        onConfirm={handleDelete} confirmLoading={deleting} cancelLabel="Cancel">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Delete <strong className="text-slate-800 dark:text-slate-200">"{deleteTarget?.name}"</strong>?
          Problems linked to this platform will remain but lose their platform association.
        </p>
      </Modal>
    </div>
  )
}
