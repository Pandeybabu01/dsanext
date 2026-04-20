# DSANext API Reference

**Version:** 1.0.0
**Base URL:** `http://localhost:8080/api` (dev) · `https://api.dsanext.com/api` (prod)
**Format:** All requests and responses use `application/json`

---

## Authentication

DSANext uses **JWT Bearer tokens**. Include the token in every protected request:

```
Authorization: Bearer <access_token>
```

Tokens expire after **24 hours** (configurable via `security.jwt_expiry_minutes` setting).

### Standard Response Envelope

Every endpoint returns this shape:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "error": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

On validation failure, `error` contains a field map:

```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "email": "Must be a valid email address",
    "password": "Must contain at least one special character"
  }
}
```

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200  | OK — request succeeded |
| 201  | Created — resource created |
| 400  | Bad Request — validation failed |
| 401  | Unauthorized — missing or invalid JWT |
| 403  | Forbidden — authenticated but wrong role |
| 404  | Not Found — resource does not exist |
| 409  | Conflict — duplicate resource (email, username, slug) |
| 429  | Too Many Requests — rate limited |
| 500  | Internal Server Error |
| 503  | Service Unavailable — maintenance mode |

---

## Auth Endpoints

### POST /auth/register

Register a new user account. Returns JWT on success.

**Access:** Public

**Request:**
```json
{
  "username":  "johndoe",
  "email":     "john@example.com",
  "password":  "Secure@123",
  "fullName":  "John Doe"
}
```

**Validation rules:**
- `username`: 3–50 chars, alphanumeric + underscore only
- `email`: valid email format, max 255 chars
- `password`: 8–100 chars, must contain uppercase, lowercase, number, special char (`@$!%*?&`)
- `fullName`: 2–150 chars

**Response `201`:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType":   "Bearer",
    "expiresIn":   86400000,
    "user": {
      "id":        "550e8400-e29b-41d4-a716-446655440000",
      "username":  "johndoe",
      "email":     "john@example.com",
      "fullName":  "John Doe",
      "role":      "USER",
      "isActive":  true,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  }
}
```

---

### POST /auth/login

Authenticate with email and password. Returns JWT.

**Access:** Public

**Request:**
```json
{
  "email":    "john@example.com",
  "password": "Secure@123"
}
```

**Response `200`:** Same shape as `/auth/register` response.

**Error `401`:**
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

---

### GET /auth/me

Get the currently authenticated user's profile.

**Access:** JWT required

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id":              "550e8400-...",
    "username":        "johndoe",
    "email":           "john@example.com",
    "fullName":        "John Doe",
    "profileImageUrl": "/uploads/profiles/550e8400-uuid.jpg",
    "role":            "USER",
    "isActive":        true,
    "createdAt":       "2024-01-15T10:30:00Z"
  }
}
```

---

## User Endpoints

### GET /users/profile

Get the authenticated user's full profile.

**Access:** JWT required

**Response `200`:** `UserResponse` object (same as `/auth/me`)

---

### PUT /users/profile

Update profile information. All fields are optional — only provided fields are updated.

**Access:** JWT required

**Request:**
```json
{
  "fullName":        "John Smith",
  "username":        "johnsmith",
  "email":           "johnsmith@example.com",
  "profileImageUrl": "/uploads/profiles/uuid.jpg"
}
```

**Response `200`:** Updated `UserResponse`

**Error `409`:** Email or username already taken

---

### PUT /users/password

Change the authenticated user's password.

**Access:** JWT required

**Request:**
```json
{
  "currentPassword": "Secure@123",
  "newPassword":     "NewSecure@456",
  "confirmPassword": "NewSecure@456"
}
```

**Response `200`:**
```json
{ "success": true, "message": "Password updated successfully" }
```

---

### POST /users/profile/image

Upload a profile image. Accepts `multipart/form-data`.

**Access:** JWT required

**Request:** `Content-Type: multipart/form-data`
- `file`: image file (JPEG, PNG, or WebP, max 5MB)

**Response `200`:** Updated `UserResponse` with new `profileImageUrl`

---

### DELETE /users/account

Permanently delete the authenticated user's account and all associated data.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "message": "Account deleted successfully" }
```

---

## Problem Endpoints

### GET /problems

List problems with optional filters. Enriches with user context when JWT provided.

**Access:** Public (enhanced with JWT)

**Query Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `search` | string | Search in title and topic |
| `difficulty` | `EASY\|MEDIUM\|HARD` | Filter by difficulty |
| `topic` | string | Filter by topic (exact match, case-insensitive) |
| `platformId` | UUID | Filter by platform |
| `page` | int | Page index, 0-based (default: 0) |
| `size` | int | Page size (default: 20, max: 100) |
| `sortBy` | string | Field to sort by (default: `createdAt`) |
| `direction` | `asc\|desc` | Sort direction (default: `desc`) |

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id":                 "uuid",
        "title":              "Two Sum",
        "slug":               "two-sum",
        "description":        "Given an array of integers...",
        "topic":              "Array",
        "difficulty":         "EASY",
        "externalUrl":        "https://leetcode.com/problems/two-sum/",
        "platform": {
          "id":      "uuid",
          "name":    "LeetCode",
          "baseUrl": "https://leetcode.com/problems/",
          "iconUrl": "https://..."
        },
        "isActive":           true,
        "createdAt":          "2024-01-15T10:30:00Z",
        "updatedAt":          "2024-01-15T10:30:00Z",
        "userProgressStatus": "SOLVED",
        "isBookmarked":       true,
        "hasNote":            false
      }
    ],
    "page":          0,
    "size":          20,
    "totalElements": 150,
    "totalPages":    8,
    "first":         true,
    "last":          false,
    "empty":         false
  }
}
```

> `userProgressStatus`, `isBookmarked`, `hasNote` are only populated when a valid JWT is sent.

---

### GET /problems/topics

Get all distinct problem topics for filter dropdowns.

**Access:** Public

**Response `200`:**
```json
{
  "success": true,
  "data": ["Array", "Binary Search", "Dynamic Programming", "Graph", "Linked List"]
}
```

---

### GET /problems/{slug}

Get a single problem by its URL slug. Enriches with user context when JWT provided.

**Access:** Public (enhanced with JWT)

**Path Params:** `slug` — URL-safe problem identifier (e.g. `two-sum`)

**Response `200`:** Single `ProblemResponse` object

**Error `404`:**
```json
{ "success": false, "message": "Problem not found with slug: 'invalid-slug'" }
```

---

## Progress Endpoints

### GET /progress

List the authenticated user's progress entries.

**Access:** JWT required

**Query Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `status` | `NOT_STARTED\|IN_PROGRESS\|SOLVED\|REVISIT` | Filter by status |
| `page` | int | Page index (default: 0) |
| `size` | int | Page size (default: 20) |

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id":               "uuid",
        "problemId":        "uuid",
        "problemTitle":     "Two Sum",
        "problemSlug":      "two-sum",
        "problemTopic":     "Array",
        "problemDifficulty":"EASY",
        "status":           "SOLVED",
        "attemptCount":     3,
        "firstAttemptedAt": "2024-01-10T08:00:00Z",
        "solvedAt":         "2024-01-12T14:30:00Z",
        "updatedAt":        "2024-01-12T14:30:00Z"
      }
    ],
    "page": 0, "size": 20, "totalElements": 45, "totalPages": 3
  }
}
```

---

### GET /progress/{problemId}

Get progress for a specific problem.

**Access:** JWT required

**Response `200`:** Single `ProgressResponse`

**Error `404`:** Progress entry not found for this user + problem combination

---

### PUT /progress/{problemId}

Create or update progress for a problem. Upsert semantics — safe to call repeatedly.

**Access:** JWT required

**Path Params:** `problemId` — UUID of the problem

**Request:**
```json
{
  "status": "SOLVED"
}
```

Valid status values: `NOT_STARTED` · `IN_PROGRESS` · `SOLVED` · `REVISIT`

**Behaviour:**
- First call with `IN_PROGRESS` or `SOLVED` → increments `attemptCount` and sets `firstAttemptedAt`
- Call with `SOLVED` → sets `solvedAt` timestamp (idempotent)
- Subsequent calls → updates status only (attempt count not re-incremented from SOLVED)

**Response `200`:** Updated `ProgressResponse`

---

## Notes Endpoints

### GET /notes

List the authenticated user's notes with optional search.

**Access:** JWT required

**Query Parameters:** `search` (string), `page` (int), `size` (int)

**Response `200`:** Paginated list of `NoteResponse`

```json
{
  "content": [
    {
      "id":               "uuid",
      "problemId":        "uuid",
      "problemTitle":     "Two Sum",
      "problemSlug":      "two-sum",
      "problemDifficulty":"EASY",
      "content":          "Use a hash map to store complement...",
      "createdAt":        "2024-01-10T08:00:00Z",
      "updatedAt":        "2024-01-12T14:30:00Z"
    }
  ]
}
```

---

### GET /notes/problem/{problemId}

Get the note for a specific problem.

**Access:** JWT required

**Response `200`:** Single `NoteResponse`

**Error `404`:** No note exists for this user + problem combination

---

### PUT /notes/problem/{problemId}

Create or update a note for a problem. Upsert semantics.

**Access:** JWT required

**Request:**
```json
{
  "content": "Approach: Use hash map for O(n) solution.\nTime: O(n), Space: O(n)"
}
```

**Validation:** `content` required, max 50,000 characters

**Response `200`:** Saved `NoteResponse`

---

### DELETE /notes/problem/{problemId}

Delete the note for a specific problem.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "message": "Note deleted" }
```

---

## Bookmark Endpoints

### GET /bookmarks

List the authenticated user's bookmarks with optional filters.

**Access:** JWT required

**Query Parameters:** `search`, `topic`, `difficulty`, `page`, `size`

**Response `200`:**
```json
{
  "content": [
    {
      "id":        "uuid",
      "problem":   { "id":"uuid", "title":"Two Sum", "slug":"two-sum", "difficulty":"EASY", "topic":"Array" },
      "createdAt": "2024-01-10T08:00:00Z"
    }
  ]
}
```

---

### POST /bookmarks/{problemId}

Add a bookmark for a problem.

**Access:** JWT required

**Response `201`:** `BookmarkResponse`

**Error `409`:** Already bookmarked

---

### DELETE /bookmarks/{problemId}

Remove a bookmark.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "message": "Bookmark removed" }
```

---

### POST /bookmarks/{problemId}/toggle

Toggle bookmark — adds if absent, removes if present.

**Access:** JWT required

**Response `200`:**
```json
{
  "success": true,
  "message": "Problem bookmarked",
  "data":    { "bookmarked": true }
}
```

---

### GET /bookmarks/{problemId}/status

Check if a problem is bookmarked by the current user.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "data": { "bookmarked": false } }
```

---

## Notification Endpoints

### GET /notifications

List the authenticated user's notifications.

**Access:** JWT required

**Query Parameters:** `page` (int), `size` (int)

**Response `200`:** Paginated list of `NotificationResponse`

```json
{
  "content": [
    {
      "id":        "uuid",
      "title":     "Welcome to DSANext Admin Panel",
      "message":   "Your admin account is set up. Start by adding problems.",
      "type":      "SUCCESS",
      "isRead":    false,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

Notification types: `INFO` · `SUCCESS` · `WARNING` · `SYSTEM`

---

### GET /notifications/unread-count

Get the count of unread notifications.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "data": { "unreadCount": 3 } }
```

---

### PATCH /notifications/{id}/read

Mark a single notification as read.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "message": "Notification marked as read" }
```

---

### PATCH /notifications/read-all

Mark all of the user's notifications as read.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "data": { "updatedCount": 5 } }
```

---

### DELETE /notifications/read

Delete all read notifications for the user.

**Access:** JWT required

**Response `200`:**
```json
{ "success": true, "data": { "deletedCount": 12 } }
```

---

## Analytics Endpoints

### GET /analytics/me

Get the authenticated user's personal analytics dashboard data.

**Access:** JWT required

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "totalSolved":      42,
    "totalInProgress":  8,
    "totalRevisit":     3,
    "totalBookmarks":   15,
    "totalNotes":       20,
    "solvedByDifficulty": { "EASY": 20, "MEDIUM": 15, "HARD": 7 },
    "solvedByTopic":    { "Array": 12, "Dynamic Programming": 8, "Graph": 5 },
    "recentlySolved": [
      {
        "problemId":        "uuid",
        "problemTitle":     "Two Sum",
        "problemDifficulty":"EASY",
        "solvedAt":         "2024-01-14T18:00:00Z"
      }
    ],
    "dailyStats": [
      { "date": "2024-01-14", "count": 3 },
      { "date": "2024-01-15", "count": 5 }
    ]
  }
}
```

---

### GET /analytics/admin

Get platform-wide analytics. **Admin only.**

**Access:** ADMIN role required

**Response `200`:**
```json
{
  "data": {
    "totalUsers":          1250,
    "totalActiveUsers":    1180,
    "totalProblems":       350,
    "totalActiveProblems": 340,
    "totalProgressEntries":18400,
    "problemsByDifficulty": { "EASY": 120, "MEDIUM": 160, "HARD": 70 },
    "usersByRole":          { "USER": 1248, "ADMIN": 2 },
    "dailyStats": [ { "date": "2024-01-15", "count": 280 } ]
  }
}
```

---

## Platform Endpoints

### GET /platforms

List all active platforms. Used for filter dropdowns.

**Access:** Public

**Response `200`:**
```json
{
  "data": [
    {
      "id":      "uuid",
      "name":    "LeetCode",
      "baseUrl": "https://leetcode.com/problems/",
      "iconUrl": "https://...",
      "isActive": true
    }
  ]
}
```

---

### GET /platforms/{id}

Get a single platform by ID.

**Access:** Public

---

### GET /platforms/admin

Search platforms with pagination. **Admin only.**

**Access:** ADMIN role

**Query Parameters:** `search`, `active` (boolean), `page`, `size`

---

### POST /platforms

Create a new platform. **Admin only.**

**Access:** ADMIN role

**Request:**
```json
{
  "name":    "LeetCode",
  "baseUrl": "https://leetcode.com/problems/",
  "iconUrl": "https://assets.leetcode.com/favicon.ico",
  "isActive": true
}
```

**Response `201`:** `PlatformResponse`

---

### PUT /platforms/{id}

Update a platform. **Admin only.**

**Access:** ADMIN role

**Request:** Same shape as POST

**Response `200`:** Updated `PlatformResponse`

---

### DELETE /platforms/{id}

Delete a platform. Problems linked to it will have `platform` set to `null`.

**Access:** ADMIN role

**Response `200`:**
```json
{ "success": true, "message": "Platform deleted" }
```

---

## Settings Endpoints

### GET /settings/public

Get all public app settings as a key-value map. No authentication required. Used for frontend bootstrap.

**Access:** Public

**Response `200`:**
```json
{
  "data": {
    "app.name":                    "DSANext",
    "app.tagline":                 "Master DSA. Land the job.",
    "app.maintenance_mode":        "false",
    "feature.notes_enabled":       "true",
    "feature.bookmarks_enabled":   "true",
    "feature.leaderboard_enabled": "false",
    "security.password_min_length":"8"
  }
}
```

---

### GET /settings/me

Get the authenticated user's settings.

**Access:** JWT required

**Response `200`:**
```json
{
  "data": {
    "id":                   "uuid",
    "theme":                "dark",
    "notificationsEnabled": true,
    "emailNotifications":   true,
    "lcUsername":           "johndoe",
    "cfUsername":           "",
    "hrUsername":           "",
    "ibUsername":           "",
    "updatedAt":            "2024-01-15T10:30:00Z"
  }
}
```

---

### PUT /settings/me

Update the authenticated user's settings. All fields optional.

**Access:** JWT required

**Request:**
```json
{
  "theme":                "dark",
  "notificationsEnabled": true,
  "emailNotifications":   false,
  "lcUsername":           "johndoe",
  "cfUsername":           "john_cf",
  "hrUsername":           "",
  "ibUsername":           ""
}
```

**Response `200`:** Updated `UserSettingResponse`

---

### GET /settings/admin

List all app settings. **Admin only.**

**Access:** ADMIN role

**Response `200`:** Array of `AppSettingResponse`

```json
{
  "data": [
    {
      "id":           "uuid",
      "settingKey":   "app.maintenance_mode",
      "settingValue": "false",
      "dataType":     "BOOLEAN",
      "description":  "Enable maintenance mode site-wide",
      "isPublic":     true,
      "updatedAt":    "2024-01-15T10:30:00Z"
    }
  ]
}
```

---

### GET /settings/admin/{key}

Get a single app setting by key. **Admin only.**

**Access:** ADMIN role

---

### PUT /settings/admin/{key}

Update a single app setting value. **Admin only.**

**Access:** ADMIN role

**Request:**
```json
{ "value": "true" }
```

**Response `200`:** Updated `AppSettingResponse`

**Common setting keys:**

| Key | Type | Description |
|-----|------|-------------|
| `app.name` | STRING | Application display name |
| `app.maintenance_mode` | BOOLEAN | Enable/disable maintenance mode |
| `feature.notes_enabled` | BOOLEAN | Toggle notes feature |
| `feature.bookmarks_enabled` | BOOLEAN | Toggle bookmarks feature |
| `feature.leaderboard_enabled` | BOOLEAN | Toggle leaderboard |
| `feature.contest_mode` | BOOLEAN | Toggle contest mode |
| `security.jwt_expiry_minutes` | INTEGER | JWT token lifetime |
| `security.max_login_attempts` | INTEGER | Lockout threshold |
| `security.password_min_length` | INTEGER | Password policy |
| `pagination.default_page_size` | INTEGER | Default API page size |
| `logs.retention_days` | INTEGER | Log retention period |

---

## Admin Endpoints

All admin endpoints require `ADMIN` role. Enforced at both URL level and `@PreAuthorize`.

### GET /admin/users

Search users with pagination.

**Access:** ADMIN

**Query Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `search` | string | Search name, email, username |
| `role` | `USER\|ADMIN` | Filter by role |
| `active` | boolean | Filter by active status |
| `page` | int | Page index |
| `size` | int | Page size |
| `sortBy` | string | Sort field |
| `direction` | `asc\|desc` | Sort direction |

**Response `200`:** Paginated `UserResponse` list

---

### GET /admin/users/{id}

Get a user by ID.

**Access:** ADMIN

**Response `200`:** `UserResponse`

---

### PATCH /admin/users/{id}/block

Block a user (set `isActive = false`). Cannot block admin users.

**Access:** ADMIN

**Response `200`:** Updated `UserResponse`

---

### PATCH /admin/users/{id}/unblock

Unblock a user.

**Access:** ADMIN

**Response `200`:** Updated `UserResponse`

---

### PATCH /admin/users/{id}/role

Change a user's role.

**Access:** ADMIN

**Query Parameters:** `role` = `USER` or `ADMIN`

**Response `200`:** Updated `UserResponse`

**Error `400`:** Admin cannot change their own role

---

### DELETE /admin/users/{id}

Permanently delete a user. Cannot delete admin accounts.

**Access:** ADMIN

**Response `200`:**
```json
{ "success": true, "message": "User deleted" }
```

---

### GET /admin/problems

List all problems including inactive ones.

**Access:** ADMIN

**Query Parameters:** `search`, `difficulty`, `topic`, `active` (boolean), `page`, `size`, `sortBy`, `direction`

**Response `200`:** Paginated `ProblemResponse` with `isActive` field included

---

### GET /admin/problems/{id}

Get a problem by UUID.

**Access:** ADMIN

---

### POST /admin/problems

Create a new problem.

**Access:** ADMIN

**Request:**
```json
{
  "title":       "Two Sum",
  "description": "Given an array of integers nums and an integer target...",
  "topic":       "Array",
  "difficulty":  "EASY",
  "externalUrl": "https://leetcode.com/problems/two-sum/",
  "platformId":  "uuid-of-leetcode-platform"
}
```

**Validation:**
- `title`: required, 3–300 chars
- `topic`: required, 2–100 chars
- `difficulty`: required — `EASY` / `MEDIUM` / `HARD`
- `externalUrl`: optional, must start with `http://` or `https://`
- `platformId`: optional UUID

**Response `201`:** Created `ProblemResponse` with auto-generated `slug`

---

### PUT /admin/problems/{id}

Update a problem. All fields optional (partial update).

**Access:** ADMIN

**Request:** Same fields as POST — any subset

**Response `200`:** Updated `ProblemResponse`

---

### DELETE /admin/problems/{id}

Delete a problem and all associated progress, notes, and bookmarks (cascade).

**Access:** ADMIN

**Response `200`:**
```json
{ "success": true, "message": "Problem deleted" }
```

---

### GET /admin/logs

Search activity logs.

**Access:** ADMIN

**Query Parameters:**

| Param | Type | Description |
|-------|------|-------------|
| `userId` | UUID | Filter by user |
| `action` | string | Filter by action keyword |
| `entityType` | string | Filter by entity type |
| `from` | ISO-8601 datetime | Start of date range |
| `to` | ISO-8601 datetime | End of date range |
| `page` | int | Page index |
| `size` | int | Page size (default: 50) |

**Response `200`:** Paginated `ActivityLogResponse`

```json
{
  "content": [
    {
      "id":         "uuid",
      "userId":     "uuid",
      "userEmail":  "john@example.com",
      "action":     "PROBLEM_SOLVED",
      "entityType": "PROBLEM",
      "entityId":   "uuid",
      "metadata":   { "problemTitle": "Two Sum", "difficulty": "EASY" },
      "ipAddress":  "192.168.1.1",
      "createdAt":  "2024-01-15T10:30:00Z"
    }
  ]
}
```

**Common action codes:**

| Action | Trigger |
|--------|---------|
| `USER_REGISTERED` | New user signs up |
| `USER_LOGIN` | Successful login |
| `PROFILE_UPDATED` | User updates profile |
| `PASSWORD_CHANGED` | Password changed |
| `ACCOUNT_DELETED` | User deletes account |
| `PROGRESS_UPDATED` | Progress status changed |
| `NOTE_SAVED` | Note created or updated |
| `NOTE_DELETED` | Note deleted |
| `BOOKMARK_ADDED` | Problem bookmarked |
| `BOOKMARK_REMOVED` | Bookmark removed |
| `PROBLEM_CREATED` | Admin creates problem |
| `PROBLEM_UPDATED` | Admin updates problem |
| `PROBLEM_DELETED` | Admin deletes problem |
| `PLATFORM_CREATED` | Admin creates platform |
| `USER_BLOCKED` | Admin blocks user |
| `USER_UNBLOCKED` | Admin unblocks user |
| `USER_ROLE_CHANGED` | Admin changes role |
| `USER_DELETED` | Admin deletes user |
| `APP_SETTING_UPDATED` | Admin changes app setting |

---

### DELETE /admin/logs/purge

Purge activity logs older than the specified retention period.

**Access:** ADMIN

**Query Parameters:** `retentionDays` (int, default: 90)

**Response `200`:**
```json
{ "success": true, "data": { "deletedCount": 4520 } }
```

---

## JWT Authentication Flow

```
Client                          Server
  |                                |
  |-- POST /auth/login ----------->|
  |   { email, password }         |
  |                                |-- Validate credentials (BCrypt)
  |                                |-- Generate JWT (HS256, 24h TTL)
  |<-- 200 { accessToken, user } --|
  |                                |
  |-- GET /problems -------------->|
  |   Authorization: Bearer <jwt> |-- Validate signature + expiry
  |                                |-- Load user from DB
  |                                |-- Set SecurityContext
  |<-- 200 { problems }  ---------|
  |                                |
  |-- GET /admin/users ----------->|
  |   Authorization: Bearer <jwt> |-- Check ROLE_ADMIN
  |<-- 403 (if USER role) --------|
```

---

## Pagination

All list endpoints follow this pattern:

**Request:**
```
GET /problems?page=0&size=20&sortBy=createdAt&direction=desc
```

**Response:**
```json
{
  "content":       [...],
  "page":          0,
  "size":          20,
  "totalElements": 150,
  "totalPages":    8,
  "first":         true,
  "last":          false,
  "empty":         false
}
```

**Defaults:** `page=0`, `size=20`, `sortBy=createdAt`, `direction=desc`
**Maximum size:** 100 items per page (enforced server-side)

---

## Error Handling

### 400 — Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "title":      "Title is required",
    "difficulty": "Difficulty is required"
  }
}
```

### 401 — Unauthorized
```json
{
  "success": false,
  "message": "Authentication required. Please login to access this resource.",
  "error":   "Unauthorized",
  "path":    "/api/progress"
}
```

### 403 — Forbidden
```json
{
  "success": false,
  "message": "You do not have permission to perform this action.",
  "error":   "Forbidden",
  "path":    "/api/admin/users"
}
```

### 404 — Not Found
```json
{
  "success": false,
  "message": "Problem not found with slug: 'nonexistent-slug'"
}
```

### 409 — Conflict
```json
{
  "success": false,
  "message": "User already exists with email: 'john@example.com'"
}
```

### 503 — Maintenance Mode
```json
{
  "success":     false,
  "message":     "We are upgrading DSANext. Back shortly!",
  "error":       "Service Unavailable",
  "maintenance": true
}
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `dsanext_db` | Database name |
| `DB_USERNAME` | `dsanext_user` | Database username |
| `DB_PASSWORD` | `dsanext_pass` | Database password |
| `JWT_SECRET` | (dev default) | JWT signing secret — **change in prod** |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiry (24h in ms) |
| `JWT_REFRESH_MS` | `2592000000` | Refresh token expiry (30d) |
| `SERVER_PORT` | `8080` | API server port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated CORS origins |
| `UPLOAD_DIR` | `./uploads/profiles` | Profile image storage path |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Frontend API base URL |
