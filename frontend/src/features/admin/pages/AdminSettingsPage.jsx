import { useEffect, useState, useCallback } from 'react'
import clsx from 'clsx'
import api      from '../../../lib/axios'
import Button   from '../../../components/ui/Button'
import Input    from '../../../components/ui/Input'
import Spinner  from '../../../components/ui/Spinner'
import { useToast } from '../../../hooks/useToast'

const SECTIONS = {
  'app':      { label: 'Branding',      icon: '🎨' },
  'feature':  { label: 'Features',      icon: '🔧' },
  'security': { label: 'Security',      icon: '🔒' },
  'pagination':{ label: 'Pagination',   icon: '📄' },
  'logs':     { label: 'Logs',          icon: '📋' },
}

function groupByPrefix(settings) {
  return settings.reduce((acc, s) => {
    const prefix = s.settingKey.split('.')[0]
    if (!acc[prefix]) acc[prefix] = []
    acc[prefix].push(s)
    return acc
  }, {})
}

export default function AdminSettingsPage() {
  const toast = useToast()
  const [settings, setSettings]   = useState([])
  const [editing,  setEditing]    = useState({})   // key → draft value
  const [saving,   setSaving]     = useState({})   // key → bool
  const [loading,  setLoading]    = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.get('/settings/admin')
      setSettings(res.data.data ?? [])
    } catch { toast.error('Failed to load settings') }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { load() }, [load])

  const getDraft = (s) => editing[s.settingKey] !== undefined ? editing[s.settingKey] : s.settingValue

  const handleChange = (key, val) => setEditing(e => ({ ...e, [key]: val }))

  const handleSave = async (key) => {
    setSaving(s => ({ ...s, [key]: true }))
    try {
      await api.put(`/settings/admin/${key}`, { value: editing[key] })
      toast.success('Setting saved')
      setEditing(e => { const n = { ...e }; delete n[key]; return n })
      load()
    } catch (e) { toast.error(e?.response?.data?.message || 'Save failed') }
    finally { setSaving(s => ({ ...s, [key]: false })) }
  }

  const isDirty = (key) => editing[key] !== undefined

  const grouped = groupByPrefix(settings)

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">App Settings</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
          Global platform configuration — changes apply immediately
        </p>
      </div>

      {Object.entries(grouped).map(([prefix, items]) => {
        const section = SECTIONS[prefix] ?? { label: prefix, icon: '⚙️' }
        return (
          <div key={prefix} className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 overflow-hidden">
            {/* Section header */}
            <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-700 flex items-center gap-2">
              <span className="text-lg">{section.icon}</span>
              <h2 className="font-semibold text-slate-800 dark:text-slate-200">{section.label}</h2>
            </div>

            {/* Settings rows */}
            <div className="divide-y divide-slate-100 dark:divide-slate-700">
              {items.map(s => {
                const isBool  = s.dataType === 'BOOLEAN'
                const draft   = getDraft(s)
                const dirty   = isDirty(s.settingKey)

                return (
                  <div key={s.settingKey} className="px-5 py-4 flex flex-col sm:flex-row sm:items-center gap-3">
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-slate-800 dark:text-slate-200 font-mono">
                        {s.settingKey}
                      </p>
                      {s.description && (
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{s.description}</p>
                      )}
                      <div className="flex items-center gap-2 mt-0.5">
                        <span className="text-xs px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-700 text-slate-500 dark:text-slate-400 font-mono">
                          {s.dataType}
                        </span>
                        {s.isPublic && (
                          <span className="text-xs px-1.5 py-0.5 rounded bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">
                            public
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2 sm:w-64 shrink-0">
                      {isBool ? (
                        <button
                          onClick={() => handleChange(s.settingKey, draft === 'true' ? 'false' : 'true')}
                          className={clsx(
                            'relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2',
                            draft === 'true' ? 'bg-brand-600' : 'bg-slate-300 dark:bg-slate-600'
                          )}
                        >
                          <span className={clsx(
                            'inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform',
                            draft === 'true' ? 'translate-x-6' : 'translate-x-1'
                          )} />
                        </button>
                      ) : (
                        <Input
                          value={draft}
                          onChange={e => handleChange(s.settingKey, e.target.value)}
                          className="text-xs font-mono"
                        />
                      )}

                      {(dirty || isBool) && (
                        <Button size="xs" loading={saving[s.settingKey]}
                          onClick={() => handleSave(s.settingKey)}
                          variant={dirty ? 'primary' : 'outline'}>
                          {isBool ? 'Save' : 'Save'}
                        </Button>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        )
      })}
    </div>
  )
}
