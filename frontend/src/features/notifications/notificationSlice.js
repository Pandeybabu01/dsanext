import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import api, { getErrorMessage } from '../../lib/axios'

export const fetchNotifications = createAsyncThunk('notifications/fetchAll', async (params, { rejectWithValue }) => {
  try { const res = await api.get('/notifications', { params }); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const fetchUnreadCount = createAsyncThunk('notifications/unreadCount', async (_, { rejectWithValue }) => {
  try { const res = await api.get('/notifications/unread-count'); return res.data.data.unreadCount }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const markNotificationRead = createAsyncThunk('notifications/markRead', async (id, { rejectWithValue }) => {
  try { await api.patch(`/notifications/${id}/read`); return id }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const markAllNotificationsRead = createAsyncThunk('notifications/markAllRead', async (_, { rejectWithValue }) => {
  try { const res = await api.patch('/notifications/read-all'); return res.data.data }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

export const clearReadNotifications = createAsyncThunk('notifications/clearRead', async (_, { rejectWithValue }) => {
  try { await api.delete('/notifications/read'); return true }
  catch (err) { return rejectWithValue(getErrorMessage(err)) }
})

const notificationSlice = createSlice({
  name: 'notifications',
  initialState: {
    items: [], unreadCount: 0, totalElements: 0, totalPages: 0,
    isLoading: false, error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending,   (state) => { state.isLoading = true })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.isLoading     = false
        state.items         = action.payload.content
        state.totalElements = action.payload.totalElements
        state.totalPages    = action.payload.totalPages
      })
      .addCase(fetchNotifications.rejected,  (state, action) => { state.isLoading = false; state.error = action.payload })
      .addCase(fetchUnreadCount.fulfilled,   (state, action) => { state.unreadCount = action.payload })
      .addCase(markNotificationRead.fulfilled, (state, action) => {
        const n = state.items.find(n => n.id === action.payload)
        if (n && !n.isRead) { n.isRead = true; state.unreadCount = Math.max(0, state.unreadCount - 1) }
      })
      .addCase(markAllNotificationsRead.fulfilled, (state) => {
        state.items.forEach(n => { n.isRead = true }); state.unreadCount = 0
      })
      .addCase(clearReadNotifications.fulfilled, (state) => {
        state.items = state.items.filter(n => !n.isRead)
      })
  },
})

export const selectNotifications      = (state) => state.notifications.items
export const selectUnreadCount        = (state) => state.notifications.unreadCount
export const selectNotificationsLoading = (state) => state.notifications.isLoading
export default notificationSlice.reducer
