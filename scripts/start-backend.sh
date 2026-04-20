#!/bin/bash
# DSANext — Start Backend Development Server
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"

echo "⚡ DSANext — Backend Startup"
echo "================================"

# Check Java
if ! command -v java &>/dev/null; then
  echo "❌ Java 21+ is required. Install from https://adoptium.net"
  exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
  echo "❌ Java 21+ required, found Java $JAVA_VER"
  exit 1
fi
echo "✅ Java $(java -version 2>&1 | head -1)"

# Check Maven
if ! command -v mvn &>/dev/null; then
  echo "❌ Maven 3.9+ is required. Install from https://maven.apache.org"
  exit 1
fi
echo "✅ Maven $(mvn -version 2>&1 | head -1 | awk '{print $3}')"

# Check PostgreSQL connection
echo ""
echo "🔍 Checking PostgreSQL connection..."
PGPASSWORD="${DB_PASSWORD:-dsanext_pass}" psql \
  -h "${DB_HOST:-localhost}" \
  -p "${DB_PORT:-5432}" \
  -U "${DB_USERNAME:-dsanext_user}" \
  -d "${DB_NAME:-dsanext_db}" \
  -c "SELECT 1" >/dev/null 2>&1 || {
  echo "❌ Cannot connect to PostgreSQL."
  echo "   Make sure PostgreSQL is running and the database is set up."
  echo "   See docs/setup-guide.md Step 2 for instructions."
  exit 1
}
echo "✅ PostgreSQL connected"

# Start Spring Boot
echo ""
echo "🚀 Starting Spring Boot (dev profile)..."
echo "   API: http://localhost:${SERVER_PORT:-8080}/api"
echo "   Health: http://localhost:${SERVER_PORT:-8080}/api/actuator/health"
echo ""

cd "$BACKEND_DIR"
mvn spring-boot:run \
  -Dspring-boot.run.profiles="${SPRING_PROFILES_ACTIVE:-dev}" \
  -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m"
