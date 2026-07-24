import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import Simulatore from './pages/Simulatore'
import Confronto from './pages/Confronto'
import Offerte from './pages/Offerte'
import Storico from './pages/Storico'

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/simulatore" element={<Simulatore />} />
          <Route path="/confronto" element={<Confronto />} />
          <Route path="/offerte" element={<Offerte />} />
          <Route path="/storico" element={<Storico />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
