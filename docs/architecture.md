# DSANext — Architecture Documentation

## Overview

DSANext is a full-stack SaaS DSA Practice Platform built on a clean three-tier architecture:

```
┌─────────────────────────────────────┐
│         React Frontend (Vite)        │
│  Redux Toolkit · React Router · Axios │
└────────────────┬────────────────────┘
                 │ HTTP/JSON (JWT)
┌────────────────▼────────────────────┐
│       Spring Boot REST API           │
│  Spring Security · JPA · JWT        │
└────────────────┬────────────────────┘
                 │ JDBC (HikariCP)
┌────────────────▼────────────────────┐
│           PostgreSQL 15+             │
│  10 tables · Flyway migrations       │
└─────────────────────────────────────┘
```

---

## Backend Architecture

### Package Structure

```
com.dsanext/
├── DSANextApplication.java        ← @SpringBootApplication entry point
├── config/
│   ├── AppConfig.java             ← ObjectMapper, async executor
│   ├── AuditConfig.java           ← JPA AuditingEntityListener
│   ├── CorsConfig.java            ← CORS from application.yml
│   ├── JwtProperties.java         ← @ConfigurationProperties record
│   └── WebMvcConfig.java          ← Static resource handlers
├── security/
│   ├── SecurityConfig.java        ← Filter chain, BCrypt, route rules
│   ├── JwtAuthFilter.java         ← OncePerRequestFilter — JWT extraction
│   ├── JwtAuthEntryPoint.java     ← 401 JSON handler
│   ├── JwtAccessDeniedHandler.java← 403 JSON handler
│   └── MaintenanceModeFilter.java ← 503 when maintenance_mode=true
├── domain/
│   ├── entity/                    ← JPA entities (all extend BaseEntity)
│   └── enums/                     ← Role, Difficulty, ProgressStatus, etc.
├── repository/                    ← Spring Data JPA repositories
├── dto/
│   ├── common/                    ← ApiResponse<T>, PageResponse<T>
│   ├── request/                   ← Input DTOs with @Valid annotations
│   └── response/                  ← Output DTOs with static from() builders
├── service/                       ← Business logic layer
├── controller/                    ← REST endpoints (@RestController)
├── exception/                     ← GlobalExceptionHandler + custom exceptions
└── util/                          ← SlugUtils, PaginationUtils
```

### Request Lifecycle

```
HTTP Request
    │
    ▼
CorsFilter                    ← Validates Origin header
    │
    ▼
MaintenanceModeFilter         ← Returns 503 if maintenance mode
    │
    ▼
JwtAuthFilter                 ← Extracts Bearer token, loads User
    │                            Sets SecurityContextHolder
    ▼
SecurityFilterChain           ← Route-based access rules
    │
    ▼
Controller                    ← @AuthenticationPrincipal User injected
    │
    ▼
Service                       ← Business logic, DB calls, activity logs
    │
    ▼
Repository                    ← Spring Data JPA → HikariCP → PostgreSQL
    │
    ▼
ActivityLogService (async)    ← @Async — never blocks response
    │
    ▼
HTTP Response (ApiResponse<T>)
```

### Layer Responsibilities

| Layer | Responsibility |
|-------|---------------|
| Controller | HTTP mapping, request parsing, response wrapping, delegation to Service |
| Service | Business logic, validation, transaction management, audit logging |
| Repository | Data access, JPQL/native queries, Spring Data derived queries |
| Entity | Domain model, JPA mappings, domain methods (recordAttempt, markSolved) |
| DTO | Data transfer only — no business logic, static `from()` factory methods |

---

## Frontend Architecture

### Feature-Based Structure

Each feature is a self-contained module:

```
features/
└── problems/
    ├── problemSlice.js        ← Redux state + async thunks
    ├── pages/
    │   ├── ProblemListPage.jsx    ← Route component
    │   └── ProblemDetailPage.jsx  ← Route component
    └── components/
        ├── ProblemCard.jsx        ← Card layout
        └── ProblemFilter.jsx      ← Filter bar
```

### State Management (Redux Toolkit)

```
store
├── auth          ← User session, JWT token, theme (persisted to localStorage)
├── problems      ← Problem list, current problem, topics, filters
├── progress      ← User progress entries
├── notes         ← User notes
├── bookmarks     ← User bookmarks
└── notifications ← Notification list, unread count
```

**Persistence strategy:**
- `auth.token` and `auth.user` → `localStorage` (hydrated on app init)
- All other slices → in-memory only (fetched fresh on page load)

### Data Flow

```
User Action
    │
    ▼
Component (dispatch thunk or direct API call)
    │
    ├─ Redux Thunk ─────────▶ api.js (Axios)
    │                              │
    │                              ▼
    │                         Spring Boot API
    │                              │
    │                              ▼
    │                        Redux state update
    │                              │
    ◀──────────────────────────────┘
    │
    ▼
Component re-renders with new state
```

### Routing Structure

```
/                   → redirect to /dashboard
/login              → LoginPage (public)
/register           → RegisterPage (public)

/dashboard          → DashboardPage       (JWT required)
/problems           → ProblemListPage     (JWT required)
/problems/:slug     → ProblemDetailPage   (JWT required)
/progress           → ProgressPage        (JWT required)
/notes              → NotesPage           (JWT required)
/bookmarks          → BookmarksPage       (JWT required)
/analytics          → AnalyticsPage       (JWT required)
/notifications      → NotificationsPage   (JWT required)
/settings           → UserSettingsPage    (JWT required)

/admin              → AdminDashboardPage  (ADMIN role)
/admin/users        → AdminUsersPage      (ADMIN role)
/admin/problems     → AdminProblemsPage   (ADMIN role)
/admin/platforms    → AdminPlatformsPage  (ADMIN role)
/admin/settings     → AdminSettingsPage   (ADMIN role)
/admin/logs         → AdminLogsPage       (ADMIN role)
```

---

## Security Architecture

### JWT Flow

```
1. Login: POST /auth/login
   Client sends email + password
   Server: DaoAuthenticationProvider → BCrypt verify → generate JWT
   JWT payload: { sub: email, userId, role, username, iat, exp }

2. Authenticated Request:
   Client: Authorization: Bearer <token>
   JwtAuthFilter:
     a. Extract token from header
     b. Validate signature (HMAC-SHA256)
     c. Check expiry
     d. Load UserDetails from DB
     e. Set UsernamePasswordAuthenticationToken in SecurityContext

3. Authorization:
   SecurityFilterChain → @PreAuthorize → method-level checks
   Role check: ROLE_USER vs ROLE_ADMIN (Spring adds "ROLE_" prefix)
```

### Password Security

- BCrypt with cost factor **12** (~250ms per hash on modern hardware)
- Minimum 8 chars, must contain uppercase + lowercase + number + special char
- Passwords stored as BCrypt hashes only — never logged or returned

---

## Database Architecture

### Table Relationships

```
users ──────────────── user_settings (1:1)
  │
  ├──── progress (many) ──── problems ──── platforms
  │
  ├──── notes (many) ─────── problems
  │
  ├──── bookmarks (many) ─── problems
  │
  ├──── notifications (many)
  │
  └──── activity_logs (many)

app_settings (standalone key-value store)
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| UUID PKs | No sequential ID leakage, safe for distributed systems |
| `TIMESTAMPTZ` everywhere | Timezone-aware, stored as UTC |
| Unique constraints on (user_id, problem_id) | One progress/note/bookmark per user per problem |
| `ON DELETE CASCADE` for user data | Deleting a user removes all their data automatically |
| `ON DELETE SET NULL` for audit data | Logs and created_by survive user deletion |
| JSONB for activity log metadata | Flexible payload without schema changes |
| `is_active` soft delete on users | Blocked users can be unblocked; data is preserved |
| Flyway for migrations | Version-controlled schema evolution |
| Auto-`updated_at` triggers | Consistent timestamps without application code |

---

## Async Processing

The following operations run asynchronously (fire-and-forget):

| Operation | Service | Executor |
|-----------|---------|----------|
| Activity log writes | `ActivityLogService.log()` | `taskExecutor` (4-20 threads) |
| Notification sends | `NotificationService.sendNotification()` | `taskExecutor` |

Async failures are caught and logged — they never propagate to the HTTP response.

---

## Scalability Considerations

### Current Architecture (Single Instance)

Suitable for: up to ~10,000 active users, ~100 req/s

- HikariCP connection pool: 10 (dev) / 30 (prod)
- Async executor: 4 core, 20 max, 500 queue

### Horizontal Scaling Path

1. **JWT is stateless** — no session affinity needed. Multiple instances can run behind a load balancer immediately.
2. **Async tasks** → Move to a message queue (RabbitMQ/Kafka) when volume requires dedicated workers.
3. **File uploads** → Move to S3/GCS when running multiple instances (filesystem is per-instance).
4. **Caching** → Add Redis for: public settings (rarely changes), problem list pages, analytics aggregates.
5. **Read replicas** → Route `@Transactional(readOnly=true)` queries to read replica via Spring's `AbstractRoutingDataSource`.
6. **Search** → Move problem full-text search to Elasticsearch when LIKE queries become slow.
