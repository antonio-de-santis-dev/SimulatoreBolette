# GUIDA INSTALLAZIONE - Simulatore Bollette Luce

## PROBLEMA: docker-compose non installato

Se `docker-compose` non e installato, ci sono 3 modi per avviare il progetto:

---

## METODO 1: Docker Compose (consigliato se installabile)

```bash
# Installa docker-compose
sudo apt update
sudo apt install docker-compose -y

# Poi avvia normalmente
cd simulatore-bollette-luce/docker
docker-compose up --build
```

---

## METODO 2: Docker run manuale (SENZA docker-compose)

### Passo 1: Avvia PostgreSQL
```bash
docker run -d --name simulatore-postgres \
  -e POSTGRES_DB=simulatore_luce \
  -e POSTGRES_USER=simulatore \
  -e POSTGRES_PASSWORD=simulatore123 \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:16-alpine
```

### Passo 2: Compila e avvia il Backend
```bash
cd simulatore-bollette-luce/backend

# Se hai Maven installato:
mvn clean package -DskipTests

# Se NON hai Maven, usa il wrapper (mvnw):
# Su Linux/Mac:
chmod +x mvnw
./mvnw clean package -DskipTests

# Avvia il JAR
java -jar target/simulatore-bollette-luce-1.0.0.jar
```

### Passo 3: Avvia il Frontend
```bash
cd simulatore-bollette-luce/frontend

# Installa Node.js 20+ se non ce l'hai:
# https://nodejs.org/

npm install
npm run dev
```

---

## METODO 3: Script automatico di avvio (Bash)

Salva questo come `avvia.sh` nella cartella principale del progetto:

```bash
#!/bin/bash

echo "=== Avvio Simulatore Bollette Luce ==="

# 1. Avvia PostgreSQL
echo "[1/4] Avvio PostgreSQL..."
docker run -d --name simulatore-postgres \
  -e POSTGRES_DB=simulatore_luce \
  -e POSTGRES_USER=simulatore \
  -e POSTGRES_PASSWORD=simulatore123 \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:16-alpine 2>/dev/null || echo "PostgreSQL gia avviato o errore"

# 2. Attendi che PostgreSQL sia pronto
echo "[2/4] Attesa avvio PostgreSQL (10s)..."
sleep 10

# 3. Avvia Backend
echo "[3/4] Avvio Backend Spring Boot..."
cd backend
if [ ! -f "target/simulatore-bollette-luce-1.0.0.jar" ]; then
    echo "Compilazione backend in corso..."
    ./mvnw clean package -DskipTests
fi
java -jar target/simulatore-bollette-luce-1.0.0.jar &
BACKEND_PID=$!
cd ..

# 4. Avvia Frontend
echo "[4/4] Avvio Frontend React..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "Installazione dipendenze frontend..."
    npm install
fi
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "=== TUTTO AVVIATO ==="
echo "Frontend: http://localhost:5173"
echo "Backend API: http://localhost:8080"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Per fermare: kill $BACKEND_PID $FRONTEND_PID"
echo "Oppure: pkill -f 'java -jar' && pkill -f 'vite'"
```

Rendi eseguibile e avvia:
```bash
chmod +x avvia.sh
./avvia.sh
```

---

## REQUISITI SISTEMA

| Software | Versione minima | Installazione |
|----------|----------------|---------------|
| Java JDK | 17 | `sudo apt install openjdk-17-jdk` |
| Maven | 3.8+ | `sudo apt install maven` (opzionale, c'e mvnw) |
| Node.js | 20 | https://nodejs.org/ o `nvm` |
| Docker | qualsiasi | `sudo apt install docker.io` |
| PostgreSQL | 16 (via Docker) | automatico con Docker |

---

## VERIFICA INSTALLAZIONE

```bash
# Controlla versioni
java -version
node -v
npm -v
docker --version
```

---

## STRUTTURA URL DOPO L'AVVIO

| Servizio | URL |
|----------|-----|
| Frontend React | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI (docs API) | http://localhost:8080/swagger-ui.html |
| API Docs JSON | http://localhost:8080/api-docs |
| PostgreSQL | localhost:5432 |

---

## TROUBLESHOOTING

### Errore: "Porta 5432 gia in uso"
```bash
# Ferma il container esistente
docker stop simulatore-postgres
docker rm simulatore-postgres
# Poi riavvia
```

### Errore: "mvnw non trovato"
```bash
# Installa Maven globalmente
sudo apt install maven
# Oppure scarica il wrapper
mvn wrapper:wrapper
```

### Errore: "npm non trovato"
```bash
# Installa Node.js
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

### Errore: "Connection refused" al backend
```bash
# Verifica che il backend sia avviato
curl http://localhost:8080/actuator/health

# Se non risponde, controlla i log del backend
# Il backend impiega ~30 secondi per avviarsi la prima volta
```

---

## COMANDI UTILI

```bash
# Ferma tutto
pkill -f 'java -jar'
pkill -f 'vite'
docker stop simulatore-postgres

# Pulizia completa
docker rm -f simulatore-postgres
docker volume rm postgres_data

# Ricompila tutto
cd backend && ./mvnw clean package -DskipTests
cd frontend && rm -rf node_modules && npm install
```
