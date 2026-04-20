import { useState, useEffect } from 'react'
import clsx  from 'clsx'
import api    from '../../../lib/axios'
import Button from '../../../components/ui/Button'
import { useToast } from '../../../hooks/useToast'

function Toggle({ checked, onChange, label, description }) {
  return (
    <div className="flex items-start justify-between gap-4 py-3">
      <div className="flex-1">
        <p className="text-sm font-medium text-slate-700 dark:text-slate-300">{label}</p>
        {description && (
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{description}</p>
        )}
      </div>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        onClick={() => onChange(!checked)}
        className={clsx(
          'relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full',
          'focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2',
          'transition-colors duration-200 ease-in-out',
          checked ? 'bg-brand-600' : 'bg-slate-200 dark:bg-slate-600'
        )}
      >
        <span className={clsx(
          'inline-block h-4 w-4 transform rounded-full bg-white shadow',
          'transition-transform duration-200 ease-in-out',
          checked ? 'translate-x-6' : 'translate-x-1'
        )} />
      </button>
    </div>
  )
}

export default function NotificationPrefs() {
  const toast = useToast()
  const [prefs, setPrefs]   = useState({ notificationsEnabled: true, emailNotifications: true })
  const [loading, setLoading] = useState(true)
  const [saving,  setSaving]  = useState(false)

  useEffect(() => {
    api.get('/settings/me')
      .then(r => setPrefs({
        notificationsEnabled: r.data.data?.notificationsEnabled ?? true,
        emailNotifications:   r.data.data?.emailNotifications   ?? true,
      }))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const set = (key) => (val) => setPrefs(p => ({ ...p, [key]: val }))

  const handleSave = async () => {
    setSaving(true)
    try {
      await api.put('/settings/me', prefs)
      toast.success('Notification preferences saved')
    } catch {
      toast.error('Failed to save preferences')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <h2 className="text-base font-semibold text-slate-800 dark:text-slate-200 mb-4">
        Notification preferences
      </h2>

      {loading ? (
        <div className="py-4 text-center text-slate-400 text-sm">Loading…</div>
      ) : (
        <div className="divide-y divide-slate-100 dark:divide-slate-700">
          <Toggle
            checked={prefs.notificationsEnabled}
            onChange={set('notificationsEnabled')}
            label="In-app notifications"
            description="Show notifications in the DSANext dashboard and bell icon"
          />
          <Toggle
            checked={prefs.emailNotifications}
            onChange={set('emailNotifications')}
            label="Email notifications"
            description="Receive important updates and announcements via email"
          />
        </div>
      )}

      <div className="flex justify-end mt-4 pt-4 border-t border-slate-100 dark:border-slate-700">
        <Button size="sm" loading={saving} onClick={handleSave}>
          Save preferences
        </Button>
      </div>
    </div>
  )
}
