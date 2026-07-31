import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import { ToastProvider } from './components/Toast'
import Home from './pages/Home'
import BolletteConcorrenti from './pages/BolletteConcorrenti'
import Confronto from './pages/Confronto'
import Gestore from './pages/Gestore'
import Storico from './pages/Storico'
import Simulatore from './pages/Simulatore'

function App() {
  return (
    <ToastProvider>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="container mx-auto px-4 pt-8 pb-24 md:pb-8">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/bollette" element={<BolletteConcorrenti />} />
            <Route path="/confronto" element={<Confronto />} />
            <Route path="/gestore" element={<Gestore />} />
            {/* Aree unificate: le vecchie rotte reindirizzano a "Il mio gestore" */}
            <Route path="/offerte" element={<Navigate to="/gestore" replace />} />
            <Route path="/parametri" element={<Navigate to="/gestore" replace />} />
            <Route path="/storico" element={<Storico />} />
            <Route path="/simulatore" element={<Simulatore />} />
          </Routes>
        </main>
      </div>
    </ToastProvider>
  )
}

export default App
