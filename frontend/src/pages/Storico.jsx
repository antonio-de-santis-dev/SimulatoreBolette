import { useState, useEffect } from 'react'
import client from '../api/client'
import { Calendar, Trash2 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

const axios = client
const API_URL = ''

// Etichette leggibili per gli enum serializzati come stringa dal backend.
const TIPO_CLIENTE_LABEL = {
  DOMESTICO_RESIDENTE: 'Domestico residente',
  DOMESTICO_NON_RESIDENTE: 'Domestico non residente',
  DOMESTICO_USI_DIVERSI: 'Domestico usi diversi',
  ALTRI_USI_BT: 'Altri usi BT',
  ILLUMINAZIONE_PUBBLICA: 'Illuminazione pubblica',
}
const POTENZA_LABEL = {
  KW_1_5: '1,5 kW', KW_3: '3 kW', KW_4_5: '4,5 kW', KW_6: '6 kW', KW_10: '10 kW',
}
const TARIFFA_LABEL = {
  MONORARIA: 'Monoraria', BIORARIA: 'Bioraria', TRIORARIA: 'Trioraria',
}

// Gestisce sia stringa-enum ("DOMESTICO_RESIDENTE") sia oggetto ({descrizione}).
const leggibile = (x, map) => {
  if (x == null) return null
  if (typeof x === 'object') return x.descrizione || x.nome || null
  return map[x] || x
}

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
          {simulazioni.map(s => {
            const dettaglio = [
              leggibile(s.tipoCliente, TIPO_CLIENTE_LABEL),
              leggibile(s.potenzaContrattuale, POTENZA_LABEL),
              leggibile(s.tipoTariffa, TARIFFA_LABEL),
            ].filter(Boolean).join(' • ')
            const tariffa = typeof s.tipoTariffa === 'object' ? s.tipoTariffa?.nome : s.tipoTariffa
            return (
            <div key={s.id} className="card flex flex-col items-start gap-3 md:flex-row md:items-center md:justify-between hover:shadow-md transition-shadow">
              <div className="flex items-center gap-4 min-w-0 w-full">
                <div className={`w-12 h-12 shrink-0 rounded-full flex items-center justify-center ${
                  tariffa === 'MONORARIA' ? 'bg-energy-blue/10' :
                  tariffa === 'BIORARIA' ? 'bg-energy-green/10' : 'bg-energy-orange/10'
                }`}>
                  <span className="text-lg font-bold text-gray-700">{s.nome?.charAt(0)}</span>
                </div>
                <div className="min-w-0">
                  <h3 className="font-semibold truncate">{s.nome}</h3>
                  <p className="text-sm text-gray-500">{dettaglio}</p>
                  <p className="text-sm text-gray-500">
                    {s.consumoTotaleKwh} kWh • {new Date(s.createdAt).toLocaleDateString('it-IT')}
                  </p>
                </div>
              </div>
              <div className="text-left md:text-right shrink-0">
                <div className="text-2xl font-bold">€{s.totaleBimestrale?.toFixed(2)}</div>
                <div className="text-sm text-gray-500">bimestrale</div>
                <div className="text-sm font-medium">€{s.totaleAnnuale?.toFixed(2)}/anno</div>
              </div>
              <div className="flex gap-2 self-end md:self-auto md:ml-4">
                <button onClick={() => deleteSim(s.id)} className="p-2 text-red-500 hover:bg-red-50 rounded">
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default Storico
