import { useState } from 'react'
import { useForm } from 'react-hook-form'
import client from '../api/client'
import { 
  Zap, Home, Battery, Euro, PieChart, TrendingUp, 
  ChevronDown, ChevronUp 
} from 'lucide-react'
import { 
  PieChart as RePieChart, Pie, Cell, ResponsiveContainer, 
  Tooltip as ReTooltip
} from 'recharts'

const axios = client
const API_URL = ''

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

const tipoClienteOptions = [
  { value: 'DOMESTICO_RESIDENTE', label: 'Domestico residente (casa principale)' },
  { value: 'DOMESTICO_NON_RESIDENTE', label: 'Domestico non residente (seconda casa)' },
  { value: 'DOMESTICO_USI_DIVERSI', label: 'Domestico usi diversi' },
  { value: 'ALTRI_USI_BT', label: 'Altri usi in bassa tensione' },
]

const potenzaOptions = [
  { value: 'KW_1_5', label: '1,5 kW' },
  { value: 'KW_3', label: '3 kW (standard)' },
  { value: 'KW_4_5', label: '4,5 kW' },
  { value: 'KW_6', label: '6 kW' },
  { value: 'KW_10', label: '10 kW' },
]

const tariffaOptions = [
  { value: 'MONORARIA', label: 'Monoraria (F0) - Unico prezzo' },
  { value: 'BIORARIA', label: 'Bioraria (F1/F23) - Giorno/Notte' },
  { value: 'TRIORARIA', label: 'Trioraria (F1/F2/F3) - 3 fasce' },
]

function Simulatore() {
  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    defaultValues: {
      nome: 'Simulazione ' + new Date().toLocaleDateString('it-IT'),
      tipoCliente: 'DOMESTICO_RESIDENTE',
      potenzaContrattuale: 'KW_3',
      tipoTariffa: 'MONORARIA',
      consumoTotaleKwh: '450',
      confrontaOfferte: true
    }
  })

  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [showDetails, setShowDetails] = useState(false)
  const [activeTab, setActiveTab] = useState('riepilogo')

  const tipoTariffa = watch('tipoTariffa')

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const request = {
        ...data,
        consumoTotaleKwh: parseFloat(data.consumoTotaleKwh),
        consumoF1: data.consumoF1 ? parseFloat(data.consumoF1) : null,
        consumoF2: data.consumoF2 ? parseFloat(data.consumoF2) : null,
        consumoF3: data.consumoF3 ? parseFloat(data.consumoF3) : null,
        confrontaOfferte: data.confrontaOfferte
      }

      const response = await axios.post(`${API_URL}/simulazioni`, request)
      setResult(response.data)
      setActiveTab('riepilogo')
    } catch (error) {
      console.error('Errore:', error)
      alert('Errore nel calcolo: ' + (error.response?.data?.message || error.message))
    } finally {
      setLoading(false)
    }
  }

  const pieData = result ? [
    { name: 'Materia Energia', value: result.spesaMateriaEnergia },
    { name: 'Trasporto', value: result.spesaTrasporto },
    { name: 'Oneri Sistema', value: result.spesaOneriSistema },
    { name: 'Accise', value: result.spesaAccise },
    { name: 'IVA', value: result.spesaIva },
  ] : []

  return (
    <div className="max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold mb-2 flex items-center gap-3">
        <Zap className="w-8 h-8 text-energy-blue" />
        Simulatore Bolletta Luce
      </h1>
      <p className="text-gray-600 mb-8">Inserisci i tuoi dati per calcolare la spesa stimata</p>

      <div className="grid lg:grid-cols-2 gap-8">
        <div className="card">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">

            <div>
              <label className="label">Nome simulazione</label>
              <input {...register('nome', { required: true })} className="input-field" />
              {errors.nome && <span className="text-red-500 text-sm">Obbligatorio</span>}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="label flex items-center gap-2">
                  <Home className="w-4 h-4" /> Tipo cliente
                </label>
                <select {...register('tipoCliente')} className="input-field">
                  {tipoClienteOptions.map(o => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label flex items-center gap-2">
                  <Battery className="w-4 h-4" /> Potenza
                </label>
                <select {...register('potenzaContrattuale')} className="input-field">
                  {potenzaOptions.map(o => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="label">Tipo tariffa</label>
              <select {...register('tipoTariffa')} className="input-field">
                {tariffaOptions.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="label flex items-center gap-2">
                <TrendingUp className="w-4 h-4" /> 
                Consumo totale bimestrale (kWh)
              </label>
              <input 
                type="number" 
                step="0.01"
                {...register('consumoTotaleKwh', { required: true, min: 0 })} 
                className="input-field" 
              />
              <p className="text-sm text-gray-500 mt-1">
                Consumo tipico: 400-500 kWh/bimestre per 3 kW
              </p>
            </div>

            {tipoTariffa !== 'MONORARIA' && (
              <div className="bg-gray-50 p-4 rounded-lg space-y-3">
                <p className="text-sm font-medium text-gray-700">
                  Consumi per fascia (opzionale - se vuoto calcolati automaticamente)
                </p>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="label text-xs">F1 (ore di punta)</label>
                    <input type="number" step="0.01" {...register('consumoF1')} className="input-field" />
                  </div>
                  <div>
                    <label className="label text-xs">F2 (ore intermedie)</label>
                    <input type="number" step="0.01" {...register('consumoF2')} className="input-field" />
                  </div>
                  <div>
                    <label className="label text-xs">F3 (ore notturne)</label>
                    <input type="number" step="0.01" {...register('consumoF3')} className="input-field" />
                  </div>
                </div>
              </div>
            )}

            <div className="flex items-center gap-2">
              <input 
                type="checkbox" 
                id="confronta" 
                {...register('confrontaOfferte')} 
                className="w-4 h-4"
              />
              <label htmlFor="confronta" className="text-sm">
                Confronta con tutte le offerte disponibili
              </label>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="btn-primary w-full text-lg"
            >
              {loading ? 'Calcolo in corso...' : (
                <>Calcola Bolletta <Euro className="w-5 h-5 inline ml-2" /></>
              )}
            </button>
          </form>
        </div>

        <div>
          {result ? (
            <div className="space-y-6">
              <div className="card bg-gradient-to-br from-energy-blue to-blue-700 text-white">
                <h3 className="text-lg font-semibold mb-4">Risultato Simulazione</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-3xl font-bold">
                      €{result.totaleBimestrale?.toFixed(2)}
                    </div>
                    <div className="text-blue-100">Totale bimestrale</div>
                  </div>
                  <div>
                    <div className="text-3xl font-bold">
                      €{result.totaleAnnuale?.toFixed(2)}
                    </div>
                    <div className="text-blue-100">Totale annuale stimato</div>
                  </div>
                </div>
                <div className="mt-4 pt-4 border-t border-white/20">
                  <div className="flex justify-between">
                    <span>Prezzo medio kWh:</span>
                    <span className="font-bold">€{result.prezzoMedioKwh?.toFixed(4)}</span>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="flex gap-2 mb-4 border-b">
                  {['riepilogo', 'grafico', 'confronto'].map(tab => (
                    <button
                      key={tab}
                      onClick={() => setActiveTab(tab)}
                      className={`px-4 py-2 font-medium capitalize ${
                        activeTab === tab 
                          ? 'text-energy-blue border-b-2 border-energy-blue' 
                          : 'text-gray-500'
                      }`}
                    >
                      {tab}
                    </button>
                  ))}
                </div>

                {activeTab === 'riepilogo' && (
                  <div className="space-y-3">
                    <h4 className="font-semibold flex items-center gap-2">
                      <PieChart className="w-5 h-5" /> Dettaglio voci di spesa
                    </h4>
                    {[
                      { label: 'Materia Energia', value: result.spesaMateriaEnergia, pct: result.pctMateriaEnergia, color: 'bg-energy-blue' },
                      { label: 'Trasporto e Contatore', value: result.spesaTrasporto, pct: result.pctTrasporto, color: 'bg-energy-green' },
                      { label: 'Oneri di Sistema', value: result.spesaOneriSistema, pct: result.pctOneriSistema, color: 'bg-energy-orange' },
                      { label: 'Accise', value: result.spesaAccise, pct: result.pctAccise, color: 'bg-energy-red' },
                      { label: 'IVA', value: result.spesaIva, pct: result.pctIva, color: 'bg-energy-purple' },
                    ].map(item => (
                      <div key={item.label} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-center gap-3">
                          <div className={`w-3 h-3 rounded-full ${item.color}`} />
                          <span>{item.label}</span>
                        </div>
                        <div className="text-right">
                          <div className="font-semibold">€{item.value?.toFixed(2)}</div>
                          <div className="text-sm text-gray-500">{item.pct?.toFixed(1)}%</div>
                        </div>
                      </div>
                    ))}

                    <button 
                      onClick={() => setShowDetails(!showDetails)}
                      className="flex items-center gap-2 text-energy-blue text-sm mt-2"
                    >
                      {showDetails ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      {showDetails ? 'Nascondi' : 'Mostra'} dettaglio consumi
                    </button>

                    {showDetails && (
                      <div className="bg-gray-50 p-4 rounded-lg text-sm space-y-2">
                        <div className="flex justify-between">
                          <span>Consumo totale:</span>
                          <span className="font-medium">{result.consumoTotaleKwh} kWh</span>
                        </div>
                        {result.consumoF1 > 0 && (
                          <div className="flex justify-between">
                            <span>F1 (punta):</span>
                            <span className="font-medium">{result.consumoF1?.toFixed(2)} kWh</span>
                          </div>
                        )}
                        {result.consumoF2 > 0 && (
                          <div className="flex justify-between">
                            <span>F2 (intermedia):</span>
                            <span className="font-medium">{result.consumoF2?.toFixed(2)} kWh</span>
                          </div>
                        )}
                        {result.consumoF3 > 0 && (
                          <div className="flex justify-between">
                            <span>F3 (notte):</span>
                            <span className="font-medium">{result.consumoF3?.toFixed(2)} kWh</span>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}

                {activeTab === 'grafico' && (
                  <div className="h-80">
                    <ResponsiveContainer width="100%" height="100%">
                      <RePieChart>
                        <Pie
                          data={pieData}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={100}
                          paddingAngle={5}
                          dataKey="value"
                          label={({name, percent}) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        >
                          {pieData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <ReTooltip formatter={(value) => `€${value.toFixed(2)}`} />
                      </RePieChart>
                    </ResponsiveContainer>
                  </div>
                )}

                {activeTab === 'confronto' && result.confrontoOfferte && (
                  <div className="space-y-3 max-h-96 overflow-y-auto">
                    <h4 className="font-semibold">Classifica offerte (per risparmio annuale)</h4>
                    {result.confrontoOfferte?.map((offerta) => (
                      <div 
                        key={offerta.offertaId}
                        className={`p-4 rounded-lg border-2 ${
                          offerta.miglioreOfferta 
                            ? 'border-energy-green bg-green-50' 
                            : 'border-gray-200'
                        }`}
                      >
                        <div className="flex justify-between items-start">
                          <div>
                            <div className="font-semibold">{offerta.nomeFornitore}</div>
                            <div className="text-sm text-gray-600">{offerta.nomeOfferta}</div>
                            <div className="text-xs text-gray-500">{offerta.tipoOfferta} - {offerta.tipoTariffa}</div>
                          </div>
                          <div className="text-right">
                            <div className="text-xl font-bold">€{offerta.totaleAnnuale?.toFixed(2)}/anno</div>
                            {offerta.risparmioAnnualeVsMedia > 0 && (
                              <div className="text-energy-green text-sm font-medium">
                                Risparmio: €{offerta.risparmioAnnualeVsMedia?.toFixed(2)}/anno
                              </div>
                            )}
                          </div>
                        </div>
                        {offerta.miglioreOfferta && (
                          <div className="mt-2 text-energy-green text-sm font-medium flex items-center gap-1">
                            <TrendingUp className="w-4 h-4" /> Migliore offerta!
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="card h-full flex items-center justify-center text-gray-400">
              <div className="text-center">
                <PieChart className="w-16 h-16 mx-auto mb-4" />
                <p>Inserisci i dati e clicca "Calcola Bolletta"</p>
                <p className="text-sm mt-2">per vedere il risultato qui</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Simulatore
