import clsx from 'clsx'

/**
 * Reusable stat card used across Dashboard and Analytics pages.
 * Shows an icon, large metric value, label, and optional trend indicator.
 */
export default function StatsCard({
  icon,
  label,
  value,
  trend,
  trendLabel,
  colorClass = 'text-brand-600 dark:text-brand-400',
  bgClass    = 'bg-brand-50 dark:bg-brand-900/20',
  onClick,
}) {
  return (
    <div
      className={clsx(
        'rounded-xl p-5 border border-slate-200 dark:border-slate-700 transition-all duration-150',
        bgClass,
        onClick && 'cursor-pointer hover:shadow-card-hover hover:scale-[1.01]'
      )}
      onClick={onClick}
    >
      <div className="flex items-start justify-between gap-2">
        <span className="text-2xl">{icon}</span>
        {trend != null && (
          <span className={clsx(
            'text-xs font-semibold px-1.5 py-0.5 rounded-full',
            trend >= 0
              ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
              : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
          )}>
            {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}
          </span>
        )}
      </div>

      <div className={clsx('text-3xl font-bold mt-2 tabular-nums', colorClass)}>
        {value ?? 0}
      </div>

      <div className="text-sm text-slate-500 dark:text-slate-400 mt-1 font-medium">
        {label}
      </div>

      {trendLabel && (
        <div className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">{trendLabel}</div>
      )}
    </div>
  )
}
