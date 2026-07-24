import axios from 'axios'

// Istanza axios unica: usa il proxy Vite (/api -> backend) in sviluppo e lo stesso
// path relativo dietro nginx in produzione. Sostituisce i vecchi
// `const API_URL = 'http://localhost:8080/api'` sparsi nelle pagine.
const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export default client
