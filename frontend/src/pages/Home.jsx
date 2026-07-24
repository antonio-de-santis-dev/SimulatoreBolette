import { Link } from 'react-router-dom'
import { Calculator, BarChart3, TrendingDown, Shield } from 'lucide-react'

function Home() {
  return (
    <div className="space-y-12">
      <section className="text-center py-10 sm:py-16">
        <h1 className="text-3xl sm:text-5xl font-bold text-gray-900 mb-4">
          Confronta le tue <span className="text-energy-blue">bollette della luce</span>
        </h1>
        <p className="text-lg sm:text-xl text-gray-600 max-w-2xl mx-auto mb-8">
          Simula e confronta i prezzi delle offerte luce in pochi click.
          Scopri quanto puoi risparmiare con tariffe a prezzo fisso o indicizzate al PUN.
        </p>
        <div className="flex flex-wrap gap-4 justify-center">
          <Link to="/simulatore" className="btn-primary text-lg">
            <Calculator className="w-5 h-5 inline mr-2" />
            Inizia Simulazione
          </Link>
          <Link to="/confronto" className="btn-secondary text-lg">
            <BarChart3 className="w-5 h-5 inline mr-2" />
            Confronta Offerte
          </Link>
        </div>
      </section>

      <section className="grid md:grid-cols-3 gap-8">
        <div className="card text-center">
          <div className="w-16 h-16 bg-energy-blue/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <Calculator className="w-8 h-8 text-energy-blue" />
          </div>
          <h3 className="text-xl font-semibold mb-2">Simulazione Precisa</h3>
          <p className="text-gray-600">
            Calcola la tua bolletta con le componenti ARERA reali: trasporto, oneri, accise e IVA.
          </p>
        </div>

        <div className="card text-center">
          <div className="w-16 h-16 bg-energy-green/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <TrendingDown className="w-8 h-8 text-energy-green" />
          </div>
          <h3 className="text-xl font-semibold mb-2">Trova il Risparmio</h3>
          <p className="text-gray-600">
            Confronta fino a 50 offerte diverse e scopri quale ti fa risparmiare di più.
          </p>
        </div>

        <div className="card text-center">
          <div className="w-16 h-16 bg-energy-orange/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <Shield className="w-8 h-8 text-energy-orange" />
          </div>
          <h3 className="text-xl font-semibold mb-2">Dati Aggiornati</h3>
          <p className="text-gray-600">
            PUN mensile GME e parametri ARERA sempre aggiornati per calcoli affidabili.
          </p>
        </div>
      </section>

      <section className="card bg-gradient-to-r from-energy-blue to-energy-purple text-white">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div>
            <div className="text-4xl font-bold">€255</div>
            <div className="text-blue-100">Risparmio medio/anno</div>
          </div>
          <div>
            <div className="text-4xl font-bold">32+</div>
            <div className="text-blue-100">Offerte confrontate</div>
          </div>
          <div>
            <div className="text-4xl font-bold">5</div>
            <div className="text-blue-100">Fornitori partner</div>
          </div>
          <div>
            <div className="text-4xl font-bold">100%</div>
            <div className="text-blue-100">Gratuito</div>
          </div>
        </div>
      </section>
    </div>
  )
}

export default Home
