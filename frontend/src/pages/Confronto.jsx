import { useState, useEffect } from 'react'
import axios from 'axios'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { Trophy } from 'lucide-react'

const API_URL = 'http://localhost:8080/api'

function Confronto() {
  const [offerte, setOfferte] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    axios.get(`${API_URL}/offerte/attive`)
      .then(res => setOfferte(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false))
  }, [])

  const chartData = offerte.map(o => ({
    name: o.nomeFornitore,
    prezzoF0: o.prezzoFissoF0 ? parseFloat(o.prezzoFissoF0) * 100 : null,
  }))

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
        <Trophy className="w-8 h-8 text-energy-orange" />
        Confronto Offerte
      </h1>
      <p className="text-gray-600 mb-8">Confronta i prezzi delle offerte luce disponibili</p>

      {loading ? (
        <div className="text-center py-12">Caricamento...</div>
      ) : (
        <div className="space-y-8">
          <div className="card overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left">Fornitore</th>
                  <th className="px-4 py-3 text-left">Offerta</th>
                  <th className="px-4 py-3 text-left">Tipo</th>
                  <th className="px-4 py-3 text-left">Tariffa</th>
                  <th className="px-4 py-3 text-right">Prezzo F0</th>
                  <th className="px-4 py-3 text-right">Spread PUN</th>
                  <th className="px-4 py-3 text-right">PCV/anno</th>
                </tr>
              </thead>
              <tbody>
                {offerte.map(o => (
                  <tr key={o.id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{o.nomeFornitore}</td>
                    <td className="px-4 py-3">{o.nomeOfferta}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded text-xs ${
                        o.tipoOfferta === 'PREZZO_FISSO' 
                          ? 'bg-energy-blue/10 text-energy-blue' 
                          : 'bg-energy-green/10 text-energy-green'
                      }`}>
                        {o.tipoOfferta === 'PREZZO_FISSO' ? 'Fisso' : 'PUN'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm">{o.tipoTariffa}</td>
                    <td className="px-4 py-3 text-right font-mono">
                      {o.prezzoFissoF0 ? `€${o.prezzoFissoF0}` : '-'}
                    </td>
                    <td className="px-4 py-3 text-right font-mono">
                      {o.spreadPunF0 ? `+€${o.spreadPunF0}` : '-'}
                    </td>
                    <td className="px-4 py-3 text-right font-mono">
                      {o.pcvAnnuo ? `€${o.pcvAnnuo}` : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {chartData.some(d => d.prezzoF0) && (
            <div className="card">
              <h3 className="text-lg font-semibold mb-4">Confronto prezzi (cent/kWh)</h3>
              <div className="h-80">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData.filter(d => d.prezzoF0)}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip formatter={(value) => `${value.toFixed(2)} c€/kWh`} />
                    <Legend />
                    <Bar dataKey="prezzoF0" name="Prezzo fisso (c€/kWh)" fill="#3b82f6" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default Confronto
