# Simulatore Bollette Luce

Simulatore e comparatore di bollette elettriche italiane. Presi i consumi reali di
un cliente (letti dalla sua bolletta attuale) li ricalcola con i corrispettivi di un
gestore e mostra, voce per voce, quanto si risparmierebbe.

Il modello di calcolo replica fedelmente il foglio `SIMULATORE_BIMESTRALE_LUCE.xlsx` ed
e' stato validato contro una fattura reale FuturEnergy (n. 1237600).

## Indice

1. [Descrizione e scopo](#1-descrizione-e-scopo)
2. [Stack tecnologico](#2-stack-tecnologico)
3. [Il modello a due sorgenti dati](#3-il-modello-a-due-sorgenti-dati)
4. [Architettura](#4-architettura)
5. [Modello dati](#5-modello-dati)
6. [Logica di calcolo](#6-logica-di-calcolo)
7. [API REST](#7-api-rest)
8. [Frontend](#8-frontend)
9. [Area parametri gestore](#9-area-parametri-gestore)
10. [Come inserire una bolletta da comparare](#10-come-inserire-una-bolletta-da-comparare)
11. [Avvio](#11-avvio)
12. [Test](#12-test)
13. [Conformita al modello Excel](#13-conformita-al-modello-excel)

---

## 1. Descrizione e scopo

Un consulente energetico inserisce la bolletta del fornitore attuale del cliente (con i
kWh consumati e gli importi gia' fatturati) e la confronta con una o piu' offerte del
proprio gestore. Il sistema ricalcola la bolletta usando i corrispettivi del gestore sugli
stessi kWh e produce un confronto categoria per categoria, con il risparmio bimestrale e
annuale.

Il problema risolto rispetto alla versione precedente: il vecchio motore **sottostimava il
totale di circa il 20%** e non distingueva le due sorgenti di dati che il modello Excel
tiene rigorosamente separate.

## 2. Stack tecnologico

| Componente | Tecnologia | Versione |
|---|---|---|
| Backend | Spring Boot | 3.2.0 |
| Linguaggio | Java | 17 |
| ORM | Hibernate / Spring Data JPA | 6.3 |
| Migrazioni | Flyway | 9.x |
| Mapping DTO | MapStruct | 1.5.5 |
| Database | PostgreSQL | 16 |
| Database test | H2 (modalita PostgreSQL) | — |
| Frontend | React | 18 |
| Build frontend | Vite | 5 |
| Grafici | Recharts | 2.10 |
| Stile | Tailwind CSS | 3.3 |
| Container | Docker + Docker Compose | — |

Tutti gli importi sono in `BigDecimal` con `RoundingMode.HALF_UP` (mai `double`).

## 3. Il modello a due sorgenti dati

Il cuore del refactoring. Nel foglio Excel le uniche celle di input sono in **giallo**: i
consumi F1/F2/F3, la potenza, e la quota Asos. Tutto il resto e' **configurazione**. Da qui
la separazione in due sorgenti:

### Sorgente A — Bolletta da comparare (`BollettaConcorrente`)

Dati del concorrente, inseriti a mano leggendo il PDF del cliente. Chi la inserisce: il
consulente. Contiene:

- Anagrafica cliente, POD, fornitore, offerta
- Periodo di fatturazione
- **Consumi F1/F2/F3 per ciascun mese** ← le celle gialle dell'Excel
- Potenza impegnata e disponibile, livello di tensione
- Gli importi **gia' fatturati** dal concorrente (termine di paragone)
- Le altre partite addebitate, l'aliquota IVA applicata

### Sorgente B — Bolletta del gestore (`ParametriGestore`)

Configurazione, modificabile nell'area dedicata. Chi la configura: l'amministratore del
gestore. Contiene:

- I **corrispettivi commerciali** del gestore (commercializzazione, PCV, spread, quote
  energia per fascia — queste ultime sull'offerta)
- I **parametri nazionali ARERA** (Asos, Arim, accisa, trasporto, coefficienti di perdita,
  corrispettivi di dispacciamento)

### Raffronto delle due sorgenti

| | Sorgente A — Bolletta concorrente | Sorgente B — Bolletta gestore |
|---|---|---|
| Cosa contiene | consumi reali + importi fatturati | corrispettivi + parametri |
| Chi la fornisce | il cliente (via PDF) | il gestore (configurazione) |
| Celle Excel | gialle (input) | colonna C (config) |
| Entity | `BollettaConcorrente`, `MeseBolletta`, `AltraPartita` | `ParametriGestore`, `VoceCorrispettivo` |
| Modificabile in | pagina **Bollette** | pagina **Parametri** |

### Il flusso del confronto

```
kWh del concorrente  +  corrispettivi del gestore  →  bolletta ricalcolata  →  differenza
   (Sorgente A)              (Sorgente B)               (motore a righe)     (voce per voce)
```

Ogni `VoceCorrispettivo` dichiara la sua **origine** (🏛️ Nazionale / 🏢 Gestore / 📄 Offerta):
e' cio' che rende visibile all'utente la separazione tra le due sorgenti.

## 4. Architettura

```
Controller  →  Service  →  MotoreCalcolo (calcolo puro)
    ↓            ↓
   DTO      Repository (Spring Data JPA)
                 ↓
              Entity  ←→  Flyway (schema) + PostgreSQL
```

- **Controller** (`/controller`): espongono le API REST, accettano DTO / entity.
- **Service** (`/service`): logica applicativa e transazioni (`ConfrontoService`,
  `ParametriGestoreService`, `BollettaConcorrenteService`).
- **MotoreCalcolo** (`/calculation`): calcolo puro, senza dipendenze dal DB — testabile in
  isolamento.
- **Repository** (`/repository`): accesso dati.
- **Entity** (`/entity`) + **enums** (`/enums`): modello di dominio.
- Gli errori passano da `GlobalExceptionHandler`: `ResourceNotFoundException` → 404,
  `IllegalArgument/IllegalState` → 400, altre `RuntimeException` → 500.

## 5. Modello dati

| Entity | Ruolo |
|---|---|
| `BollettaConcorrente` | Sorgente A: la bolletta da comparare |
| `MeseBolletta` | un mese di consumi (F1/F2/F3), 1 o 2 per bimestre |
| `AltraPartita` | riga di altra partita (importo anche negativo, flag `soggettaIva`) |
| `ParametriGestore` | Sorgente B: profilo di parametri e corrispettivi |
| `VoceCorrispettivo` | riga generica del motore (categoria, base, origine, corrispettivo) |
| `Offerta` | offerta del gestore, con la lista di `voci` (quote energia per fascia) |
| `Confronto` | esito salvato di un confronto (storico) |
| `ParametroARERA`, `PunMensile`, `Simulazione` | legacy del vecchio motore `/api/simulazioni` |

## 6. Logica di calcolo

La sezione piu' importante.

### 6.1 Struttura bimestrale

Ogni bimestre = **due mesi calcolati separatamente**, con corrispettivi propri, poi sommati.
Non si moltiplica un mese per due: nel foglio Excel i due mesi hanno prezzi energia e
corrispettivi di dispacciamento diversi.

### 6.2 Motore a righe

Ogni voce e' una riga: `TOTALE = CORRISPETTIVO × QUANTITA`. La quantita e' determinata dalla
`BaseQuantita`:

| Base | Quantita |
|---|---|
| `FISSO_MESE` | 1 |
| `POD_MESE` | 1 |
| `KW_MESE` | potenza in kW (**gia' mensile, NON diviso per 12**) |
| `KWH_NETTI` | F1+F2+F3 |
| `KWH_CON_PERDITE` | F1+F2+F3 + perdite arrotondate |
| `KWH_F1` / `KWH_F2` / `KWH_F3` | kWh della fascia |
| `PERDITE_F1` / `PERDITE_F2` / `PERDITE_F3` | perdite della fascia |

### 6.3 Perdite di rete — regola critica

Le perdite si applicano alla **quantita**, arrotondata a intero, **non al prezzo**:

```
kWh_perdite_F1 = ROUND(kWh_F1 × coefficiente, 0)
importo_perdite_F1 = prezzo_F1 × kWh_perdite_F1
```

Esempio: 24 kWh F1 con coefficiente 10% → `ROUND(2,4) = 2` kWh (non 2,4). In Java:
`.setScale(0, RoundingMode.HALF_UP)`. Il coefficiente e' parametrico per livello di tensione:

| Tensione | Coefficiente |
|---|---|
| 370 kW | 0,70% |
| 220 kW | 1,10% |
| ≤ 150 kW | 1,80% |
| MT | 4,70% |
| **BT** | **10,40%** |

Il profilo "Modello Excel" usa il 10% tondo per far quadrare i casi di verita; il profilo
"Standard ARERA 2026" usa il 10,40% reale della bassa tensione. L'arrotondamento e'
disattivabile con il flag `arrotondaPerdite`.

### 6.4 Le due basi di quantita

| Base | Formula | Usata da |
|---|---|---|
| `KWH_NETTI` | F1+F2+F3 | trasporto scaglione, Arim, Asos variabile, accisa |
| `KWH_CON_PERDITE` | netti + perdite arrotondate | MSD, DIS/RTN, INT, UES, SAL, mercato capacita, PCV, sbilanciamento |

Esempio reale: 94 kWh netti → 103 kWh con perdite.

### 6.5 Le cinque categorie

- **Materia energia** — commercializzazione (🏢), DISPBT (🏛️), quote energia F1/F2/F3 (📄),
  perdite (📄), e i corrispettivi di dispacciamento (🏛️/🏢).
- **Trasporto e gestione contatore** — 3 voci nazionali (€/kW, €/pod/mese, €/kWh). UC3
  rimosso (non esiste nell'Excel).
- **Oneri di sistema** — Asos quota fissa (applicata **sempre**, anche ai residenti), Arim
  e Asos variabili.
- **Imposte** — accisa `0,0227 × kWh netti`, **senza soglia di esenzione** per default.
- **Altre partite** — righe libere, importi anche negativi.

### 6.6 Imponibile e IVA

L'imponibile include le altre partite **soggette a IVA**. Verifica sulla fattura reale:

```
15,31 + 7,65 + 8,29 + 75,94 + 0,39 = 107,58  = imponibile ✓
```

Le altre partite **non soggette** (es. il -11,40 del foglio GENNAIO-FEBBRAIO) si sommano
invece **dopo** l'IVA. L'IVA si calcola come `imponibile × aliquota`, dove l'aliquota viene
presa dal campo `aliquotaIvaApplicata` della bolletta (flag `usaAliquotaIvaBolletta`),
**non** derivata dal tipo cliente: nella fattura reale un cliente "Domestico non Residente"
ha comunque IVA al 10%.

### 6.7 Risparmio

`risparmio_bimestrale = totale_concorrente − totale_gestore`, e
`risparmio_annuale = risparmio_bimestrale × 6`.

### 6.8 Due esempi numerici completi

**Bimestre NOVEMBRE-DICEMBRE** (3 kW, trioraria, 24/37/33 per mese, coeff 10%):

```
Materia energia:  62,430414 €
Trasporto+oneri:  31,460846 €
Imposte:           4,267600 €
Imponibile:       98,158860 €
IVA (10%):         9,815886 €
TOTALE FATTURA:  107,974746 €
```

**Fattura reale FuturEnergy** (mese singolo, 6/4/7 kWh, 3 kW):

```
Materia energia:   15,31 €
Trasporto:          7,65 €
Oneri sistema:      8,29 €
Altre partite:     75,94 €   (incluse nell'imponibile)
Imposte:            0,39 €   (0,0227 × 17)
Imponibile:       107,58 €
IVA 10%:           10,76 €
TOTALE:           118,34 €
```

## 7. API REST

### Bollette da comparare

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/bollette-concorrenti` | lista |
| GET | `/api/bollette-concorrenti/{id}` | dettaglio |
| POST | `/api/bollette-concorrenti` | crea |
| PUT | `/api/bollette-concorrenti/{id}` | aggiorna |
| DELETE | `/api/bollette-concorrenti/{id}` | elimina |
| POST | `/api/bollette-concorrenti/{id}/duplica` | clona |
| GET | `/api/bollette-concorrenti/{id}/quadratura` | verifica somma = totale |

### Parametri gestore

| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/api/parametri-gestore` | lista profili |
| GET | `/api/parametri-gestore/predefinito` | profilo attivo |
| GET | `/api/parametri-gestore/{id}` | dettaglio |
| POST | `/api/parametri-gestore` | crea |
| PUT | `/api/parametri-gestore/{id}` | aggiorna |
| DELETE | `/api/parametri-gestore/{id}` | elimina |
| POST | `/api/parametri-gestore/{id}/predefinito` | imposta predefinito (unico) |
| POST | `/api/parametri-gestore/{id}/duplica` | clona |
| GET | `/api/parametri-gestore/{id}/anteprima` | simula su un consumo campione |

### Confronto

| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/api/confronti` | esegue il confronto |
| POST | `/api/confronti/multiplo` | una bolletta vs N offerte |
| GET | `/api/confronti` | storico |
| GET | `/api/confronti/{id}` | dettaglio |

Esempio richiesta confronto:

```json
POST /api/confronti
{ "bollettaConcorrenteId": 1, "offertaGestoreId": 1, "parametriGestoreId": null }
```

Estratto risposta:

```json
{
  "risparmioBimestrale": 12.34,
  "risparmioAnnuale": 74.04,
  "gestoreImponibile": 100.10, "gestoreIva": 10.01, "gestoreTotale": 110.11,
  "categorie": [
    { "categoria": "MATERIA_ENERGIA", "fatturatoConcorrente": 15.31,
      "calcolatoGestore": 12.20, "differenza": 3.11, "differenzaPercentuale": 20.31 }
  ],
  "mesi": [ { "numeroMese": 1, "kwhNetti": 17, "kwhConPerdite": 19, "righe": [ ... ] } ]
}
```

Il vecchio endpoint `/api/simulazioni` resta funzionante (vecchio motore, immutato).

## 8. Frontend

| Rotta | Pagina | Scopo |
|---|---|---|
| `/` | Home | landing |
| `/bollette` | **BolletteConcorrenti** | inserimento bollette da comparare (5 sezioni + quadratura live) |
| `/confronto` | **Confronto** | confronto bolletta vs offerta, tabella + grafici + dettaglio righe |
| `/offerte` | Offerte | offerte del gestore |
| `/parametri` | **Parametri** | area configurazione gestore |
| `/storico` | Storico | confronti / simulazioni salvate |
| `/simulatore` | Simulatore | vecchio simulatore (legacy) |

Tutte le pagine usano l'istanza axios condivisa `src/api/client.js` (baseURL `/api`) e il
componente `Toast` per gli errori (niente piu' `console.error` silenziosi).

## 9. Area parametri gestore

La pagina **Parametri** gestisce i profili della Sorgente B. Ogni campo mostra label,
placeholder di riferimento e un badge di origine (🏛️ nazionale / 🏢 gestore), organizzati in
sezioni: perdite, trasporto, oneri, imposte, IVA, dispacciamento, corrispettivi commerciali.
Azioni disponibili: Salva, Duplica, Imposta predefinito, Elimina, **Anteprima** (simula su
24/37/33 kWh, 3 kW e mostra imponibile/IVA/totale).

Il seed crea due profili:

- **"Modello Excel"** — replica esatta del foglio (perdite 10% tondo, mercato capacita
  0,008995): serve a far quadrare i casi di verita dei test.
- **"Standard ARERA 2026"** (`predefinito`) — valori correnti, perdite BT 10,40%, mercato
  capacita 0,009001, gestione capacita 0,01293 (dalla fattura reale).

Vincolo: **un solo profilo** puo' essere predefinito (garantito in transazione).

## 10. Come inserire una bolletta da comparare

Guida campo per campo, con riferimento a dove trovare il dato in una fattura reale:

| Campo | Dove trovarlo in fattura |
|---|---|
| Numero / data fattura | intestazione |
| Periodo dal/al | "periodo di riferimento" |
| Fornitore, offerta, codice | intestazione / condizioni economiche |
| POD | "punto di prelievo" (IT001E...) |
| Tipologia cliente | "tipologia di utenza" (es. domestico non residente) |
| Potenza impegnata / disponibile | "dati tecnici della fornitura" |
| **Consumi F1/F2/F3** | tabella "letture e consumi" per fascia (celle evidenziate) |
| Altre partite | dettaglio "altre partite / oneri" |
| Importi fatturati | "quadro sintetico" (materia, trasporto, oneri, imposte, IVA, totale) |
| Aliquota IVA | riga IVA del riepilogo |

Il **widget di quadratura** in fondo alla pagina diventa verde quando la somma delle
componenti fatturate corrisponde all'imponibile e al totale (tolleranza 1 centesimo).

## 11. Avvio

### Docker (consigliato)

```bash
cp docker/.env.example docker/.env   # personalizza le credenziali
./avvia.sh docker
```

Attende l'healthcheck del backend e stampa gli URL. Servizi:

- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Stop: `./ferma.sh` (aggiungi `--volumes` per cancellare anche i dati del DB).

### Locale (sviluppo)

```bash
./avvia.sh local
```

Avvia PostgreSQL in Docker, il backend (Maven) e il frontend Vite (hot reload su
http://localhost:5173). Prerequisiti: Java 17, Node 20+, Maven, Docker.

### Troubleshooting

- **Porta occupata** (80/8080/5432/5173): lo script avvisa; libera la porta o cambia i
  mapping in `docker/docker-compose.yml`.
- **`ddl-auto: validate` fallisce**: significa che lo schema Flyway non coincide con le
  entity; controlla `V1__schema_iniziale.sql`. La migrazione e' verificata su H2 in
  modalita PostgreSQL dai test di integrazione.

## 12. Test

```bash
cd backend && mvn test
```

Copertura:

- `BollettaCalculatorTest` — casi di verita del foglio Excel (NOVEMBRE-DICEMBRE
  107,974746 €, DICEMBRE-GENNAIO 44,276529 €, GENNAIO-FEBBRAIO 59,112642 € con altra
  partita negativa) e della fattura FuturEnergy (accisa 0,39, imponibile 107,58 con altre
  partite, IVA 10,76); piu' unit mirati su perdite, due basi, Asos, accisa, trasporto,
  quadratura. Tolleranza ± 0,01 €.
- `ConfrontoIntegrationTest` — migrazione Flyway + `validate`, seed, unicita del profilo
  predefinito, calcolo del risparmio, anteprima. Gira su H2 in modalita PostgreSQL.

## 13. Conformita al modello Excel

Allineato: struttura bimestrale a due mesi, motore a righe, perdite `ROUND(,0)` sulla
quantita, doppia base kWh netti / con perdite, Asos fissa sempre applicata, accisa senza
soglia, trasporto €/kW mensile, altre partite soggette nell'imponibile, IVA dall'aliquota
della bolletta.

Punti ancora da chiarire (ambiguita del foglio originale):

- **Asos quota fissa** 7,6302 in alcuni mesi e 0 in altri: una volta per bimestre o per
  mese? Nel foglio il primo mese la applica e il secondo no.
- **Corrispettivo mercato capacita** oscilla tra 0,008995 / 0,00093 / 0,01093 tra i fogli:
  variazione 10×, sospetta.
- **Bug nel file Excel**: `F139` corrotta (`F69++F80127`, `K130`), `F138` incoerente tra
  fogli, `F86` punta a `F84` vuota, `F53` azzerata a mano in GENNAIO-FEBBRAIO, foglio `DATI`
  mai referenziato. Il motore Java non li replica: calcola l'imponibile in modo coerente.

Per questi motivi i corrispettivi variabili per mese sono modellati sull'`Offerta`
(`VoceCorrispettivo.numeroMese`), mentre i parametri stabili stanno su `ParametriGestore`.
