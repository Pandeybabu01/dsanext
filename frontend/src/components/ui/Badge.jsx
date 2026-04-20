import clsx from 'clsx'

const variants = {
  default: 'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300',
  primary: 'bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-400',
  success: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
  warning: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400',
  danger:  'bg-red-100   text-red-800   dark:bg-red-900/30   dark:text-red-400',
  info:    'bg-blue-100  text-blue-800  dark:bg-blue-900/30  dark:text-blue-400',
}

export default function Badge({ children, variant = 'default', className = '' }) {
  return (
    <span className={clsx(
      'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium',
      variants[variant],
      className
    )}>
      {children}
    </span>
  )
}
