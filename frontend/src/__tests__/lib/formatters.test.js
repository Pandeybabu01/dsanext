import { describe, it, expect } from 'vitest'
import {
  formatDate, formatDateTime, formatRelative,
  formatNumber, formatPercent,
  truncate, toTitleCase, slugToTitle,
  getDifficultyColor, getDifficultyBg,
  getProgressColor, getProgressBg,
} from '../../lib/formatters'

describe('formatDate', () => {
  it('formats ISO date string to readable date', () => {
    expect(formatDate('2024-01-15T10:30:00Z')).toBe('Jan 15, 2024')
  })
  it('returns em dash for null', () => {
    expect(formatDate(null)).toBe('—')
  })
  it('returns em dash for undefined', () => {
    expect(formatDate(undefined)).toBe('—')
  })
})

describe('formatNumber', () => {
  it('returns 0 for null', () => {
    expect(formatNumber(null)).toBe('0')
  })
  it('returns number as-is for small values', () => {
    expect(formatNumber(42)).toBe('42')
    expect(formatNumber(999)).toBe('999')
  })
  it('abbreviates thousands with K suffix', () => {
    expect(formatNumber(1500)).toBe('1.5K')
    expect(formatNumber(10000)).toBe('10.0K')
  })
  it('abbreviates millions with M suffix', () => {
    expect(formatNumber(2_500_000)).toBe('2.5M')
  })
})

describe('formatPercent', () => {
  it('calculates percentage correctly', () => {
    expect(formatPercent(3, 10)).toBe('30%')
    expect(formatPercent(1, 4)).toBe('25%')
    expect(formatPercent(10, 10)).toBe('100%')
  })
  it('returns 0% when total is 0', () => {
    expect(formatPercent(5, 0)).toBe('0%')
  })
})

describe('truncate', () => {
  it('returns string unchanged if within limit', () => {
    expect(truncate('Short', 80)).toBe('Short')
  })
  it('truncates long strings with ellipsis', () => {
    const long = 'a'.repeat(100)
    const result = truncate(long, 80)
    expect(result).toHaveLength(81) // 80 chars + ellipsis
    expect(result.endsWith('…')).toBe(true)
  })
  it('returns empty string for null', () => {
    expect(truncate(null)).toBe('')
  })
})

describe('toTitleCase', () => {
  it('converts underscored string to title case', () => {
    expect(toTitleCase('IN_PROGRESS')).toBe('In Progress')
    expect(toTitleCase('NOT_STARTED')).toBe('Not Started')
  })
  it('handles regular strings', () => {
    expect(toTitleCase('hello world')).toBe('Hello World')
  })
})

describe('slugToTitle', () => {
  it('converts slug to title', () => {
    expect(slugToTitle('two-sum')).toBe('Two Sum')
    expect(slugToTitle('dynamic-programming')).toBe('Dynamic Programming')
    expect(slugToTitle('trapping-rain-water')).toBe('Trapping Rain Water')
  })
  it('returns empty string for null', () => {
    expect(slugToTitle(null)).toBe('')
  })
})

describe('getDifficultyColor', () => {
  it('returns green class for EASY', () => {
    expect(getDifficultyColor('EASY')).toContain('green')
  })
  it('returns amber class for MEDIUM', () => {
    expect(getDifficultyColor('MEDIUM')).toContain('amber')
  })
  it('returns red class for HARD', () => {
    expect(getDifficultyColor('HARD')).toContain('red')
  })
  it('is case-insensitive', () => {
    expect(getDifficultyColor('easy')).toContain('green')
  })
  it('returns slate for unknown difficulty', () => {
    expect(getDifficultyColor(null)).toContain('slate')
  })
})

describe('getDifficultyBg', () => {
  it('returns EASY background classes', () => {
    const cls = getDifficultyBg('EASY')
    expect(cls).toContain('bg-green-100')
    expect(cls).toContain('text-green-800')
  })
  it('returns MEDIUM background classes', () => {
    const cls = getDifficultyBg('MEDIUM')
    expect(cls).toContain('bg-amber-100')
  })
  it('returns HARD background classes', () => {
    const cls = getDifficultyBg('HARD')
    expect(cls).toContain('bg-red-100')
  })
})

describe('getProgressBg', () => {
  it('returns green classes for SOLVED', () => {
    expect(getProgressBg('SOLVED')).toContain('green')
  })
  it('returns blue classes for IN_PROGRESS', () => {
    expect(getProgressBg('IN_PROGRESS')).toContain('blue')
  })
  it('returns amber classes for REVISIT', () => {
    expect(getProgressBg('REVISIT')).toContain('amber')
  })
  it('returns slate classes for NOT_STARTED', () => {
    expect(getProgressBg('NOT_STARTED')).toContain('slate')
  })
})
