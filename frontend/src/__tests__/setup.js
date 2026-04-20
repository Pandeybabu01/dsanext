import '@testing-library/jest-dom'
import { vi } from 'vitest'

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error:   vi.fn(),
    loading: vi.fn(),
    dismiss: vi.fn(),
    promise: vi.fn(),
  },
  Toaster: () => null,
}))

// Mock react-router-dom navigate
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => vi.fn(),
    useParams:   () => ({ slug: 'two-sum' }),
    useLocation: () => ({ pathname: '/problems', state: null }),
  }
})

// Silence console.error in tests (React warnings etc.)
const originalError = console.error
beforeAll(() => {
  console.error = (...args) => {
    if (args[0]?.includes?.('Warning:')) return
    originalError(...args)
  }
})
afterAll(() => { console.error = originalError })

// Clean up after each test
afterEach(() => { vi.clearAllMocks() })
