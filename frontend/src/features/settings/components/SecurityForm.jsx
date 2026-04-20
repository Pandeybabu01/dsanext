import { useState } from 'react'
import clsx from 'clsx'
import api       from '../../../lib/axios'
import Button    from '../../../components/ui/Button'
import Input     from '../../../components/ui/Input'
import { useToast }      from '../../../hooks/useToast'
import { validatePassword } from '../../../lib/validators'

function PasswordStrength({ password }) {
  if (!password) return null

  const checks = [
    { label: 'At least 8 characters', pass: password.length >= 8 },
    { label: 'Uppercase letter',       pass: /[A-Z]/.test(password) },
    { label: 'Lowercase letter',       pass: /[a-z]/.test(password) },
    { label: 'Number',                 pass: /\d/.test(password) },
    { label: 'Special character',      pass: /[@$!%*?&]/.test(password) },
  ]

  const score = checks.filter(c => c.pass).length

  const barColor = score <= 1 ? 'bg-red-500'
    : score <= 2 ? 'bg-orange-500'
    : score <= 3 ? 'bg-amber-500'
    : score <= 4 ? 'bg-blue-500'
    : 'bg-green-500'

  const label = score <= 1 ? 'Very weak'
    : score <= 2 ? 'Weak'
    : score <= 3 ? 'Fair'
    : score <= 4 ? 'Strong'
    : 'Very strong'

  return (
    <div className="mt-2 space-y-2">
      <div className="flex items-center gap-2">
        <div className="flex gap-1 flex-1">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className={clsx(
              'h-1.5 flex-1 rounded-full transition-all duration-300',
              i < score ? barColor : 'bg-slate-200 dark:bg-slate-700'
            )} />
          ))}
        </div>
        <span className="text-xs text-slate-500 dark:text-slate-400 w-20 text-right">{label}</span>
      </div>
      <div className="grid grid-cols-2 gap-1">
        {checks.map(c => (
          <div key={c.label} className="flex items-center gap-1.5">
            <span className={clsx('text-xs', c.pass ? 'text-green-500' : 'text-slate-400')}>
              {c.pass ? '✓' : '○'}
            </span>
            <span className={clsx('text-xs', c.pass ? 'text-green-700 dark:text-green-400' : 'text-slate-400 dark:text-slate-500')}>
              {c.label}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

export default function SecurityForm() {
  const toast = useToast()

  const [form, setForm] = useState({
    currentPassword: '',
    newPassword:     '',
    confirmPassword: '',
  })
  const [errors,  setErrors]  = useState({})
  const [saving,  setSaving]  = useState(false)
  const [showPwd, setShowPwd] = useState({
    current: false, new: false, confirm: false,
  })

  const set = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.currentPassword)     e.currentPassword = 'Current password is required'
    const passErr = validatePassword(form.newPassword)
    if (passErr) e.newPassword = passErr
    if (form.newPassword !== form.confirmPassword) {
      e.confirmPassword = 'Passwords do not match'
    }
    if (form.currentPassword === form.newPassword) {
      e.newPassword = 'New password must be different from current password'
    }
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      await api.put('/users/password', form)
      toast.success('Password changed successfully')
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setErrors({})
    } catch (err) {
      const msg = err?.response?.data?.message
      toast.error(msg || 'Failed to change password')
      if (msg?.toLowerCase().includes('current')) {
        setErrors(e => ({ ...e, currentPassword: 'Current password is incorrect' }))
      }
    } finally {
      setSaving(false)
    }
  }

  const toggle = (field) => () => setShowPwd(p => ({ ...p, [field]: !p[field] }))

  const eyeIcon = (field) => (
    <button type="button" onClick={toggle(field)} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300">
      {showPwd[field] ? '🙈' : '👁'}
    </button>
  )

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <h2 className="text-base font-semibold text-slate-800 dark:text-slate-200 mb-5">
        Change password
      </h2>

      <div className="space-y-4">
        <Input
          label="Current password"
          type={showPwd.current ? 'text' : 'password'}
          value={form.currentPassword}
          onChange={set('currentPassword')}
          error={errors.currentPassword}
          required
          placeholder="Enter current password"
          rightIcon={eyeIcon('current')}
          autoComplete="current-password"
        />

        <div>
          <Input
            label="New password"
            type={showPwd.new ? 'text' : 'password'}
            value={form.newPassword}
            onChange={set('newPassword')}
            error={errors.newPassword}
            required
            placeholder="Create new password"
            rightIcon={eyeIcon('new')}
            autoComplete="new-password"
          />
          <PasswordStrength password={form.newPassword} />
        </div>

        <Input
          label="Confirm new password"
          type={showPwd.confirm ? 'text' : 'password'}
          value={form.confirmPassword}
          onChange={set('confirmPassword')}
          error={errors.confirmPassword}
          required
          placeholder="Repeat new password"
          rightIcon={eyeIcon('confirm')}
          autoComplete="new-password"
        />
      </div>

      <div className="flex justify-end mt-5 pt-5 border-t border-slate-100 dark:border-slate-700">
        <Button size="sm" loading={saving} onClick={handleSave}>
          Update password
        </Button>
      </div>
    </div>
  )
}
