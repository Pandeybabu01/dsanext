import clsx from 'clsx'

const config = {
  EASY:   { label: 'Easy',   dot: '🟢', className: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400' },
  MEDIUM: { label: 'Medium', dot: '🟡', className: 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400' },
  HARD:   { label: 'Hard',   dot: '🔴', className: 'bg-red-100   text-red-800   dark:bg-red-900/30   dark:text-red-400' },
}

/**
 * DifficultyBadge — renders difficulty with colored pill + dot emoji.
 * Used in: ProblemListPage, ProblemDetailPage, Admin Panel, Progress list.
 *
 * Props:
 *  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
 *  showDot: boolean (default true)
 *  size: 'sm' | 'md'
 */
export default function DifficultyBadge({ difficulty, showDot = true, size = 'md' }) {
  if (!difficulty) return null

  const cfg = config[difficulty?.toUpperCase()] ?? config.EASY

  return (
    <span className={clsx(
      'inline-flex items-center gap-1 rounded-full font-medium',
      size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs',
      cfg.className
    )}>
      {showDot && <span className="text-xs leading-none">{cfg.dot}</span>}
      {cfg.label}
    </span>
  )
}
