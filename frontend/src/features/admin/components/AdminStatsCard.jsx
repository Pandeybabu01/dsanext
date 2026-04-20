import clsx from 'clsx'

export default function AdminStatsCard({ icon, label, value, sub, color = 'brand', onClick }) {
  const colors = {
    brand:  'bg-brand-50  dark:bg-brand-900/20  text-brand-600  dark:text-brand-400',
    green:  'bg-green-50  dark:bg-green-900/20  text-green-600  dark:text-green-400',
    amber:  'bg-amber-50  dark:bg-amber-900/20  text-amber-600  dark:text-amber-400',
    red:    'bg-red-50    dark:bg-red-900/20    text-red-600    dark:text-red-400',
    purple: 'bg-purple-50 dark:bg-purple-900/20 text-purple-600 dark:text-purple-400',
    blue:   'bg-blue-50   dark:bg-blue-900/20   text-blue-600   dark:text-blue-400',
  }

  return (
    <div
      onClick={onClick}
      className={clsx(
        'rounded-xl p-5 border border-slate-200 dark:border-slate-700 transition-all duration-150',
        'bg-white dark:bg-slate-800',
        onClick && 'cursor-pointer hover:shadow-card-hover hover:scale-[1.01]'
      )}
    >
      <div className={clsx('inline-flex items-center justify-center w-10 h-10 rounded-lg text-xl mb-3', colors[color])}>
        {icon}
      </div>
      <div className="text-3xl font-bold text-slate-900 dark:text-slate-100 tabular-nums">
        {value ?? 0}
      </div>
      <div className="text-sm font-medium text-slate-600 dark:text-slate-400 mt-1">{label}</div>
      {sub && <div className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">{sub}</div>}
    </div>
  )
}
