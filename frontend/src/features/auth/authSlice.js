import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'
import { TOKEN_KEY, USER_KEY } from '../../lib/constants'

// ── Async thunks ──────────────────────────────────────────

export const loginUser = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const res = await api.post('/auth/login', credentials)
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

export const registerUser = createAsyncThunk(
  'auth/register',
  async (userData, { rejectWithValue }) => {
    try {
      const res = await api.post('/auth/register', userData)
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

export const fetchCurrentUser = createAsyncThunk(
  'auth/me',
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get('/auth/me')
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

export const updateProfile = createAsyncThunk(
  'auth/updateProfile',
  async (data, { rejectWithValue }) => {
    try {
      const res = await api.put('/users/profile', data)
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

// ── Helpers — hydrate from localStorage ──────────────────

const loadPersistedAuth = () => {
  try {
    const token = localStorage.getItem(TOKEN_KEY)
    const user  = localStorage.getItem(USER_KEY)
    if (token && user) {
      return { token, user: JSON.parse(user) }
    }
  } catch {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
  return { token: null, user: null }
}

const { token: persistedToken, user: persistedUser } = loadPersistedAuth()

// ── Slice ─────────────────────────────────────────────────

const authSlice = createSlice({
  name: 'auth',

  initialState: {
    user:        persistedUser,
    token:       persistedToken,
    isLoading:   false,
    error:       null,
    theme:       persistedUser?.theme ?? 'light',
  },

  reducers: {
    logout(state) {
      state.user    = null
      state.token   = null
      state.error   = null
      state.theme   = 'light'
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },

    setTheme(state, action) {
      state.theme = action.payload
      if (state.user) {
        state.user = { ...state.user, theme: action.payload }
        localStorage.setItem(USER_KEY, JSON.stringify(state.user))
      }
    },

    clearError(state) {
      state.error = null
    },

    updateUserState(state, action) {
      state.user = { ...state.user, ...action.payload }
      localStorage.setItem(USER_KEY, JSON.stringify(state.user))
    },
  },

  extraReducers: (builder) => {
    // ── Login ──────────────────────────────────────────────
    builder
      .addCase(loginUser.pending, (state) => {
        state.isLoading = true
        state.error     = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.isLoading = false
        state.token     = action.payload.accessToken
        state.user      = action.payload.user
        state.theme     = action.payload.user?.theme ?? 'light'
        localStorage.setItem(TOKEN_KEY, action.payload.accessToken)
        localStorage.setItem(USER_KEY,  JSON.stringify(action.payload.user))
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.isLoading = false
        state.error     = action.payload
      })

    // ── Register ───────────────────────────────────────────
    builder
      .addCase(registerUser.pending, (state) => {
        state.isLoading = true
        state.error     = null
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.isLoading = false
        state.token     = action.payload.accessToken
        state.user      = action.payload.user
        localStorage.setItem(TOKEN_KEY, action.payload.accessToken)
        localStorage.setItem(USER_KEY,  JSON.stringify(action.payload.user))
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.isLoading = false
        state.error     = action.payload
      })

    // ── Fetch current user ─────────────────────────────────
    builder
      .addCase(fetchCurrentUser.fulfilled, (state, action) => {
        state.user = action.payload
        localStorage.setItem(USER_KEY, JSON.stringify(action.payload))
      })
      .addCase(fetchCurrentUser.rejected, (state) => {
        // Token invalid — clear auth
        state.user  = null
        state.token = null
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
      })

    // ── Update profile ─────────────────────────────────────
    builder
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.user = { ...state.user, ...action.payload }
        localStorage.setItem(USER_KEY, JSON.stringify(state.user))
      })
  },
})

export const { logout, setTheme, clearError, updateUserState } = authSlice.actions

// ── Selectors ─────────────────────────────────────────────
export const selectUser        = (state) => state.auth.user
export const selectToken       = (state) => state.auth.token
export const selectIsAuth      = (state) => !!state.auth.token && !!state.auth.user
export const selectIsAdmin     = (state) => state.auth.user?.role === 'ADMIN'
export const selectAuthLoading = (state) => state.auth.isLoading
export const selectAuthError   = (state) => state.auth.error
export const selectTheme       = (state) => state.auth.theme ?? 'light'

export default authSlice.reducer
