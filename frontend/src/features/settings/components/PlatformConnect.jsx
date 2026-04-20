import { useState, useEffect } from 'react'
import api    from '../../../lib/axios'
import Button from '../../../components/ui/Button'
import Input  from '../../../components/ui/Input'
import { useToast } from '../../../hooks/useToast'

const PLATFORMS = [
  {
    key:         'lcUsername',
    name:        'LeetCode',
    icon:        '🟡',
    placeholder: 'your-leetcode-username',
    url:         'https://leetcode.com/',
    color:       'text-amber-600 dark:text-amber-400',
  },
  {
    key:         'cfUsername',
    name:        'Codeforces',
    icon:        '🔵',
    placeholder: 'your-codeforces-handle',
    url:         'https://codeforces.com/',
    color:       'text-blue-600 dark:text-blue-400',
  },
  {
    key:         'hrUsername',
    name:        'HackerRank',
    icon:        '🟢',
    placeholder: 'your-hackerrank-username',
    url:         'https://www.hackerrank.com/',
    color:       'text-green-600 dark:text-green-400',
  },
  {
    key:         'ibUsername',
    name:        'InterviewBit',
    icon:        '🟣',
    placeholder: 'your-interviewbit-username',
    url:         'https://www.interviewbit.com/',
    color:       'text-purple-600 dark:text-purple-400',
  },
]

export default function PlatformConnect() {
  const toast = useToast()
  const [form,    setForm]    = useState({ lcUsername:'', cfUsername:'', hrUsername:'', ibUsername:'' })
  const [loading, setLoading] = useState(true)
  const [saving,  setSaving]  = useState(false)

  useEffect(() => {
    api.get('/settings/me')
      .then(r => {
        const d = r.data.data
        setForm({
          lcUsername: d?.lcUsername ?? '',
          cfUsername: d?.cfUsername ?? '',
          hrUsername: d?.hrUsername ?? '',
          ibUsername: d?.ibUsername ?? '',
        })
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const set = (key) => (e) => setForm(f => ({ ...f, [key]: e.target.value }))

  const handleSave = async () => {
    setSaving(true)
    try {
      await api.put('/settings/me', form)
      toast.success('Platform usernames saved')
    } catch {
      toast.error('Failed to save platform settings')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <div className="mb-5">
        <h2 className="text-base font-semibold text-slate-800 dark:text-slate-200">
          Connected platforms
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Link your external coding platform profiles to track your activity across platforms.
        </p>
      </div>

      {loading ? (
        <div className="py-4 text-center text-slate-400 text-sm">Loading…</div>
      ) : (
        <div className="space-y-4">
          {PLATFORMS.map(p => (
            <div key={p.key} className="flex items-start gap-4">
              {/* Platform info */}
              <div className="w-32 shrink-0 pt-2">
                <div className="flex items-center gap-1.5">
                  <span className="text-base">{p.icon}</span>
                  <a
                    href={p.url}
                    target="_blank"
                    rel="noreferrer"
                    className={`text-sm font-medium hover:underline ${p.color}`}
                  >
                    {p.name}
                  </a>
                </div>
                {form[p.key] && (
                  <a
                    href={`${p.url}${form[p.key]}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-xs text-slate-400 hover:text-brand-500 mt-0.5 block truncate"
                  >
                    ↗ View profile
                  </a>
                )}
              </div>

              {/* Username input */}
              <div className="flex-1">
                <Input
                  value={form[p.key]}
                  onChange={set(p.key)}
                  placeholder={p.placeholder}
                  leftIcon={<span className="text-slate-400 text-xs">@</span>}
                />
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="flex justify-end mt-5 pt-5 border-t border-slate-100 dark:border-slate-700">
        <Button size="sm" loading={saving} onClick={handleSave}>
          Save platforms
        </Button>
      </div>
    </div>
  )
}
