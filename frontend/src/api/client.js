import axios from 'axios'

// Istanza axios unica: usa il proxy Vite (/api -> backend) in sviluppo e lo stesso
// path relativo in produzione (dove il JAR serve anche il frontend). Path RELATIVO:
// e' fondamentale perche' il browser dell'utente chiami il server, non il proprio PC.
const client = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// Log degli errori senza interrompere il flusso (i Toast gestiscono la UI).
client.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Errore API:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export default client
