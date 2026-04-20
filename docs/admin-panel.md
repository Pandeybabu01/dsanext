# DSANext — Admin Panel Documentation

## Accessing the Admin Panel

1. Login with an admin account (default: `admin@dsanext.com` / `Admin@123`)
2. Navigate to `/admin` — you are automatically redirected if you have `ROLE_ADMIN`
3. The admin sidebar replaces the user sidebar with admin-specific navigation

Non-admin users attempting to access `/admin/**` are silently redirected to `/dashboard`.

---

## Admin Dashboard

**Route:** `/admin`
**API:** `GET /api/analytics/admin`

Displays:
- Total users (active vs inactive)
- Total problems (active vs inactive)
- Total progress entries across all users
- Problems by difficulty (pie chart)
- Users by role (pie chart)
- Platform activity — daily solved count (bar chart, last 14 days)
- Quick action links to all admin sections

---

## User Management

**Route:** `/admin/users`
**APIs:** `GET /api/admin/users`, `PATCH /api/admin/users/{id}/block`, etc.

### Search and Filters

- Search by **full name**, **email**, or **username** (debounced, case-insensitive)
- Filter by **role** (USER / ADMIN)
- Filter by **active status** (Active / Blocked)
- Paginated results (default 20 per page)

### Actions (per user row)

| Action | API | Restriction |
|--------|-----|-------------|
| Block | `PATCH /admin/users/{id}/block` | Cannot block admin users |
| Unblock | `PATCH /admin/users/{id}/unblock` | No restriction |
| Make Admin | `PATCH /admin/users/{id}/role?role=ADMIN` | Cannot change own role |
| Make User | `PATCH /admin/users/{id}/role?role=USER` | Cannot change own role |
| Delete | `DELETE /admin/users/{id}` | Cannot delete admin accounts |

All destructive actions require confirmation via modal dialog.

---

## Problem Management

**Route:** `/admin/problems`
**APIs:** `GET/POST/PUT/DELETE /api/admin/problems`

### Create Problem

Required fields:
- **Title** — 3 to 300 characters
- **Topic** — select from 20 DSA topic categories
- **Difficulty** — `EASY` / `MEDIUM` / `HARD` (mandatory, displayed as 🟢 / 🟡 / 🔴)

Optional fields:
- **Description** — full problem statement (up to 50,000 chars)
- **Platform** — link to LeetCode, Codeforces, etc.
- **External URL** — direct link to the problem on the platform

Slug is auto-generated from the title (e.g., "Two Sum" → `two-sum`). If a slug already exists, a numeric suffix is appended (`two-sum-2`).

### Edit Problem

All fields editable. The `isActive` toggle allows hiding problems from users without deleting them.

### Delete Problem

Cascade deletes all associated:
- Progress entries (all users)
- Notes (all users)
- Bookmarks (all users)

A confirmation modal displays the problem title and warns about cascade effects.

---

## Platform Management

**Route:** `/admin/platforms`
**APIs:** `GET/POST/PUT/DELETE /api/platforms`

Platforms are displayed as cards showing the platform name, base URL, icon (with fallback), and active status.

### Fields

| Field | Required | Description |
|-------|----------|-------------|
| Name | Yes | Display name (e.g., "LeetCode") |
| Base URL | Yes | Root URL for problem links |
| Icon URL | No | Favicon or logo URL |
| Active | Yes | Whether the platform appears in filters |

Deleting a platform sets `platform_id = NULL` on all linked problems — the problems themselves remain.

---

## App Settings

**Route:** `/admin/settings`
**APIs:** `GET /api/settings/admin`, `PUT /api/settings/admin/{key}`

Settings are grouped by their key prefix and displayed in sections:

### Branding
| Key | Type | Description |
|-----|------|-------------|
| `app.name` | STRING | Platform name shown in UI |
| `app.tagline` | STRING | Tagline on the login page |
| `app.logo_url` | STRING | Logo path or URL |

### Maintenance
| Key | Type | Description |
|-----|------|-------------|
| `app.maintenance_mode` | BOOLEAN | **Toggle switch** — blocks non-admin access with 503 |
| `app.maintenance_message` | STRING | Message shown during maintenance |

> ⚠️ Enabling maintenance mode immediately affects all non-admin users. Admins retain full access.

### Feature Toggles
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `feature.notes_enabled` | BOOLEAN | `true` | Enable/disable the notes feature |
| `feature.bookmarks_enabled` | BOOLEAN | `true` | Enable/disable bookmarks |
| `feature.leaderboard_enabled` | BOOLEAN | `false` | Coming soon feature |
| `feature.contest_mode` | BOOLEAN | `false` | Coming soon feature |

### Security
| Key | Type | Description |
|-----|------|-------------|
| `security.jwt_expiry_minutes` | INTEGER | JWT token lifetime in minutes |
| `security.max_login_attempts` | INTEGER | Lockout threshold |
| `security.password_min_length` | INTEGER | Minimum password length |
| `security.require_special_char` | BOOLEAN | Require special character in passwords |

> Note: JWT expiry and password policy changes take effect for new tokens/registrations only. Existing sessions are not affected.

### Pagination
| Key | Type | Default |
|-----|------|---------|
| `pagination.default_page_size` | INTEGER | 20 |
| `pagination.max_page_size` | INTEGER | 100 |

### Logs
| Key | Type | Description |
|-----|------|-------------|
| `logs.retention_days` | INTEGER | Auto-purge logs older than this many days |
| `logs.admin_actions_enabled` | BOOLEAN | Log all admin actions |

---

## Activity Logs

**Route:** `/admin/logs`
**API:** `GET /api/admin/logs`

### Filters

- **Action keyword** — partial match (e.g., "USER" matches `USER_LOGIN`, `USER_BLOCKED`, etc.)
- **Entity type** — exact match (`USER`, `PROBLEM`, `SETTING`, `PLATFORM`)
- **Date range** — ISO-8601 `from` and `to` parameters

### Log Purge

Clicking "Purge old logs" deletes all entries older than 90 days (the default retention period). This is irreversible. The response shows the count of deleted entries.

The retention period is configurable via the `logs.retention_days` app setting.

---

## Admin Role Management

### Promoting a User to Admin

1. Go to `/admin/users`
2. Find the user
3. Click "🛡 Make Admin"
4. Confirm in the modal

The user must log out and log back in for their new role to take effect in their existing JWT.

### Demoting an Admin to User

Same process — click "👤 Make User". The admin cannot demote themselves.

---

## Admin Security Notes

- Admin endpoints are protected at **both** URL level (SecurityConfig) and method level (@PreAuthorize)
- All admin actions are logged to the `activity_logs` table with the admin's user ID
- The default admin account (`admin@dsanext.com`) should have its password changed immediately after first login
- Admin accounts cannot be deleted or blocked via the admin panel — only via direct DB access
