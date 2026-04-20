import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, PieChart, Pie, Cell, ResponsiveContainer } from 'recharts'
import api            from '../../../lib/axios'
import Spinner        from '../../../components/ui/Spinner'
import AdminStatsCard from '../components/AdminStatsCard'

const DIFF_COLORS = { EASY: '#22c55e', MEDIUM: '#f59e0b', HARD: '#ef4444' }
const ROLE_COLORS = ['#6366f1', '#f59e0b']

export default function AdminDashboardPage() {
  const [data, setData]       = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/analytics/admin').then(r => setData(r.data.data)).catch(console.error).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>

  const diffData  = Object.entries(data?.problemsByDifficulty ?? {}).map(([name, value]) => ({ name, value }))
  const roleData  = Object.entries(data?.usersByRole ?? {}).map(([name, value]) => ({ name, value }))
  const dailyData = (data?.dailyStats ?? []).slice(-14).map(d => ({ date: d.date?.slice(5), count: d.count }))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Admin Dashboard</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">Platform overview and system health</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { icon:'👥', label:'Total Users',     value: data?.totalUsers,           sub:`${data?.totalActiveUsers} active`,         color:'brand',  to:'/admin/users'    },
          { icon:'💻', label:'Total Problems',  value: data?.totalProblems,        sub:`${data?.totalActiveProblems} active`,      color:'green',  to:'/admin/problems' },
          { icon:'📈', label:'Progress Entries',value: data?.totalProgressEntries, sub:'across all users',                        color:'purple', to:null              },
          { icon:'🚫', label:'Inactive Users',  value:(data?.totalUsers??0)-(data?.totalActiveUsers??0), sub:'blocked / disabled',color:'red', to:'/admin/users'  },
        ].map(s => s.to
          ? <Link key={s.label} to={s.to}><AdminStatsCard {...s} /></Link>
          : <AdminStatsCard key={s.label} {...s} />
        )}
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Problems by difficulty</h2>
          <ResponsiveContainer width="100%" height={180}>
            <PieChart>
              <Pie data={diffData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70} label={({name,value})=>`${name}: ${value}`} labelLine={false}>
                {diffData.map(d => <Cell key={d.name} fill={DIFF_COLORS[d.name]??'#6366f1'} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Users by role</h2>
          <ResponsiveContainer width="100%" height={180}>
            <PieChart>
              <Pie data={roleData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70} label={({name,value})=>`${name}: ${value}`} labelLine={false}>
                {roleData.map((_,i) => <Cell key={i} fill={ROLE_COLORS[i%ROLE_COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Quick actions</h2>
          <div className="space-y-1">
            {[
              { to:'/admin/problems', icon:'➕', label:'Add new problem'   },
              { to:'/admin/users',    icon:'👥', label:'Manage users'      },
              { to:'/admin/platforms',icon:'🌐', label:'Manage platforms'  },
              { to:'/admin/settings', icon:'⚙️', label:'App settings'      },
              { to:'/admin/logs',     icon:'📋', label:'Activity logs'     },
            ].map(item => (
              <Link key={item.to} to={item.to}
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 hover:text-slate-900 dark:hover:text-slate-100 transition-colors">
                <span>{item.icon}</span>{item.label}
              </Link>
            ))}
          </div>
        </div>
      </div>

      {dailyData.length > 0 && (
        <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5">
          <h2 className="font-semibold text-slate-800 dark:text-slate-200 mb-4">Platform activity — problems solved (last 14 days)</h2>
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
