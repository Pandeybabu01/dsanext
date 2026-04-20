# ⚡ DSANext

**Master DSA. Land the job.**

DSANext is a production-grade SaaS DSA Practice Platform where developers can solve coding problems, track their learning journey, manage notes and bookmarks, and view detailed analytics — all in one place.

---

## Features

- 🔐 **JWT Authentication** — secure login with role-based access (USER / ADMIN)
- 💻 **Problem Library** — filterable by difficulty (Easy 🟢 / Medium 🟡 / Hard 🔴), topic, and platform
- 📈 **Progress Tracking** — mark problems as In Progress / Solved / Revisit
- 📝 **Notes System** — write and save notes per problem
- 🔖 **Bookmarks** — save problems for later
- 📊 **Analytics Dashboard** — solved by difficulty, topic, platform, and daily activity
- 🔔 **Notifications** — in-app notification center
- 🌐 **Platform Integration** — link LeetCode, Codeforces, HackerRank, InterviewBit accounts
- 🛡 **Admin Control Center** — full user, problem, platform, and settings management
- 🌙 **Dark Mode** — system / light / dark theme with instant preview
- 📱 **Responsive** — works on mobile, tablet, and desktop

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, Vite, Tailwind CSS, Redux Toolkit |
| Backend | Spring Boot 3.2, Spring Security, Hibernate JPA |
| Database | PostgreSQL 15+ |
| Auth | JWT (HMAC-SHA256, BCrypt) |
| Charts | Recharts |

---

## Quick Start

### Prerequisites

- Java 17+, Maven 3.9+
- Node.js 20+ LTS, npm 10+
- PostgreSQL 15+

### 1. Clone

```bash
git clone https://github.com/your-org/dsanext.git
cd dsanext
```

### 2. Initialize database

```bash
bash scripts/init-db.sh
```

### 3. Start backend

```bash
bash scripts/start-backend.sh
# API: http://localhost:8080/api
```

### 4. Start frontend

```bash
bash scripts/start-frontend.sh
# App: http://localhost:5173
```


---

## Documentation

| Document | Description |
|----------|-------------|
| [Setup Guide](docs/setup-guide.md) | Complete local development setup |
| [API Reference](docs/api-reference.md) | All 42 REST endpoints |
| [Architecture](docs/architecture.md) | System design and data flow |
| [JWT Flow](docs/jwt-flow.md) | Authentication deep dive |
| [Admin Panel](docs/admin-panel.md) | Admin feature guide |
| [Deployment](docs/deployment.md) | Production deployment on Linux |

---

## Project Structure

```
dsanext/
├── backend/          Spring Boot REST API (Java 21)
│   └── src/main/java/com/dsanext/
│       ├── config/       App + CORS + Audit + JWT config
│       ├── security/     JWT filter, handlers, SecurityConfig
│       ├── domain/       JPA entities and enums
│       ├── repository/   Spring Data repositories
│       ├── service/      Business logic
│       ├── controller/   REST endpoints
│       ├── dto/          Request/response DTOs
│       └── exception/    Global exception handler
│
├── frontend/         React + Vite SPA
│   └── src/
│       ├── app/          Redux store
│       ├── components/   Shared UI (Button, Input, Modal, etc.)
│       ├── features/     Feature modules (auth, problems, admin, etc.)
│       ├── hooks/        Custom React hooks
│       ├── lib/          Axios, constants, formatters, validators
│       └── routes/       AppRouter with lazy loading
│
├── database/         PostgreSQL schema and migrations
│   ├── schema.sql        Full DDL
│   ├── seed.sql          Seed data (admin, platforms, problems)
│   └── migrations/       Flyway versioned migrations (V1-V3)
│
├── docs/             Documentation
└── scripts/          Setup and deployment scripts
```

---


---

## License

MIT — see [LICENSE](LICENSE)
