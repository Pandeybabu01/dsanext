import { useEffect, useState } from 'react'
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import api from '../../../lib/axios'
import Spinner from '../../../components/ui/Spinner'

const DIFF_COLORS  = { EASY: '#22c55e', MEDIUM: '#f59e0b', HARD: '#ef4444' }
const TOPIC_COLORS = ['#6366f1','#8b5cf6','#ec4899','#14b8a6','#f59e0b','#ef4444','#3b82f6','#10b981']

export default function AnalyticsPage() {
  const [data,    setData]    = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/analytics/me')
      .then(r => setData(r.data.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (!data)   return <div className="text-center py-20 text-slate-400">No analytics data yet. Start solving problems!</div>

  const diffData  = Object.entries(data.solvedByDifficulty || {}).map(([name, value]) => ({ name, value }))
  const topicData = Object.entries(data.solvedByTopic    || {}).slice(0, 8).map(([name, value]) => ({ name, value }))
  const dailyData = (data.dailyStats || []).slice(-14).map(d => ({ date: d.date?.slice(5), count: d.count }))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Analytics</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">Your DSA progress breakdown</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Total Solved',   value: data.totalSolved,     icon: '✅', color: 'text-green-600 dark:text-green-400' },
          { label: 'In Progress',    value: data.totalInProgress, icon: '🔄', color: 'text-blue-600  dark:text-blue-400'  },
          { label: 'Bookmarks',      value: data.totalBookmarks,  icon: '🔖', color: 'text-amber-600 dark:text-amber-400' },
          { label: 'Notes Written',  value: data.totalNotes,      icon: '📝', color: 'text-purple-600 dark:text-purple-400' },
        ].map(s => (
          <div key={s.label} className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
            <div className="text-2xl mb-2">{s.icon}</div>
            <div className={`text-3xl font-bold ${s.color}`}>{s.value ?? 0}</div>
            <div className="text-sm text-slate-500 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Difficulty pie */}
        {diffData.length > 0 && (
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
            <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Solved by difficulty</h2>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={diffData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label={({name,value}) => `${name}: ${value}`}>
                  {diffData.map((d) => <Cell key={d.name} fill={DIFF_COLORS[d.name] || '#6366f1'} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Topic bar */}
        {topicData.length > 0 && (
          <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
            <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Solved by topic</h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={topicData} layout="vertical">
                <XAxis type="number" tick={{ fontSize: 11 }} />
                <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={90} />
                <Tooltip />
                <Bar dataKey="value" radius={[0,4,4,0]}>
                  {topicData.map((_, i) => <Cell key={i} fill={TOPIC_COLORS[i % TOPIC_COLORS.length]} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Daily activity */}
      {dailyData.length > 0 && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Daily activity (last 14 days)</h2>
          <ResponsiveContainer width="100%" height={160}>
            <BarChart data={dailyData}>
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#6366f1" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}
