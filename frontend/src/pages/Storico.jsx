import { useState, useEffect } from 'react'
import axios from 'axios'
import { Calendar, Trash2 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

const API_URL = 'http://localhost:8080/api'

function Storico() {
  const [simulazioni, setSimulazioni] = useState([])
  const navigate = useNavigate()

  useEffect(() => {
    axios.get(`${API_URL}/simulazioni`).then(res => setSimulazioni(res.data))
  }, [])

  const deleteSim = (id) => {
    if (confirm('Eliminare questa simulazione?')) {
      axios.delete(`${API_URL}/simulazioni/${id}`).then(() => {
        setSimulazioni(simulazioni.filter(s => s.id !== id))
      })
    }
  }

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
        <Calendar className="w-8 h-8 text-energy-purple" />
        Storico Simulazioni
      </h1>
      <p className="text-gray-600 mb-8">Le tue simulazioni salvate</p>

      {simulazioni.length === 0 ? (
        <div className="card text-center py-12 text-gray-500">
          Nessuna simulazione salvata. Vai al <button onClick={() => navigate('/simulatore')} className="text-energy-blue underline">simulatore</button> per crearne una.
        </div>
      ) : (
        <div className="space-y-4">
          {simulazioni.map(s => (
            <div key={s.id} className="card flex items-center justify-between hover:shadow-md transition-shadow">
              <div className="flex items-center gap-4">
                <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                  s.tipoTariffa === 'MONORARIA' ? 'bg-energy-blue/10' :
                  s.tipoTariffa === 'BIORARIA' ? 'bg-energy-green/10' : 'bg-energy-orange/10'
                }`}>
                  <span className="text-lg font-bold text-gray-700">{s.nome?.charAt(0)}</span>
                </div>
                <div>
                  <h3 className="font-semibold">{s.nome}</h3>
                  <p className="text-sm text-gray-500">
                    {s.tipoCliente} • {s.potenzaContrattuale?.descrizione} • {s.tipoTariffa?.descrizione}
                  </p>
                  <p className="text-sm text-gray-500">
                    {s.consumoTotaleKwh} kWh • {new Date(s.createdAt).toLocaleDateString('it-IT')}
                  </p>
                </div>
              </div>
              <div className="text-right">
                <div className="text-2xl font-bold">€{s.totaleBimestrale?.toFixed(2)}</div>
                <div className="text-sm text-gray-500">bimestrale</div>
                <div className="text-sm font-medium">€{s.totaleAnnuale?.toFixed(2)}/anno</div>
              </div>
              <div className="flex gap-2 ml-4">
                <button onClick={() => deleteSim(s.id)} className="p-2 text-red-500 hover:bg-red-50 rounded">
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default Storico
