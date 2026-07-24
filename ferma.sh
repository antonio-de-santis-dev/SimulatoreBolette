#!/usr/bin/env bash
set -euo pipefail

# ════════════════════════════════════════════════════════════════════════════
#  Simulatore Bollette Luce — stop
#  Uso: ./ferma.sh [--volumes]
#       --volumes  rimuove anche i dati di PostgreSQL (volume docker)
# ════════════════════════════════════════════════════════════════════════════

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RIMUOVI_VOLUMI="no"
[ "${1:-}" = "--volumes" ] && RIMUOVI_VOLUMI="si"

echo "=== Simulatore Bollette Luce — stop ==="

# 1. Docker compose (modalita docker)
if command -v docker >/dev/null 2>&1 && [ -f "$ROOT/docker/docker-compose.yml" ]; then
  if [ "$RIMUOVI_VOLUMI" = "si" ]; then
    (cd "$ROOT/docker" && docker compose down --volumes) || true
    echo "[OK] Container e volumi rimossi"
  else
    (cd "$ROOT/docker" && docker compose down) || true
    echo "[OK] Container arrestati"
  fi
fi

# 2. Processi locali (modalita local)
pkill -f 'java -jar.*target' 2>/dev/null || true
pkill -f 'vite' 2>/dev/null || true

# 3. PostgreSQL standalone (avviato in modalita local)
if command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' | grep -q simulatore-postgres; then
  docker stop simulatore-postgres >/dev/null 2>&1 || true
  if [ "$RIMUOVI_VOLUMI" = "si" ]; then
    docker rm simulatore-postgres >/dev/null 2>&1 || true
  fi
fi

echo "[OK] Servizi arrestati."
