import { useState, useEffect } from 'react'
import axios from 'axios'
import { Plus, Trash2 } from 'lucide-react'

const API_URL = '/api'

function Offerte() {
  const [offerte, setOfferte] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    nomeFornitore: '', nomeOfferta: '', tipoOfferta: 'PREZZO_FISSO',
    tipoTariffa: 'MONORARIA', prezzoFissoF0: '', spreadPunF0: '', pcvAnnuo: ''
  })

  useEffect(() => { loadOfferte() }, [])

  const loadOfferte = () => {
    axios.get(`${API_URL}/offerte`).then(res => setOfferte(res.data))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    axios.post(`${API_URL}/offerte`, {
      ...formData,
      prezzoFissoF0: formData.prezzoFissoF0 || null,
      spreadPunF0: formData.spreadPunF0 || null,
      pcvAnnuo: formData.pcvAnnuo || null
    }).then(() => {
      loadOfferte()
      setShowForm(false)
      setFormData({ nomeFornitore: '', nomeOfferta: '', tipoOfferta: 'PREZZO_FISSO',
        tipoTariffa: 'MONORARIA', prezzoFissoF0: '', spreadPunF0: '', pcvAnnuo: '' })
    })
  }

  const deleteOfferta = (id) => {
    if (confirm('Eliminare questa offerta?')) {
      axios.delete(`${API_URL}/offerte/${id}`).then(loadOfferte)
    }
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Gestione Offerte</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          <Plus className="w-5 h-5 inline mr-2" /> Nuova Offerta
        </button>
      </div>

      {showForm && (
        <div className="card mb-6 bg-gray-50">
          <form onSubmit={handleSubmit} className="grid md:grid-cols-3 gap-4">
            <input placeholder="Fornitore" value={formData.nomeFornitore}
              onChange={e => setFormData({...formData, nomeFornitore: e.target.value})}
              className="input-field" required />
            <input placeholder="Nome offerta" value={formData.nomeOfferta}
              onChange={e => setFormData({...formData, nomeOfferta: e.target.value})}
              className="input-field" required />
            <select value={formData.tipoOfferta}
              onChange={e => setFormData({...formData, tipoOfferta: e.target.value})}
              className="input-field">
              <option value="PREZZO_FISSO">Prezzo fisso</option>
              <option value="INDICIZZATA_PUN">Indicizzata PUN</option>
            </select>
            <select value={formData.tipoTariffa}
              onChange={e => setFormData({...formData, tipoTariffa: e.target.value})}
              className="input-field">
              <option value="MONORARIA">Monoraria</option>
              <option value="BIORARIA">Bioraria</option>
              <option value="TRIORARIA">Trioraria</option>
            </select>
            <input placeholder="Prezzo fisso €/kWh" type="number" step="0.0001"
              value={formData.prezzoFissoF0}
              onChange={e => setFormData({...formData, prezzoFissoF0: e.target.value})}
              className="input-field" />
            <input placeholder="Spread PUN €/kWh" type="number" step="0.0001"
              value={formData.spreadPunF0}
              onChange={e => setFormData({...formData, spreadPunF0: e.target.value})}
              className="input-field" />
            <input placeholder="PCV €/anno" type="number" step="0.01"
              value={formData.pcvAnnuo}
              onChange={e => setFormData({...formData, pcvAnnuo: e.target.value})}
              className="input-field" />
            <div className="md:col-span-3">
              <button type="submit" className="btn-primary">Salva Offerta</button>
            </div>
          </form>
        </div>
      )}

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {offerte.map(o => (
          <div key={o.id} className="card hover:shadow-lg transition-shadow">
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-semibold text-lg">{o.nomeFornitore}</h3>
              <button onClick={() => deleteOfferta(o.id)} className="text-red-500 hover:text-red-700">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            <p className="text-gray-600 text-sm mb-3">{o.nomeOfferta}</p>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Tipo:</span>
                <span className={`font-medium ${
                  o.tipoOfferta === 'PREZZO_FISSO' ? 'text-energy-blue' : 'text-energy-green'
                }`}>{o.tipoOfferta === 'PREZZO_FISSO' ? 'Prezzo fisso' : 'Indicizzata PUN'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Tariffa:</span>
                <span>{o.tipoTariffa}</span>
              </div>
              {o.prezzoFissoF0 && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Prezzo F0:</span>
                  <span className="font-mono">€{o.prezzoFissoF0}/kWh</span>
                </div>
              )}
              {o.spreadPunF0 && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Spread PUN:</span>
                  <span className="font-mono">+€{o.spreadPunF0}/kWh</span>
                </div>
              )}
              {o.pcvAnnuo && (
                <div className="flex justify-between">
                  <span className="text-gray-500">PCV:</span>
                  <span className="font-mono">€{o.pcvAnnuo}/anno</span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Offerte
