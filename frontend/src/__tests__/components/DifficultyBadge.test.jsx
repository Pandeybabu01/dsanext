import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import DifficultyBadge from '../../components/ui/DifficultyBadge'

describe('DifficultyBadge', () => {

  it('renders EASY with green styling and dot', () => {
    render(<DifficultyBadge difficulty="EASY" />)
    const badge = screen.getByText('Easy')
    expect(badge).toBeInTheDocument()
    expect(badge.closest('span')).toHaveClass('bg-green-100')
  })

  it('renders MEDIUM with amber styling and dot', () => {
    render(<DifficultyBadge difficulty="MEDIUM" />)
    const badge = screen.getByText('Medium')
    expect(badge).toBeInTheDocument()
    expect(badge.closest('span')).toHaveClass('bg-amber-100')
  })

  it('renders HARD with red styling and dot', () => {
    render(<DifficultyBadge difficulty="HARD" />)
    const badge = screen.getByText('Hard')
    expect(badge).toBeInTheDocument()
    expect(badge.closest('span')).toHaveClass('bg-red-100')
  })

  it('shows emoji dot by default', () => {
    render(<DifficultyBadge difficulty="EASY" />)
    expect(screen.getByText('🟢')).toBeInTheDocument()
  })

  it('hides dot when showDot=false', () => {
    render(<DifficultyBadge difficulty="EASY" showDot={false} />)
    expect(screen.queryByText('🟢')).not.toBeInTheDocument()
  })

  it('applies sm size class', () => {
    const { container } = render(<DifficultyBadge difficulty="HARD" size="sm" />)
    expect(container.firstChild).toHaveClass('text-xs')
  })

  it('is case-insensitive for difficulty prop', () => {
    render(<DifficultyBadge difficulty="easy" />)
    expect(screen.getByText('Easy')).toBeInTheDocument()
  })

  it('returns null for undefined difficulty', () => {
    const { container } = render(<DifficultyBadge difficulty={undefined} />)
    expect(container.firstChild).toBeNull()
  })
})
