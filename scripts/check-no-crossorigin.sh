#!/usr/bin/env bash
# Impedisce la reintroduzione di @CrossOrigin sui controller: attiva il processing
# CORS anche same-origin e causa 403 in produzione. Usare la CorsConfig centralizzata.
set -euo pipefail

# Match solo l'annotazione reale (righe non di commento), non le menzioni nei commenti.
if grep -rnE '^[^*/]*@CrossOrigin' backend/src/main/java/; then
  echo "ERRORE: @CrossOrigin trovata. Usa la CorsConfig centralizzata (app.cors.allowed-origins)."
  exit 1
fi

echo "OK: nessuna @CrossOrigin nei controller."
