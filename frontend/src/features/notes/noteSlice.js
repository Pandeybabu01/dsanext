import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'

export const fetchNotes = createAsyncThunk('notes/fetchAll', async (params, { rejectWithValue }) => {
  try { const res = await api.get('/notes', { params }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const fetchNoteForProblem = createAsyncThunk('notes/fetchOne', async (problemId, { rejectWithValue }) => {
  try { const res = await api.get(`/notes/problem/${problemId}`); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const saveNote = createAsyncThunk('notes/save', async ({ problemId, content }, { rejectWithValue }) => {
  try { const res = await api.put(`/notes/problem/${problemId}`, { content }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const deleteNote = createAsyncThunk('notes/delete', async (problemId, { rejectWithValue }) => {
  try { await api.delete(`/notes/problem/${problemId}`); return problemId }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

const noteSlice = createSlice({
  name: 'notes',
  initialState: { items: [], currentNote: null, totalElements: 0, totalPages: 0, isLoading: false, error: null },
  reducers: { clearCurrentNote(state) { state.currentNote = null } },
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotes.pending,   (state) => { state.isLoading = true })
      .addCase(fetchNotes.fulfilled, (state, action) => {
        state.isLoading = false; state.items = action.payload.content
        state.totalElements = action.payload.totalElements; state.totalPages = action.payload.totalPages
      })
      .addCase(fetchNotes.rejected,  (state, action) => { state.isLoading = false; state.error = action.payload })
      .addCase(fetchNoteForProblem.fulfilled, (state, action) => { state.currentNote = action.payload })
      .addCase(saveNote.fulfilled, (state, action) => {
        state.currentNote = action.payload
        const idx = state.items.findIndex(n => n.problemId === action.payload.problemId)
        if (idx >= 0) state.items[idx] = action.payload
        else state.items.unshift(action.payload)
      })
      .addCase(deleteNote.fulfilled, (state, action) => {
        state.currentNote = null
        state.items = state.items.filter(n => n.problemId !== action.payload)
      })
  },
})
export const { clearCurrentNote } = noteSlice.actions
export const selectNotes        = (state) => state.notes.items
export const selectCurrentNote  = (state) => state.notes.currentNote
export const selectNotesLoading = (state) => state.notes.isLoading
export default noteSlice.reducer
