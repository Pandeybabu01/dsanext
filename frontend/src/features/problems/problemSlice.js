import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'

export const fetchProblems = createAsyncThunk(
  'problems/fetchAll',
  async (params, { rejectWithValue }) => {
    try {
      const res = await api.get('/problems', { params })
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

export const fetchProblemBySlug = createAsyncThunk(
  'problems/fetchBySlug',
  async (slug, { rejectWithValue }) => {
    try {
      const res = await api.get(`/problems/${slug}`)
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

export const fetchTopics = createAsyncThunk(
  'problems/fetchTopics',
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get('/problems/topics')
      return res.data.data
    } catch (err) {
      return rejectWithValue(getErrorMessage(err))
    }
  }
)

const problemSlice = createSlice({
  name: 'problems',
  initialState: {
    items:        [],
    currentProblem: null,
    topics:       [],
    totalElements: 0,
    totalPages:   0,
    currentPage:  0,
    isLoading:    false,
    error:        null,
    filters: {
      search:     '',
      difficulty: '',
      topic:      '',
      platformId: '',
    },
  },
  reducers: {
    setFilters(state, action) {
      state.filters = { ...state.filters, ...action.payload }
    },
    clearFilters(state) {
      state.filters = { search: '', difficulty: '', topic: '', platformId: '' }
    },
    clearCurrentProblem(state) {
      state.currentProblem = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProblems.pending, (state) => { state.isLoading = true; state.error = null })
      .addCase(fetchProblems.fulfilled, (state, action) => {
        state.isLoading    = false
        state.items        = action.payload.content
        state.totalElements = action.payload.totalElements
        state.totalPages   = action.payload.totalPages
        state.currentPage  = action.payload.page
      })
      .addCase(fetchProblems.rejected, (state, action) => {
        state.isLoading = false; state.error = action.payload
      })
      .addCase(fetchProblemBySlug.pending, (state) => { state.isLoading = true })
      .addCase(fetchProblemBySlug.fulfilled, (state, action) => {
        state.isLoading     = false
        state.currentProblem = action.payload
      })
      .addCase(fetchProblemBySlug.rejected, (state, action) => {
        state.isLoading = false; state.error = action.payload
      })
      .addCase(fetchTopics.fulfilled, (state, action) => {
        state.topics = action.payload
      })
  },
})

export const { setFilters, clearFilters, clearCurrentProblem } = problemSlice.actions
export const selectProblems      = (state) => state.problems.items
export const selectCurrentProblem = (state) => state.problems.currentProblem
export const selectTopics        = (state) => state.problems.topics
export const selectProblemsMeta  = (state) => ({
  totalElements: state.problems.totalElements,
  totalPages:    state.problems.totalPages,
  currentPage:   state.problems.currentPage,
})
export const selectProblemsLoading = (state) => state.problems.isLoading
export const selectProblemFilters  = (state) => state.problems.filters
export default problemSlice.reducer
