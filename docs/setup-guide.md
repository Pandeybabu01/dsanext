# DSANext — Local Development Setup Guide

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java (JDK) | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20+ LTS | `node -version` |
| npm | 10+ | `npm -version` |
| PostgreSQL | 15+ | `psql --version` |
| Git | any | `git --version` |

---

## Step 1 — Clone the Repository

```bash
git clone https://github.com/your-org/dsanext.git
cd dsanext
```

Project structure:
```
dsanext/
├── backend/          ← Spring Boot API
├── frontend/         ← React + Vite
├── database/         ← SQL schema and seed files
└── docs/             ← Documentation
```

---

## Step 2 — PostgreSQL Setup

### Install PostgreSQL

**macOS (Homebrew):**
```bash
brew install postgresql@15
brew services start postgresql@15
echo 'export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Ubuntu / Debian:**
```bash
sudo apt update
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

**Windows:**
Download the installer from https://www.postgresql.org/download/windows/ and run it. During installation:
- Port: `5432` (default)
- Password for `postgres` superuser: set a strong password
- Add PostgreSQL `bin` directory to PATH

---

### Create Database and User

Connect as the postgres superuser:

```bash
# macOS / Linux
psql -U postgres

# Windows (in PostgreSQL SQL Shell / psql)
# Open "SQL Shell (psql)" from Start Menu
```

Run these SQL commands:

```sql
-- Create dedicated user for DSANext
CREATE USER dsanext_user WITH PASSWORD 'dsanext_pass';

-- Create the database
CREATE DATABASE dsanext_db OWNER dsanext_user;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE dsanext_db TO dsanext_user;

-- Connect to the new database
\c dsanext_db

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO dsanext_user;

-- Exit
\q
```

### Verify Connection

```bash
psql -h localhost -U dsanext_user -d dsanext_db -c "SELECT version();"
# Enter password: dsanext_pass
# Should print: PostgreSQL 15.x ...
```

---

### Initialize Database Schema

**Option A — Manual (run SQL files directly):**

```bash
# Apply schema
psql -h localhost -U dsanext_user -d dsanext_db -f database/schema.sql

# Apply seed data (platforms, admin user, app settings, sample problems)
psql -h localhost -U dsanext_user -d dsanext_db -f database/seed.sql
```

**Option B — Flyway (recommended, auto-runs on Spring Boot startup):**

Flyway is configured in `application.yml` and runs automatically when the backend starts. It picks up migrations from `backend/src/main/resources/db/migration/`.

If you want to run Flyway manually before starting the app:
```bash
cd backend
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/dsanext_db \
                   -Dflyway.user=dsanext_user \
                   -Dflyway.password=dsanext_pass
```

---

## Step 3 — Backend Setup

### Configure Environment

Create or edit `backend/src/main/resources/application-dev.yml` (this file is in `.gitignore` for local overrides):

```yaml
spring:
  datasource:
    url:      jdbc:postgresql://localhost:5432/dsanext_db
    username: dsanext_user
    password: dsanext_pass

dsanext:
  jwt:
    secret: dsanext-dev-secret-key-at-least-32-characters-long-for-hs256
    expiration-ms: 86400000
```

Or use environment variables (preferred for production):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=dsanext_db
export DB_USERNAME=dsanext_user
export DB_PASSWORD=dsanext_pass
export JWT_SECRET=your-dev-secret-key-at-least-32-chars
export SPRING_PROFILES_ACTIVE=dev
```

### Build and Run

```bash
cd backend

# Install dependencies and compile
mvn clean compile

# Run tests
mvn test

# Start the server (dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or build fat JAR and run it
mvn clean package -DskipTests
java -jar target/dsanext-backend-1.0.0.jar --spring.profiles.active=dev
```

**Expected output:**
```
  ____  ____    _    _   _           _
 |  _ \/ ___|  / \  | \ | | _____  _| |_
 | | | \___ \ / _ \ |  \| |/ _ \ \/ / __|
 | |_| |___) / ___ \| |\  |  __/>  <| |_
 |____/|____/_/   \_\_| \_|\___/_/\_\\__|

DSANext Backend v1.0.0
Started DSANextApplication in 4.2 seconds
Tomcat started on port 8080
Flyway: Successfully applied 3 migrations
```

### Verify Backend

```bash
# Health check
curl http://localhost:8080/api/actuator/health
# {"status":"UP"}

# Public settings
curl http://localhost:8080/api/settings/public
# {"success":true,"data":{"app.name":"DSANext",...}}

# Login with admin (seeded by seed.sql)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@dsanext.com","password":"Admin@123"}'
```

---

## Step 4 — Frontend Setup

### Install Dependencies

```bash
cd frontend
npm install
```

### Configure Environment

The default `.env` file is already configured for local development:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=DSANext
VITE_APP_VERSION=1.0.0
```

No changes needed for local dev — Vite's dev proxy forwards `/api` calls to `http://localhost:8080`.

### Start Development Server

```bash
cd frontend
npm run dev
```

**Expected output:**
```
  VITE v5.x  ready in 312 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

Open `http://localhost:5173` in your browser.

---

## Step 5 — First Login

### Admin Account (seeded)

| Field | Value |
|-------|-------|
| Email | `admin@dsanext.com` |
| Password | `Admin@123` |
| Role | ADMIN |

**⚠️ Change the admin password immediately after first login.**

### Create a Regular User

Visit `http://localhost:5173/register` and create a new account, or use the API:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":  "testuser",
    "email":     "test@example.com",
    "password":  "Test@1234",
    "fullName":  "Test User"
  }'
```

---

## Step 6 — Verify Full Stack

Run this checklist to confirm everything works:

```bash
# 1. Backend health
curl http://localhost:8080/api/actuator/health

# 2. Database: list problems (seeded)
curl http://localhost:8080/api/problems | python3 -m json.tool

# 3. Login and capture token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@dsanext.com","password":"Admin@123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
echo "Token: $TOKEN"

# 4. Authenticated request
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/auth/me | python3 -m json.tool

# 5. Admin analytics
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/analytics/admin | python3 -m json.tool
```

---

## Development Scripts

### Backend

```bash
cd backend

# Run with live reload (requires spring-boot-devtools)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests only
mvn test

# Run tests with coverage report
mvn verify

# Check code style
mvn checkstyle:check

# Build production JAR
mvn clean package -DskipTests -Pproduction
```

### Frontend

```bash
cd frontend

# Start dev server (hot reload)
npm run dev

# Type check + lint
npm run lint

# Fix lint issues automatically
npm run lint:fix

# Format code with Prettier
npm run format

# Build for production
npm run build

# Preview production build locally
npm run preview
```

---

## Directory Reference

```
backend/src/main/resources/
├── application.yml          ← Base config (shared)
├── application-dev.yml      ← Dev overrides (local only, gitignored)
├── application-prod.yml     ← Prod overrides (env vars only)
└── db/migration/
    ├── V1__init_schema.sql  ← Full schema DDL
    ├── V2__seed_platforms.sql
    └── V3__app_settings.sql

frontend/
├── .env                     ← Dev environment (committed)
├── .env.production          ← Prod environment (committed, no secrets)
└── src/
    └── lib/axios.js         ← API base URL configured from VITE_API_BASE_URL
```

---

## Common Issues and Fixes

### Backend won't start — "Connection refused" to PostgreSQL

```bash
# Check PostgreSQL is running
pg_ctl status -D /usr/local/var/postgresql@15   # macOS
sudo systemctl status postgresql                 # Linux

# Restart PostgreSQL
brew services restart postgresql@15             # macOS
sudo systemctl restart postgresql               # Linux
```

### Backend: "Flyway found non-empty schema without schema history table"

The database already has tables but no Flyway history. Fix:

```bash
# Option 1: Drop and recreate (loses data)
psql -U dsanext_user -d dsanext_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Option 2: Baseline (marks existing state as V1)
mvn flyway:baseline -Dflyway.baselineVersion=1 \
    -Dflyway.url=jdbc:postgresql://localhost:5432/dsanext_db \
    -Dflyway.user=dsanext_user -Dflyway.password=dsanext_pass
```

### Backend: JWT secret too short

```
IllegalArgumentException: The specified key byte array is 31 bytes
```

Fix: ensure `JWT_SECRET` or the `dsanext.jwt.secret` property is at least 32 characters (256 bits for HS256).

### Frontend: CORS error in browser

Ensure the backend is running on port 8080 and the Vite proxy is configured:

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': { target: 'http://localhost:8080', changeOrigin: true }
  }
}
```

If calling the API directly (not through Vite proxy), add your origin to `dsanext.cors.allowed-origins` in `application-dev.yml`.

### Frontend: "Module not found" after pulling changes

```bash
cd frontend
rm -rf node_modules
npm install
```

### PostgreSQL: "role does not exist"

```sql
-- Connect as postgres superuser and recreate
psql -U postgres
CREATE USER dsanext_user WITH PASSWORD 'dsanext_pass';
GRANT ALL PRIVILEGES ON DATABASE dsanext_db TO dsanext_user;
```

### Port already in use

```bash
# Find and kill process on port 8080 (backend)
lsof -ti:8080 | xargs kill -9   # macOS/Linux
netstat -ano | findstr :8080     # Windows (then taskkill /PID <pid> /F)

# Find and kill process on port 5173 (frontend)
lsof -ti:5173 | xargs kill -9
```

---

## IDE Setup

### IntelliJ IDEA (recommended for backend)

1. Open `backend/` as a Maven project
2. Install plugins: **Lombok**, **Spring Boot**
3. Set Project SDK to Java 21
4. Run `DSANextApplication` with VM option: `-Dspring.profiles.active=dev`
5. Enable annotation processing: Settings → Build → Compiler → Annotation Processors → Enable

### VS Code (recommended for frontend)

Install extensions:
- **ESLint** — `dbaeumer.vscode-eslint`
- **Prettier** — `esbenp.prettier-vscode`
- **Tailwind CSS IntelliSense** — `bradlc.vscode-tailwindcss`
- **ES7+ React/Redux snippets** — `dsznajder.es7-react-js-snippets`

Add to `.vscode/settings.json`:
```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "tailwindCSS.experimental.classRegex": [
    ["clsx\\(([^)]*)\\)", "(?:'|\"|`)([^']*)(?:'|\"|`)"]
  ]
}
```

---

## Quick Start Script

Save as `scripts/start-dev.sh` and run with `bash scripts/start-dev.sh`:

```bash
#!/bin/bash
# DSANext — Quick Development Start Script
set -e

echo "🚀 Starting DSANext development environment..."

# Check prerequisites
command -v java  >/dev/null 2>&1 || { echo "❌ Java 21+ required"; exit 1; }
command -v mvn   >/dev/null 2>&1 || { echo "❌ Maven 3.9+ required"; exit 1; }
command -v node  >/dev/null 2>&1 || { echo "❌ Node.js 20+ required"; exit 1; }
command -v psql  >/dev/null 2>&1 || { echo "❌ PostgreSQL required"; exit 1; }

echo "✅ Prerequisites verified"

# Check PostgreSQL connection
PGPASSWORD=dsanext_pass psql -h localhost -U dsanext_user -d dsanext_db \
  -c "SELECT 1" >/dev/null 2>&1 || {
  echo "❌ PostgreSQL connection failed. Run setup steps 2-3 first."
  exit 1
}
echo "✅ PostgreSQL connected"

# Start backend in background
echo "🔧 Starting Spring Boot backend..."
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev -q &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"

# Wait for backend to be ready
echo "⏳ Waiting for backend to start..."
for i in {1..30}; do
  if curl -s http://localhost:8080/api/actuator/health | grep -q '"UP"'; then
    echo "✅ Backend ready at http://localhost:8080/api"
    break
  fi
  sleep 2
  echo "   Attempt $i/30..."
done

# Start frontend
echo "🎨 Starting React frontend..."
cd ../frontend
npm install --silent
npm run dev &
FRONTEND_PID=$!

echo ""
echo "✅ DSANext is running!"
echo ""
echo "   Frontend:  http://localhost:5173"
echo "   Backend:   http://localhost:8080/api"
echo "   Health:    http://localhost:8080/api/actuator/health"
echo ""
echo "   Admin:     admin@dsanext.com / Admin@123"
echo ""
echo "   Press Ctrl+C to stop all services"
echo ""

# Wait and handle shutdown
trap "echo ''; echo 'Stopping...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT
wait
```

Make it executable:
```bash
chmod +x scripts/start-dev.sh
bash scripts/start-dev.sh
```
