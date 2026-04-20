import { format, formatDistanceToNow, parseISO } from 'date-fns'

// ── Date formatting ───────────────────────────────────────

export const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  try {
    return format(parseISO(dateStr), 'MMM d, yyyy')
  } catch {
    return '—'
  }
}

export const formatDateTime = (dateStr) => {
  if (!dateStr) return '—'
  try {
    return format(parseISO(dateStr), 'MMM d, yyyy · h:mm a')
  } catch {
    return '—'
  }
}

export const formatRelative = (dateStr) => {
  if (!dateStr) return '—'
  try {
    return formatDistanceToNow(parseISO(dateStr), { addSuffix: true })
  } catch {
    return '—'
  }
}

// ── Number formatting ─────────────────────────────────────

export const formatNumber = (n) => {
  if (n == null) return '0'
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`
  if (n >= 1_000)     return `${(n / 1_000).toFixed(1)}K`
  return n.toString()
}

export const formatPercent = (value, total) => {
  if (!total || total === 0) return '0%'
  return `${Math.round((value / total) * 100)}%`
}

// ── String formatting ─────────────────────────────────────

export const truncate = (str, maxLen = 80) => {
  if (!str) return ''
  return str.length > maxLen ? `${str.slice(0, maxLen)}…` : str
}

export const toTitleCase = (str) => {
  if (!str) return ''
  return str.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())
}

export const slugToTitle = (slug) => {
  if (!slug) return ''
  return slug.split('-').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
}

// ── Difficulty helpers ────────────────────────────────────

export const getDifficultyColor = (difficulty) => {
  switch (difficulty?.toUpperCase()) {
    case 'EASY':   return 'text-green-600 dark:text-green-400'
    case 'MEDIUM': return 'text-amber-600 dark:text-amber-400'
    case 'HARD':   return 'text-red-600 dark:text-red-400'
    default:       return 'text-slate-500'
  }
}

export const getDifficultyBg = (difficulty) => {
  switch (difficulty?.toUpperCase()) {
    case 'EASY':   return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
    case 'MEDIUM': return 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400'
    case 'HARD':   return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
    default:       return 'bg-slate-100 text-slate-600'
  }
}

// ── Progress status helpers ───────────────────────────────

export const getProgressColor = (status) => {
  switch (status) {
    case 'SOLVED':      return 'text-green-600 dark:text-green-400'
    case 'IN_PROGRESS': return 'text-blue-600 dark:text-blue-400'
    case 'REVISIT':     return 'text-amber-600 dark:text-amber-400'
    default:            return 'text-slate-400'
  }
}

export const getProgressBg = (status) => {
  switch (status) {
    case 'SOLVED':      return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
    case 'IN_PROGRESS': return 'bg-blue-100  text-blue-800  dark:bg-blue-900/30  dark:text-blue-400'
    case 'REVISIT':     return 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400'
    default:            return 'bg-slate-100 text-slate-500  dark:bg-slate-700   dark:text-slate-400'
  }
}
