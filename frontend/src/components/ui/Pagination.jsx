import clsx from 'clsx'
import Button from './Button'

export default function Pagination({ page, totalPages, totalElements, size, onPageChange }) {
  if (totalPages <= 1) return null

  const start = page * size + 1
  const end   = Math.min((page + 1) * size, totalElements)

  const pages = Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
    if (totalPages <= 7) return i
    if (page < 4) return i
    if (page > totalPages - 5) return totalPages - 7 + i
    return page - 3 + i
  })

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6">
      <p className="text-sm text-slate-500 dark:text-slate-400">
        Showing <span className="font-medium text-slate-700 dark:text-slate-300">{start}–{end}</span>
        {' '}of <span className="font-medium text-slate-700 dark:text-slate-300">{totalElements}</span> results
      </p>

      <div className="flex items-center gap-1">
        <Button
          variant="outline" size="sm"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          aria-label="Previous page"
        >
          ←
        </Button>

        {pages.map((p) => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            className={clsx(
              'w-8 h-8 text-sm rounded-lg font-medium transition-colors',
              p === page
                ? 'bg-brand-600 text-white'
                : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
            )}
          >
            {p + 1}
          </button>
        ))}

        <Button
          variant="outline" size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          aria-label="Next page"
        >
          →
        </Button>
      </div>
    </div>
  )
}
