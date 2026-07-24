# Deploy su Fly.io

Guida per pubblicare il Simulatore Bollette online, gratis e sempre attivo (no sleep),
con i dati persistenti. Risultato: un URL tipo `https://simulatore-bollette.fly.dev`.

> **La preparazione del codice e' gia' fatta.** Questo repository contiene gia' il
> `Dockerfile` unificato, il `SpaController`, l'`application.yml` parametrico, il
> `fly.toml`, l'`api/client.js` con path relativo e i controller senza `@CrossOrigin`.
> Puoi saltare direttamente all'**installazione di flyctl**.

## Indice
1. [Architettura su Fly](#1-architettura-su-fly)
2. [Cosa e' gia' pronto nel repo](#2-cosa-e-gia-pronto-nel-repo)
3. [Prerequisiti](#3-prerequisiti)
4. [Installazione di flyctl](#4-installazione-di-flyctl)
5. [Creazione dell'app](#5-creazione-dellapp)
6. [Creazione del database](#6-creazione-del-database)
7. [Deploy](#7-deploy)
8. [Verifica](#8-verifica)
9. [Protezione dai costi](#9-protezione-dai-costi)
10. [Aggiornamenti](#10-aggiornamenti)
11. [Risoluzione problemi](#11-risoluzione-problemi)
12. [Checklist](#12-checklist)

---

## 1. Architettura su Fly

In locale (docker-compose) il progetto gira come tre container. Su Fly conviene **una sola
app** (frontend dentro il JAR) + **un database**:

```
┌─────────────────────────────────────┐      ┌──────────────────┐
│  simulatore-bollette                │      │  simulatore-db   │
│   Spring Boot                       │─────▶│  PostgreSQL 16   │
│    ├─ /api/**  → REST               │      │  volume 1 GB     │
│    └─ /**      → React (statico)    │      │  rete .flycast   │
│   512 MB · sempre attiva            │      └──────────────────┘
└─────────────────────────────────────┘
```

Unire frontend e backend elimina il CORS (stessa origine), usa una sola VM (tier gratuito)
e un solo comando di deploy.

## 2. Cosa e' gia' pronto nel repo

| File | Ruolo |
|---|---|
| `Dockerfile` (radice) | build a 3 stage: React → `static/` del JAR → runtime JRE |
| `.dockerignore` | esclude node_modules/target/dist dal contesto |
| `fly.toml` | config Fly: always-on, health check, 512 MB, `DDL_AUTO=validate` |
| `backend/.../config/SpaController.java` | inoltra le rotte React a `index.html` (no 404 sul refresh) |
| `backend/.../resources/application.yml` | `server.port=${PORT:8080}`, datasource e Flyway parametrici |
| `frontend/src/api/client.js` | `baseURL: '/api'` (path relativo) |
| controller | `@CrossOrigin` rimosse, CORS centralizzato e configurabile |

> **Nota schema DB.** Lo schema e' gestito da **Flyway** (`V1__schema_iniziale.sql`) e
> Hibernate lo **valida** (`ddl-auto: validate`). Per questo `fly.toml` imposta
> `DDL_AUTO=validate`, **non** `update`: al primo avvio su un Postgres vuoto Flyway crea
> tutte le tabelle e il `DataInitializer` inserisce i dati di esempio (i due profili
> parametri, la bolletta FuturEnergy, le offerte del gestore).

## 3. Prerequisiti

- Account Fly.io ([fly.io/app/sign-up](https://fly.io/app/sign-up)) con carta di credito
  (solo verifica identita'; al punto 9 mettiamo il limite di spesa a 0).
- `git`. Docker solo se vuoi provare il build in locale.

## 4. Installazione di flyctl

```bash
# macOS / Linux
curl -L https://fly.io/install.sh | sh
# Windows PowerShell
iwr https://fly.io/install.ps1 -useb | iex
# macOS Homebrew
brew install flyctl
```

```bash
fly version
fly auth login
```

## 5. Creazione dell'app

Dalla radice del progetto:

```bash
fly launch --no-deploy
```

| Domanda | Risposta |
|---|---|
| App name | `simulatore-bollette` (se occupato aggiungi un suffisso e aggiornalo in `fly.toml`) |
| Region | `fra` (Francoforte) o `cdg` (Parigi) |
| Postgres / Redis | **No** (il DB lo creiamo dopo) |
| Overwrite fly.toml? | **No** — quello nel repo e' gia' pronto |
| Deploy now? | **No** |

Se `fly launch` prova a sovrascrivere `fly.toml`, rispondi No e verifica che `app = "..."`
corrisponda al nome scelto.

## 6. Creazione del database

```bash
fly postgres create --name simulatore-db --region fra
```

| Domanda | Risposta |
|---|---|
| Select configuration | **Development** (single node, 256 MB, 1 GB disk) |
| Scale to zero after 1h? | **No** ⚠️ (altrimenti il DB va in sleep) |

Salva le credenziali stampate (la password non e' piu' recuperabile). Poi collega il DB:

```bash
fly postgres attach simulatore-db --app simulatore-bollette
```

Questo crea `DATABASE_URL` in formato libpq. Spring vuole invece tre secret in formato
JDBC. Leggi il valore generato:

```bash
fly ssh console --app simulatore-bollette -C "printenv DATABASE_URL"
# es: postgres://simulatore_bollette:PASSWORD@simulatore-db.flycast:5432/simulatore_bollette
```

e convertilo (nota il prefisso `jdbc:` e l'host `.flycast`):

```bash
fly secrets set \
  SPRING_DATASOURCE_URL="jdbc:postgresql://simulatore-db.flycast:5432/simulatore_bollette" \
  SPRING_DATASOURCE_USERNAME="simulatore_bollette" \
  SPRING_DATASOURCE_PASSWORD="LA_PASSWORD_DAL_COMANDO_SOPRA" \
  --app simulatore-bollette

fly secrets list --app simulatore-bollette
```

## 7. Deploy

```bash
fly deploy
```

Il primo deploy richiede ~8-15 minuti (Maven scarica le dipendenze; i successivi 3-5 min).
Alla fine:

```
1 desired, 1 placed, 1 healthy [health checks: 1 passing]
Visit your app at https://simulatore-bollette.fly.dev/
```

## 8. Verifica

```bash
# 1. Salute (Flyway migrato + DB connesso)
curl https://simulatore-bollette.fly.dev/actuator/health         # {"status":"UP"}

# 2. Seed: offerte del gestore
curl https://simulatore-bollette.fly.dev/api/offerte

# 3. Seed: profilo parametri predefinito e bolletta di esempio
curl https://simulatore-bollette.fly.dev/api/parametri-gestore/predefinito
curl https://simulatore-bollette.fly.dev/api/bollette-concorrenti

# 4. Confronto end-to-end (bolletta FuturEnergy seedata = id 1, offerta trioraria = id 1)
curl -X POST https://simulatore-bollette.fly.dev/api/confronti \
  -H "Content-Type: application/json" \
  -d '{"bollettaConcorrenteId":1,"offertaGestoreId":1}'
```

L'ultima chiamata deve restituire `risparmioBimestrale`, `risparmioAnnuale`, le 5 categorie
e il dettaglio dei mesi: il ciclo frontend → API → motore → DB funziona.

Nel browser apri `https://simulatore-bollette.fly.dev` e verifica: l'interfaccia carica, la
navigazione tra `/bollette`, `/confronto`, `/parametri` funziona, **un refresh su
`/confronto` non da' 404** (grazie al `SpaController`), la console (F12) non ha errori.

## 9. Protezione dai costi

1. [fly.io/dashboard](https://fly.io/dashboard) → **Billing** → **Spend limits** → **0 $**.
2. Attiva gli avvisi email sotto **Billing → Notifications**.

Consumo atteso (dentro il tier gratuito): app 1× shared-cpu-1x 512 MB + DB 256 MB + volume
1 GB. Le 3 VM gratuite da 256 MB sono cumulabili.

## 10. Aggiornamenti

```bash
fly deploy            # zero downtime: la nuova VM passa l'health check prima di spegnere la vecchia
fly logs              # log in tempo reale
fly status
```

Rollback: `fly releases` poi `fly deploy --image <ref-precedente>`.

Deploy automatico da GitHub (opzionale): crea il secret `FLY_API_TOKEN`
(`fly tokens create deploy -x 999999h`) e un workflow che gira `flyctl deploy --remote-only`
sui push del branch.

## 11. Risoluzione problemi

| Sintomo | Causa / Soluzione |
|---|---|
| `Schema-validation: missing table/column` | Flyway non ha migrato: controlla che `FLYWAY_ENABLED=true` e che il DB sia vuoto al primo avvio. Non impostare `DDL_AUTO=update`. |
| `Connection refused` al DB | Secret mancanti o URL senza `jdbc:`/`.flycast`. `fly secrets list`. |
| Health check in loop | Spring parte in >90s: alza `grace_period` a `"120s"` in `fly.toml` e rideploya. |
| `OutOfMemoryError` | `fly scale memory 512 --app simulatore-bollette`. |
| Pagina bianca | Il build React non e' entrato nel JAR: `fly ssh console -C "ls /app"` e verifica che l'immagine contenga `BOOT-INF/classes/static/index.html`. |
| 404 su `/confronto` dopo refresh | `SpaController` non intercetta: controlla che la classe sia nel package scansionato. |
| Chiamate API a `localhost:8080` | `grep -rn "localhost:8080" frontend/src` deve essere vuoto (gia' verificato nel repo). |
| L'app va in sleep | `fly config show \| grep auto_stop` deve dare `false`; verifica anche che il DB non sia scale-to-zero. |

## 12. Checklist

- [ ] `fly launch --no-deploy` eseguito, `fly.toml` con il nome app giusto
- [ ] DB creato **senza** scale-to-zero
- [ ] Tre secret `SPRING_DATASOURCE_*` impostati (URL con `jdbc:` e `.flycast`)
- [ ] `fly deploy` completato
- [ ] `/actuator/health` → `UP`
- [ ] `/api/offerte`, `/api/parametri-gestore/predefinito`, `/api/bollette-concorrenti` → dati di seed
- [ ] `POST /api/confronti` → risparmio calcolato
- [ ] Refresh su `/confronto` non da' 404
- [ ] Spend limit a 0 $ e avvisi email attivi
- [ ] Credenziali DB salvate

---

## Messaggio per il collega

```
Ciao,
il simulatore bollette e' online:
https://simulatore-bollette.fly.dev

Non serve installare niente, si apre dal browser.

Le pagine:
· Bollette   → inserisci la bolletta del cliente da comparare
· Confronto  → confronta i consumi con un'offerta del gestore e vedi il risparmio
· Offerte    → catalogo offerte del gestore
· Parametri  → configurazione (parametri nazionali + corrispettivi del gestore)
· Storico    → i confronti salvati

Documentazione API: https://simulatore-bollette.fly.dev/swagger-ui.html
I dati sono di esempio: puoi modificarli e cancellarli liberamente.
```
