import { Link } from 'react-router-dom'
import clsx from 'clsx'
import DifficultyBadge from '../../../components/ui/DifficultyBadge'
import { getProgressBg } from '../../../lib/formatters'

/**
 * ProblemCard — card layout for the problem list.
 * Shows title, topic, difficulty, platform, progress status, and bookmark/note indicators.
 */
export default function ProblemCard({ problem }) {
  const { title, slug, topic, difficulty, platform, userProgressStatus, isBookmarked, hasNote } = problem

  return (
    <Link
      to={`/problems/${slug}`}
      className={clsx(
        'block bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-5',
        'hover:border-brand-300 dark:hover:border-brand-600 hover:shadow-card-hover',
        'transition-all duration-150 group'
      )}
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-2 mb-3">
        <DifficultyBadge difficulty={difficulty} />
        <div className="flex items-center gap-1.5">
          {isBookmarked && <span className="text-amber-500 text-sm" title="Bookmarked">🔖</span>}
          {hasNote      && <span className="text-blue-500  text-sm" title="Has note">📝</span>}
        </div>
      </div>

      {/* Title */}
      <h3 className="font-semibold text-slate-800 dark:text-slate-200 group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors line-clamp-2 leading-snug mb-2">
        {title}
      </h3>

      {/* Meta */}
      <div className="flex items-center justify-between gap-2 mt-auto pt-3 border-t border-slate-100 dark:border-slate-700">
        <div className="flex items-center gap-2 min-w-0">
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-400 truncate">
            {topic}
          </span>
          {platform?.name && (
            <span className="text-xs text-slate-400 dark:text-slate-500 truncate hidden sm:block">
              {platform.name}
            </span>
          )}
        </div>

        {userProgressStatus && (
          <span className={clsx(
            'shrink-0 text-xs font-medium px-2 py-0.5 rounded-full',
            getProgressBg(userProgressStatus)
          )}>
            {userProgressStatus === 'IN_PROGRESS' ? 'In Progress'
              : userProgressStatus === 'NOT_STARTED' ? 'Not Started'
              : userProgressStatus.charAt(0) + userProgressStatus.slice(1).toLowerCase()}
          </span>
        )}
      </div>
    </Link>
  )
}
