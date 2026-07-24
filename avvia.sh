#!/bin/bash

echo "=========================================="
echo "  Simulatore Bollette Luce - Avvio"
echo "=========================================="

# Colori
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verifica prerequisiti
echo ""
echo "[CHECK] Verifica prerequisiti..."

if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERRORE] Java non trovato. Installa OpenJDK 17:${NC}"
    echo "  sudo apt install openjdk-17-jdk"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo -e "${RED}[ERRORE] Node.js non trovato. Installa Node 20:${NC}"
    echo "  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -"
    echo "  sudo apt install -y nodejs"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo -e "${RED}[ERRORE] Docker non trovato. Installa Docker:${NC}"
    echo "  sudo apt install docker.io"
    exit 1
fi

echo -e "${GREEN}[OK] Tutti i prerequisiti sono soddisfatti${NC}"

# 1. Avvia PostgreSQL
echo ""
echo "[1/4] Avvio PostgreSQL..."
if docker ps | grep -q simulatore-postgres; then
    echo -e "${YELLOW}[INFO] PostgreSQL gia in esecuzione${NC}"
else
    docker run -d --name simulatore-postgres \
      -e POSTGRES_DB=simulatore_luce \
      -e POSTGRES_USER=simulatore \
      -e POSTGRES_PASSWORD=simulatore123 \
      -p 5432:5432 \
      -v postgres_data:/var/lib/postgresql/data \
      postgres:16-alpine 2>/dev/null

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[OK] PostgreSQL avviato${NC}"
    else
        echo -e "${YELLOW}[WARN] PostgreSQL potrebbe essere gia esistente, provo a startarlo...${NC}"
        docker start simulatore-postgres 2>/dev/null
    fi
fi

# 2. Attesa
echo ""
echo "[2/4] Attesa avvio PostgreSQL (10 secondi)..."
for i in {1..10}; do
    echo -n "."
    sleep 1
done
echo ""

# 3. Compila e avvia Backend
echo ""
echo "[3/4] Avvio Backend Spring Boot..."
cd backend

if [ ! -f "target/simulatore-bollette-luce-1.0.0.jar" ]; then
    echo "Compilazione in corso (prima volta, potrebbe richiedere qualche minuto)..."
    if [ -f "mvnw" ]; then
        chmod +x mvnw
        ./mvnw clean package -DskipTests
    else
        mvn clean package -DskipTests
    fi
fi

if [ -f "target/simulatore-bollette-luce-1.0.0.jar" ]; then
    nohup java -jar target/simulatore-bollette-luce-1.0.0.jar > ../backend.log 2>&1 &
    BACKEND_PID=$!
    echo -e "${GREEN}[OK] Backend avviato (PID: $BACKEND_PID)${NC}"
else
    echo -e "${RED}[ERRORE] JAR non trovato. Verifica la compilazione.${NC}"
    exit 1
fi
cd ..

# 4. Avvia Frontend
echo ""
echo "[4/4] Avvio Frontend React..."
cd frontend

if [ ! -d "node_modules" ]; then
    echo "Installazione dipendenze npm..."
    npm install
fi

nohup npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}[OK] Frontend avviato (PID: $FRONTEND_PID)${NC}"
cd ..

# Riepilogo
echo ""
echo "=========================================="
echo -e "${GREEN}  TUTTO AVVIATO CON SUCCESSO!${NC}"
echo "=========================================="
echo ""
echo "  Frontend:     http://localhost:5173"
echo "  Backend API:  http://localhost:8080"
echo "  Swagger UI:   http://localhost:8080/swagger-ui.html"
echo "  PostgreSQL:   localhost:5432"
echo ""
echo "  Log backend:  tail -f backend.log"
echo "  Log frontend: tail -f frontend.log"
echo ""
echo "  Per fermare tutto:"
echo "    ./ferma.sh"
echo ""
