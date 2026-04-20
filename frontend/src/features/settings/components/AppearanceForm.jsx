import clsx from 'clsx'
import { useTheme }  from '../../../hooks/useTheme'
import { useToast }  from '../../../hooks/useToast'
import api           from '../../../lib/axios'
import Button        from '../../../components/ui/Button'
import { useState }  from 'react'

const THEMES = [
  {
    value:       'light',
    label:       'Light',
    icon:        '☀️',
    description: 'Clean white interface',
    preview:     'bg-white border-slate-200',
    dot:         'bg-slate-800',
  },
  {
    value:       'dark',
    label:       'Dark',
    icon:        '🌙',
    description: 'Easy on the eyes',
    preview:     'bg-slate-900 border-slate-700',
    dot:         'bg-slate-200',
  },
  {
    value:       'system',
    label:       'System',
    icon:        '🖥',
    description: 'Follows OS setting',
    preview:     'bg-gradient-to-br from-white to-slate-900 border-slate-400',
    dot:         'bg-slate-500',
  },
]

export default function AppearanceForm() {
  const { theme, setTheme } = useTheme()
  const toast   = useToast()
  const [saving, setSaving] = useState(false)

  const handleSave = async (newTheme) => {
    setTheme(newTheme)
    setSaving(true)
    try {
      await api.put('/settings/me', { theme: newTheme })
      toast.success('Theme saved')
    } catch {
      toast.error('Failed to save theme preference')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <h2 className="text-base font-semibold text-slate-800 dark:text-slate-200 mb-1">
        Appearance
      </h2>
      <p className="text-xs text-slate-500 dark:text-slate-400 mb-5">
        Choose how DSANext looks on your device.
      </p>

      <div className="grid grid-cols-3 gap-3">
        {THEMES.map(t => (
          <button
            key={t.value}
            onClick={() => handleSave(t.value)}
            className={clsx(
              'relative flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all text-center',
              theme === t.value
                ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/20'
                : 'border-slate-200 dark:border-slate-600 hover:border-brand-300 dark:hover:border-brand-700 hover:bg-slate-50 dark:hover:bg-slate-700/30'
            )}
          >
            {/* Selected checkmark */}
            {theme === t.value && (
              <span className="absolute top-2 right-2 text-brand-600 dark:text-brand-400 text-xs font-bold">✓</span>
            )}

            {/* Mini preview */}
            <div className={clsx('w-12 h-8 rounded border-2 flex items-center justify-center', t.preview)}>
              <div className={clsx('w-4 h-1.5 rounded-full', t.dot)} />
            </div>

            <span className="text-xl leading-none">{t.icon}</span>

            <div>
              <p className={clsx(
                'text-sm font-semibold',
                theme === t.value
                  ? 'text-brand-700 dark:text-brand-400'
                  : 'text-slate-700 dark:text-slate-300'
              )}>
                {t.label}
              </p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">{t.description}</p>
            </div>
          </button>
        ))}
      </div>
    </div>
  )
}
