import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'

export const fetchProgress = createAsyncThunk('progress/fetchAll', async (params, { rejectWithValue }) => {
  try { const res = await api.get('/progress', { params }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const upsertProgress = createAsyncThunk('progress/upsert', async ({ problemId, status }, { rejectWithValue }) => {
  try { const res = await api.put(`/progress/${problemId}`, { status }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

const progressSlice = createSlice({
  name: 'progress',
  initialState: { items: [], totalElements: 0, totalPages: 0, isLoading: false, error: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchProgress.pending,   (state) => { state.isLoading = true })
      .addCase(fetchProgress.fulfilled, (state, action) => {
        state.isLoading    = false
        state.items        = action.payload.content
        state.totalElements = action.payload.totalElements
        state.totalPages   = action.payload.totalPages
      })
      .addCase(fetchProgress.rejected,  (state, action) => { state.isLoading = false; state.error = action.payload })
      .addCase(upsertProgress.fulfilled, (state, action) => {
        const idx = state.items.findIndex(p => p.problemId === action.payload.problemId)
        if (idx >= 0) state.items[idx] = action.payload
        else state.items.unshift(action.payload)
      })
  },
})
export const selectProgress        = (state) => state.progress.items
export const selectProgressLoading = (state) => state.progress.isLoading
export default progressSlice.reducer
