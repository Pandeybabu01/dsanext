// ── App ───────────────────────────────────────────────────
export const APP_NAME    = import.meta.env.VITE_APP_NAME || 'DSANext'
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0'

// ── Auth ──────────────────────────────────────────────────
export const TOKEN_KEY = 'dsanext_token'
export const USER_KEY  = 'dsanext_user'

// ── Difficulty ────────────────────────────────────────────
export const DIFFICULTY = {
  EASY:   { label: 'Easy',   color: 'easy',   dot: '🟢' },
  MEDIUM: { label: 'Medium', color: 'medium', dot: '🟡' },
  HARD:   { label: 'Hard',   color: 'hard',   dot: '🔴' },
}

export const DIFFICULTY_OPTIONS = [
  { value: '',       label: 'All Difficulties' },
  { value: 'EASY',   label: '🟢 Easy' },
  { value: 'MEDIUM', label: '🟡 Medium' },
  { value: 'HARD',   label: '🔴 Hard' },
]

// ── Progress Status ───────────────────────────────────────
export const PROGRESS_STATUS = {
  NOT_STARTED: { label: 'Not Started', color: 'slate' },
  IN_PROGRESS: { label: 'In Progress', color: 'blue'  },
  SOLVED:      { label: 'Solved',      color: 'green' },
  REVISIT:     { label: 'Revisit',     color: 'amber' },
}

export const PROGRESS_OPTIONS = [
  { value: '',            label: 'All Statuses'  },
  { value: 'NOT_STARTED', label: 'Not Started'   },
  { value: 'IN_PROGRESS', label: 'In Progress'   },
  { value: 'SOLVED',      label: 'Solved'        },
  { value: 'REVISIT',     label: 'Revisit'       },
]

// ── DSA Topics ────────────────────────────────────────────
export const TOPICS = [
  'Array', 'String', 'Linked List', 'Stack', 'Queue',
  'Binary Tree', 'Binary Search Tree', 'Graph',
  'Dynamic Programming', 'Greedy', 'Backtracking',
  'Binary Search', 'Sliding Window', 'Two Pointers',
  'Divide and Conquer', 'Heap', 'Trie',
  'Math', 'Bit Manipulation', 'Matrix',
]

// ── Roles ─────────────────────────────────────────────────
export const ROLES = {
  USER:  'USER',
  ADMIN: 'ADMIN',
}

// ── Pagination ────────────────────────────────────────────
export const DEFAULT_PAGE_SIZE = 20
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

// ── Notification types ────────────────────────────────────
export const NOTIFICATION_TYPES = {
  INFO:    { label: 'Info',    color: 'blue'  },
  SUCCESS: { label: 'Success', color: 'green' },
  WARNING: { label: 'Warning', color: 'amber' },
  SYSTEM:  { label: 'System',  color: 'slate' },
}

// ── Routes ────────────────────────────────────────────────
export const ROUTES = {
  HOME:            '/',
  LOGIN:           '/login',
  REGISTER:        '/register',
  DASHBOARD:       '/dashboard',
  PROBLEMS:        '/problems',
  PROBLEM_DETAIL:  '/problems/:slug',
  PROGRESS:        '/progress',
  NOTES:           '/notes',
  BOOKMARKS:       '/bookmarks',
  ANALYTICS:       '/analytics',
  SETTINGS:        '/settings',
  ADMIN:           '/admin',
  ADMIN_USERS:     '/admin/users',
  ADMIN_PROBLEMS:  '/admin/problems',
  ADMIN_PLATFORMS: '/admin/platforms',
  ADMIN_SETTINGS:  '/admin/settings',
  ADMIN_LOGS:      '/admin/logs',
}
