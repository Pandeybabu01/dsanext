import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from '../components/guards/ProtectedRoute'
import AdminRoute     from '../components/guards/AdminRoute'
import Spinner        from '../components/ui/Spinner'
import UserLayout     from '../components/layout/UserLayout'
import AdminLayout    from '../components/layout/AdminLayout'

// ── Auth pages ─────────────────────────────────────────────
const LoginPage    = lazy(() => import('../features/auth/pages/LoginPage'))
const RegisterPage = lazy(() => import('../features/auth/pages/RegisterPage'))

// ── User pages ─────────────────────────────────────────────
const DashboardPage     = lazy(() => import('../features/dashboard/pages/DashboardPage'))
const ProblemListPage   = lazy(() => import('../features/problems/pages/ProblemListPage'))
const ProblemDetailPage = lazy(() => import('../features/problems/pages/ProblemDetailPage'))
const ProgressPage      = lazy(() => import('../features/progress/pages/ProgressPage'))
const NotesPage         = lazy(() => import('../features/notes/pages/NotesPage'))
const BookmarksPage     = lazy(() => import('../features/bookmarks/pages/BookmarksPage'))
const AnalyticsPage     = lazy(() => import('../features/analytics/pages/AnalyticsPage'))
const NotificationsPage = lazy(() => import('../features/notifications/pages/NotificationsPage'))
const UserSettingsPage  = lazy(() => import('../features/settings/pages/UserSettingsPage'))

// ── Admin pages ────────────────────────────────────────────
const AdminDashboardPage = lazy(() => import('../features/admin/pages/AdminDashboardPage'))
const AdminUsersPage     = lazy(() => import('../features/admin/pages/AdminUsersPage'))
const AdminProblemsPage  = lazy(() => import('../features/admin/pages/AdminProblemsPage'))
const AdminPlatformsPage = lazy(() => import('../features/admin/pages/AdminPlatformsPage'))
const AdminSettingsPage  = lazy(() => import('../features/admin/pages/AdminSettingsPage'))
const AdminLogsPage      = lazy(() => import('../features/admin/pages/AdminLogsPage'))

const PageLoader = () => (
  <div className="flex items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-900">
    <Spinner size="lg" />
  </div>
)

export default function AppRouter() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>

        {/* Public */}
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/"         element={<Navigate to="/dashboard" replace />} />

        {/* User (JWT required) */}
        <Route element={<ProtectedRoute><UserLayout /></ProtectedRoute>}>
          <Route path="/dashboard"      element={<DashboardPage />} />
          <Route path="/problems"       element={<ProblemListPage />} />
          <Route path="/problems/:slug" element={<ProblemDetailPage />} />
          <Route path="/progress"       element={<ProgressPage />} />
          <Route path="/notes"          element={<NotesPage />} />
          <Route path="/bookmarks"      element={<BookmarksPage />} />
          <Route path="/analytics"      element={<AnalyticsPage />} />
          <Route path="/notifications"  element={<NotificationsPage />} />
          <Route path="/settings"       element={<UserSettingsPage />} />
        </Route>

        {/* Admin (ADMIN role required) */}
        <Route element={<AdminRoute><AdminLayout /></AdminRoute>}>
          <Route path="/admin"           element={<AdminDashboardPage />} />
          <Route path="/admin/users"     element={<AdminUsersPage />} />
          <Route path="/admin/problems"  element={<AdminProblemsPage />} />
          <Route path="/admin/platforms" element={<AdminPlatformsPage />} />
          <Route path="/admin/settings"  element={<AdminSettingsPage />} />
          <Route path="/admin/logs"      element={<AdminLogsPage />} />
        </Route>

        {/* 404 */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />

      </Routes>
    </Suspense>
  )
}
