import { useState, useCallback } from 'react'

/**
 * Pagination state management hook.
 * Usage: const { page, size, goToPage, nextPage, prevPage, setPageSize } = usePagination()
 */
export function usePagination(initialSize = 20) {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(initialSize)

  const goToPage   = useCallback((p) => setPage(p), [])
  const nextPage   = useCallback(() => setPage(p => p + 1), [])
  const prevPage   = useCallback(() => setPage(p => Math.max(0, p - 1)), [])
  const setPageSize = useCallback((s) => { setSize(s); setPage(0) }, [])
  const reset      = useCallback(() => setPage(0), [])

  return { page, size, goToPage, nextPage, prevPage, setPageSize, reset }
}
