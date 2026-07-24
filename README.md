# Simulatore Bollette Luce

Applicazione full-stack per la simulazione e comparazione dei prezzi delle bollette della luce.

## Stack Tecnologico

- **Backend:** Java 17, Spring Boot 3.2, PostgreSQL 16
- **Frontend:** React 18, Vite, Tailwind CSS, Recharts
- **Container:** Docker, Docker Compose

## Struttura Progetto

```
simulatore-bollette-luce/
├── backend/          # Spring Boot API
├── frontend/         # React SPA
└── docker/           # Docker Compose
```

## Avvio Rapido

### Opzione 1: Docker Compose (consigliata)

```bash
cd docker
docker-compose up --build
```

L'app sara disponibile su:
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Opzione 2: Sviluppo Locale

**1. Avvia PostgreSQL:**
```bash
docker run -d --name postgres -e POSTGRES_DB=simulatore_luce \
  -e POSTGRES_USER=simulatore -e POSTGRES_PASSWORD=simulatore123 \
  -p 5432:5432 postgres:16-alpine
```

**2. Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**3. Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Funzionalita

- Simulazione bolletta bimestrale con componenti ARERA
- Confronto offerte a prezzo fisso e indicizzate al PUN
- Gestione offerte fornitori
- Storico simulazioni
- Grafici interattivi (pie chart, bar chart)

## Componenti Bolletta

1. **Materia Energia** - variabile per fornitore
2. **Trasporto e Contatore** (Tau + UC) - regolato ARERA
3. **Oneri di Sistema** (ASOS + ARIM) - regolato ARERA
4. **Imposte** (Accise + IVA) - regolato stato

## API Endpoints

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | /api/simulazioni | Crea nuova simulazione |
| GET | /api/simulazioni | Lista simulazioni |
| GET | /api/offerte | Lista offerte |
| GET | /api/offerte/attive | Offerte attive |
| POST | /api/offerte | Crea offerta |
| GET | /api/pun | Storico PUN |
