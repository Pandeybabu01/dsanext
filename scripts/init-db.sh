#!/bin/bash
# DSANext — Database Initialization Script
# Run this once to set up the PostgreSQL database from scratch.
# Usage: bash scripts/init-db.sh
# Usage (custom): DB_PASSWORD=mypass bash scripts/init-db.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DATABASE_DIR="$PROJECT_ROOT/database"

# Configuration (override via environment variables)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-dsanext_db}"
DB_USERNAME="${DB_USERNAME:-dsanext_user}"
DB_PASSWORD="${DB_PASSWORD:-dsanext_pass}"
PG_SUPERUSER="${PG_SUPERUSER:-postgres}"

echo "⚡ DSANext — Database Initialization"
echo "======================================"
echo "  Host:     $DB_HOST:$DB_PORT"
echo "  Database: $DB_NAME"
echo "  User:     $DB_USERNAME"
echo ""

# Check psql
if ! command -v psql &>/dev/null; then
  echo "❌ psql not found. Install PostgreSQL client."
  exit 1
fi

# Check if we can connect as superuser
echo "🔍 Connecting as PostgreSQL superuser ($PG_SUPERUSER)..."
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -c "SELECT 1" >/dev/null 2>&1; then
  echo "❌ Cannot connect as '$PG_SUPERUSER'."
  echo "   On Linux: sudo -u postgres bash scripts/init-db.sh"
  echo "   On macOS: make sure PostgreSQL is running (brew services start postgresql@15)"
  exit 1
fi
echo "✅ Superuser connected"

# Create user (ignore error if already exists)
echo ""
echo "👤 Creating database user '$DB_USERNAME'..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -c \
  "DO \$\$ BEGIN
     IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$DB_USERNAME') THEN
       CREATE USER $DB_USERNAME WITH PASSWORD '$DB_PASSWORD';
     ELSE
       ALTER USER $DB_USERNAME WITH PASSWORD '$DB_PASSWORD';
     END IF;
   END \$\$;" 2>/dev/null
echo "✅ User ready"

# Create database (ignore error if already exists)
echo "🗄  Creating database '$DB_NAME'..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -c \
  "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1 || \
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -c \
  "CREATE DATABASE $DB_NAME OWNER $DB_USERNAME;"
echo "✅ Database ready"

# Grant privileges
psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -c \
  "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USERNAME;" 2>/dev/null

psql -h "$DB_HOST" -p "$DB_PORT" -U "$PG_SUPERUSER" -d "$DB_NAME" -c \
  "GRANT ALL ON SCHEMA public TO $DB_USERNAME;" 2>/dev/null
echo "✅ Privileges granted"

# Apply schema
echo ""
echo "📋 Applying database schema..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" -p "$DB_PORT" \
  -U "$DB_USERNAME" -d "$DB_NAME" \
  -v ON_ERROR_STOP=0 \
  -f "$DATABASE_DIR/schema.sql" 2>&1 | grep -v "already exists" | grep -v "^$" || true
echo "✅ Schema applied"

# Apply seed data
echo "🌱 Applying seed data..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" -p "$DB_PORT" \
  -U "$DB_USERNAME" -d "$DB_NAME" \
  -v ON_ERROR_STOP=0 \
  -f "$DATABASE_DIR/seed.sql" 2>&1 | grep -E "(ERROR|INSERT|row)" | head -20 || true
echo "✅ Seed data applied"

# Verify
echo ""
echo "🔍 Verifying setup..."
PGPASSWORD="$DB_PASSWORD" psql \
  -h "$DB_HOST" -p "$DB_PORT" \
  -U "$DB_USERNAME" -d "$DB_NAME" \
  -c "SELECT
        (SELECT count(*) FROM users)    AS users,
        (SELECT count(*) FROM platforms) AS platforms,
        (SELECT count(*) FROM problems)  AS problems,
        (SELECT count(*) FROM app_settings) AS settings;"

echo ""
echo "✅ DSANext database initialized successfully!"
echo ""
echo "   Admin credentials:"
echo "   Email:    admin@dsanext.com"
echo "   Password: Admin@123"
echo ""
echo "   ⚠️  Change the admin password after first login!"
echo ""
echo "   Next step: bash scripts/start-backend.sh"
