import { describe, it, expect, beforeEach, vi } from 'vitest'
import { configureStore } from '@reduxjs/toolkit'
import authReducer, {
  logout, setTheme, clearError, updateUserState,
  selectUser, selectToken, selectIsAuth, selectIsAdmin, selectTheme,
} from '../../features/auth/authSlice'

// Mock localStorage
const localStorageMock = (() => {
  let store = {}
  return {
    getItem:    (key) => store[key] ?? null,
    setItem:    (key, val) => { store[key] = String(val) },
    removeItem: (key) => { delete store[key] },
    clear:      () => { store = {} },
  }
})()
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

const makeStore = (preloadedState) =>
  configureStore({ reducer: { auth: authReducer }, preloadedState })

const MOCK_USER  = { id: 'uuid-1', email: 'test@test.com', role: 'USER', fullName: 'Test User' }
const MOCK_ADMIN = { id: 'uuid-2', email: 'admin@test.com', role: 'ADMIN', fullName: 'Admin' }
const MOCK_TOKEN = 'mock.jwt.token'

describe('authSlice reducers', () => {

  beforeEach(() => localStorage.clear())

  // ── Initial state ────────────────────────────────────────────

  it('has correct initial state when no localStorage', () => {
    const store = makeStore()
    const state = store.getState().auth
    expect(state.user).toBeNull()
    expect(state.token).toBeNull()
    expect(state.isLoading).toBe(false)
    expect(state.error).toBeNull()
  })

  // ── logout ────────────────────────────────────────────────────

  it('logout: clears user, token, and localStorage', () => {
    localStorage.setItem('dsanext_token', MOCK_TOKEN)
    localStorage.setItem('dsanext_user',  JSON.stringify(MOCK_USER))

    const store = makeStore({
      auth: { user: MOCK_USER, token: MOCK_TOKEN, isLoading: false, error: null, theme: 'light' }
    })

    store.dispatch(logout())

    const state = store.getState().auth
    expect(state.user).toBeNull()
    expect(state.token).toBeNull()
    expect(localStorage.getItem('dsanext_token')).toBeNull()
    expect(localStorage.getItem('dsanext_user')).toBeNull()
  })

  // ── setTheme ──────────────────────────────────────────────────

  it('setTheme: updates theme in state', () => {
    const store = makeStore({
      auth: { user: MOCK_USER, token: MOCK_TOKEN, isLoading: false, error: null, theme: 'light' }
    })

    store.dispatch(setTheme('dark'))

    expect(store.getState().auth.theme).toBe('dark')
  })

  it('setTheme: persists theme in user object in localStorage', () => {
    const store = makeStore({
      auth: { user: MOCK_USER, token: MOCK_TOKEN, isLoading: false, error: null, theme: 'light' }
    })

    store.dispatch(setTheme('system'))

    const savedUser = JSON.parse(localStorage.getItem('dsanext_user') ?? '{}')
    expect(savedUser.theme).toBe('system')
  })

  // ── clearError ────────────────────────────────────────────────

  it('clearError: resets error to null', () => {
    const store = makeStore({
      auth: { user: null, token: null, isLoading: false, error: 'Some error', theme: 'light' }
    })

    store.dispatch(clearError())

    expect(store.getState().auth.error).toBeNull()
  })

  // ── updateUserState ───────────────────────────────────────────

  it('updateUserState: merges partial user data', () => {
    const store = makeStore({
      auth: { user: MOCK_USER, token: MOCK_TOKEN, isLoading: false, error: null, theme: 'light' }
    })

    store.dispatch(updateUserState({ fullName: 'Updated Name' }))

    const { user } = store.getState().auth
    expect(user.fullName).toBe('Updated Name')
    expect(user.email).toBe(MOCK_USER.email) // unchanged
  })
})

describe('authSlice selectors', () => {

  it('selectIsAuth — true when both token and user present', () => {
    const state = { auth: { user: MOCK_USER, token: MOCK_TOKEN } }
    expect(selectIsAuth(state)).toBe(true)
  })

  it('selectIsAuth — false when token missing', () => {
    const state = { auth: { user: MOCK_USER, token: null } }
    expect(selectIsAuth(state)).toBe(false)
  })

  it('selectIsAdmin — true for ADMIN role', () => {
    const state = { auth: { user: MOCK_ADMIN, token: MOCK_TOKEN } }
    expect(selectIsAdmin(state)).toBe(true)
  })

  it('selectIsAdmin — false for USER role', () => {
    const state = { auth: { user: MOCK_USER, token: MOCK_TOKEN } }
    expect(selectIsAdmin(state)).toBe(false)
  })

  it('selectTheme — returns current theme', () => {
    expect(selectTheme({ auth: { theme: 'dark' } })).toBe('dark')
    expect(selectTheme({ auth: { theme: 'system' } })).toBe('system')
  })

  it('selectTheme — defaults to light when undefined', () => {
    expect(selectTheme({ auth: { theme: undefined } })).toBe('light')
  })
})
