import { useState, useEffect } from 'react'

/**
 * Debounces a value by the given delay in milliseconds.
 * Useful for search inputs — prevents firing an API call on every keystroke.
 *
 * Usage: const debouncedSearch = useDebounce(searchValue, 400)
 */
export function useDebounce(value, delay = 400) {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])

  return debouncedValue
}
