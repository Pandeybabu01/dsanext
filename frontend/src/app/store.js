import { configureStore } from '@reduxjs/toolkit'
import authReducer         from '../features/auth/authSlice'
import problemReducer      from '../features/problems/problemSlice'
import progressReducer     from '../features/progress/progressSlice'
import noteReducer         from '../features/notes/noteSlice'
import bookmarkReducer     from '../features/bookmarks/bookmarkSlice'
import notificationReducer from '../features/notifications/notificationSlice'

/**
 * DSANext Redux Store.
 * Each feature owns its slice — no shared global state between features.
 */
export const store = configureStore({
  reducer: {
    auth:          authReducer,
    problems:      problemReducer,
    progress:      progressReducer,
    notes:         noteReducer,
    bookmarks:     bookmarkReducer,
    notifications: notificationReducer,
  },

  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore non-serializable Date values in action payloads
        ignoredActionPaths: ['payload.createdAt', 'payload.updatedAt', 'payload.solvedAt'],
      },
    }),

  devTools: import.meta.env.DEV,
})
