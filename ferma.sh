#!/bin/bash

echo "=========================================="
echo "  Simulatore Bollette Luce - Stop"
echo "=========================================="

echo ""
echo "Arresto servizi..."

# Ferma processi
pkill -f 'java -jar.*simulatore-bollette-luce' 2>/dev/null
pkill -f 'vite' 2>/dev/null

# Ferma Docker
docker stop simulatore-postgres 2>/dev/null

echo ""
echo "Tutti i servizi sono stati arrestati."
echo ""
