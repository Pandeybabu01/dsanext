#!/bin/bash
# DSANext — Start Frontend Development Server
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

echo "⚡ DSANext — Frontend Startup"
echo "================================"

# Check Node.js
if ! command -v node &>/dev/null; then
  echo "❌ Node.js 20+ is required. Install from https://nodejs.org"
  exit 1
fi

NODE_VER=$(node -v | cut -c2- | cut -d'.' -f1)
if [ "$NODE_VER" -lt 20 ] 2>/dev/null; then
  echo "❌ Node.js 20+ required, found v$NODE_VER"
  exit 1
fi
echo "✅ Node.js $(node -v)"
echo "✅ npm $(npm -v)"

# Install dependencies if needed
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ] || [ "package.json" -nt "node_modules" ]; then
  echo ""
  echo "📦 Installing dependencies..."
  npm install
fi
echo "✅ Dependencies ready"

# Check backend connectivity
echo ""
echo "🔍 Checking backend connection..."
if curl -sf http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
  echo "✅ Backend is running at http://localhost:8080/api"
else
  echo "⚠️  Backend not detected at http://localhost:8080"
  echo "   Start the backend first: bash scripts/start-backend.sh"
  echo "   Continuing anyway — API calls will fail until backend is up."
fi

# Start Vite dev server
echo ""
echo "🚀 Starting Vite development server..."
echo "   Frontend: http://localhost:5173"
echo "   API proxy: /api → http://localhost:8080/api"
echo ""

npm run dev
