#!/usr/bin/env bash
set -euo pipefail

# ════════════════════════════════════════════════════════════════════════════
#  Simulatore Bollette Luce — avvio
#  Uso: ./avvia.sh [docker|local]   (default: docker)
# ════════════════════════════════════════════════════════════════════════════

MODE="${1:-docker}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
err()  { echo -e "${RED}[ERRORE]${NC} $1"; }

wait_for() {  # wait_for <url> <nome> <tentativi>
  local url="$1" nome="$2" tentativi="${3:-60}"
  for _ in $(seq 1 "$tentativi"); do
    if curl -sf "$url" >/dev/null 2>&1; then ok "$nome pronto"; return 0; fi
    sleep 2
  done
  err "$nome non risponde ($url)"; return 1
}

porta_libera() {  # porta_libera <porta>
  if command -v lsof >/dev/null 2>&1 && lsof -i ":$1" -sTCP:LISTEN >/dev/null 2>&1; then
    warn "La porta $1 e' gia' occupata"
    return 1
  fi
  return 0
}

richiedi() {  # richiedi <comando> <versione-minima> <hint>
  if ! command -v "$1" >/dev/null 2>&1; then
    err "$1 non trovato. Versione minima consigliata: $2. $3"
    exit 1
  fi
}

echo "=== Simulatore Bollette Luce — modalita: $MODE ==="

if [ "$MODE" = "docker" ]; then
  richiedi docker "24" "Installa Docker Engine"
  if [ ! -f "$ROOT/docker/.env" ]; then
    warn "docker/.env mancante: lo creo da .env.example"
    cp "$ROOT/docker/.env.example" "$ROOT/docker/.env"
  fi
  porta_libera 80   || true
  porta_libera 8080 || true

  echo "[1/2] docker compose up --build -d"
  (cd "$ROOT/docker" && docker compose up --build -d)

  echo "[2/2] Attesa healthcheck backend..."
  wait_for "http://localhost:8080/actuator/health" "Backend" 90
  wait_for "http://localhost/" "Frontend" 30 || true

  echo ""
  ok "Avviato"
  echo "  Frontend:    http://localhost"
  echo "  Backend API: http://localhost:8080"
  echo "  Swagger UI:  http://localhost:8080/swagger-ui.html"

elif [ "$MODE" = "local" ]; then
  richiedi java "17" "Installa OpenJDK 17"
  richiedi node "20" "Installa Node.js 20+"
  richiedi mvn  "3.9" "Installa Maven"
  richiedi docker "24" "Serve per PostgreSQL"
  porta_libera 5432 || true
  porta_libera 8080 || true
  porta_libera 5173 || true

  echo "[1/3] PostgreSQL in Docker..."
  if [ ! -f "$ROOT/docker/.env" ]; then cp "$ROOT/docker/.env.example" "$ROOT/docker/.env"; fi
  # shellcheck disable=SC1091
  set -a; . "$ROOT/docker/.env"; set +a
  if ! docker ps --format '{{.Names}}' | grep -q simulatore-postgres; then
    docker run -d --name simulatore-postgres \
      -e POSTGRES_DB="$POSTGRES_DB" -e POSTGRES_USER="$POSTGRES_USER" \
      -e POSTGRES_PASSWORD="$POSTGRES_PASSWORD" -p 5432:5432 \
      postgres:16-alpine >/dev/null || docker start simulatore-postgres
  fi
  sleep 5; ok "PostgreSQL avviato"

  echo "[2/3] Backend Spring Boot..."
  export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/${POSTGRES_DB}"
  export SPRING_DATASOURCE_USERNAME="${POSTGRES_USER}"
  export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}"
  (cd "$ROOT/backend" && mvn -q clean package -DskipTests \
    && nohup java -jar target/*.jar > "$ROOT/backend.log" 2>&1 &)
  wait_for "http://localhost:8080/actuator/health" "Backend" 90

  echo "[3/3] Frontend dev server..."
  (cd "$ROOT/frontend" && { [ -d node_modules ] || npm install; } \
    && nohup npm run dev > "$ROOT/frontend.log" 2>&1 &)
  wait_for "http://localhost:5173/" "Frontend" 30 || true

  echo ""
  ok "Avviato"
  echo "  Frontend:    http://localhost:5173"
  echo "  Backend API: http://localhost:8080"
  echo "  Log:         backend.log / frontend.log"

else
  err "Modalita' non valida: $MODE (usa 'docker' o 'local')"
  exit 1
fi
