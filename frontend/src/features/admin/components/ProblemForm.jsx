import { useState, useEffect } from 'react'
import Modal    from '../../../components/ui/Modal'
import Input    from '../../../components/ui/Input'
import Button   from '../../../components/ui/Button'
import api      from '../../../lib/axios'
import { DIFFICULTY_OPTIONS, TOPICS } from '../../../lib/constants'

/**
 * ProblemForm — create or edit a problem via modal.
 * Props:
 *   isOpen, onClose, onSuccess, problem (null = create mode)
 */
export default function ProblemForm({ isOpen, onClose, onSuccess, problem = null }) {
  const isEdit = !!problem
  const [platforms, setPlatforms] = useState([])
  const [saving,    setSaving]    = useState(false)
  const [errors,    setErrors]    = useState({})

  const [form, setForm] = useState({
    title: '', description: '', topic: '', difficulty: 'EASY',
    externalUrl: '', platformId: '', isActive: true,
  })

  useEffect(() => {
    api.get('/platforms').then(r => setPlatforms(r.data.data ?? [])).catch(() => {})
  }, [])

  useEffect(() => {
    if (problem) {
      setForm({
        title:       problem.title       ?? '',
        description: problem.description ?? '',
        topic:       problem.topic       ?? '',
        difficulty:  problem.difficulty  ?? 'EASY',
        externalUrl: problem.externalUrl ?? '',
        platformId:  problem.platform?.id ?? '',
        isActive:    problem.isActive    ?? true,
      })
    } else {
      setForm({ title:'', description:'', topic:'', difficulty:'EASY', externalUrl:'', platformId:'', isActive:true })
    }
    setErrors({})
  }, [problem, isOpen])

  const set = (field) => (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm(f => ({ ...f, [field]: val }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.title.trim())  e.title = 'Title is required'
    if (!form.topic.trim())  e.topic = 'Topic is required'
    if (!form.difficulty)    e.difficulty = 'Difficulty is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      const payload = { ...form, platformId: form.platformId || undefined }
      if (isEdit) {
        await api.put(`/admin/problems/${problem.id}`, payload)
      } else {
        await api.post('/admin/problems', payload)
      }
      onSuccess?.()
      onClose()
    } catch (e) {
      const msg = e?.response?.data?.message
      const errs = e?.response?.data?.error
      if (typeof errs === 'object') setErrors(errs)
      else setErrors({ _form: msg || 'Failed to save problem' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose}
      title={isEdit ? 'Edit problem' : 'Add new problem'}
      size="lg" showFooter
      confirmLabel={isEdit ? 'Save changes' : 'Create problem'}
      onConfirm={handleSubmit} confirmLoading={saving} cancelLabel="Cancel">

      {errors._form && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 text-sm text-red-700 dark:text-red-400">
          {errors._form}
        </div>
      )}

      <div className="space-y-4">
        <Input label="Title" value={form.title} onChange={set('title')} error={errors.title} required
          placeholder="e.g. Two Sum" />

        <div>
          <label className="label">Description</label>
          <textarea value={form.description} onChange={set('description')}
            className="input resize-none h-24 text-sm" placeholder="Problem description (optional)" />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Topic */}
          <div>
            <label className="label">Topic <span className="text-red-500">*</span></label>
            <select value={form.topic} onChange={set('topic')} className="input cursor-pointer">
              <option value="">Select topic…</option>
              {TOPICS.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            {errors.topic && <p className="mt-1 text-xs text-red-500">⚠ {errors.topic}</p>}
          </div>

          {/* Difficulty */}
          <div>
            <label className="label">Difficulty <span className="text-red-500">*</span></label>
            <select value={form.difficulty} onChange={set('difficulty')} className="input cursor-pointer">
              {DIFFICULTY_OPTIONS.filter(o => o.value).map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
            {errors.difficulty && <p className="mt-1 text-xs text-red-500">⚠ {errors.difficulty}</p>}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Platform */}
          <div>
            <label className="label">Platform</label>
            <select value={form.platformId} onChange={set('platformId')} className="input cursor-pointer">
              <option value="">None</option>
              {platforms.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>

          {/* External URL */}
          <Input label="External URL" value={form.externalUrl} onChange={set('externalUrl')}
            placeholder="https://…" error={errors.externalUrl} />
        </div>

        {/* Active toggle (edit mode only) */}
        {isEdit && (
          <label className="flex items-center gap-3 cursor-pointer">
            <input type="checkbox" checked={form.isActive} onChange={set('isActive')}
              className="w-4 h-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500" />
            <span className="text-sm text-slate-700 dark:text-slate-300">Active (visible to users)</span>
          </label>
        )}
      </div>
    </Modal>
  )
}
