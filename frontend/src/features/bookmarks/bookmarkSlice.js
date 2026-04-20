import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'

export const fetchBookmarks = createAsyncThunk('bookmarks/fetchAll', async (params, { rejectWithValue }) => {
  try { const res = await api.get('/bookmarks', { params }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const toggleBookmark = createAsyncThunk('bookmarks/toggle', async (problemId, { rejectWithValue }) => {
  try { const res = await api.post(`/bookmarks/${problemId}/toggle`); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const removeBookmark = createAsyncThunk('bookmarks/remove', async (problemId, { rejectWithValue }) => {
  try { await api.delete(`/bookmarks/${problemId}`); return problemId }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

const bookmarkSlice = createSlice({
  name: 'bookmarks',
  initialState: {
    items: [], totalElements: 0, totalPages: 0,
    isLoading: false, error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchBookmarks.pending,   (state) => { state.isLoading = true })
      .addCase(fetchBookmarks.fulfilled, (state, action) => {
        state.isLoading     = false
        state.items         = action.payload.content
        state.totalElements = action.payload.totalElements
        state.totalPages    = action.payload.totalPages
      })
      .addCase(fetchBookmarks.rejected,  (state, action) => { state.isLoading = false; state.error = action.payload })
      .addCase(removeBookmark.fulfilled, (state, action) => {
        state.items = state.items.filter(b => b.problem?.id !== action.payload)
      })
  },
})

export const selectBookmarks        = (state) => state.bookmarks.items
export const selectBookmarksLoading = (state) => state.bookmarks.isLoading
export default bookmarkSlice.reducer
