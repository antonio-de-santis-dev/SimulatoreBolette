import { useEffect, useState } from 'react'
import client from '../api/client'
import { useToast } from '../components/Toast'
import { Star, Copy, Trash2, Eye, Save } from 'lucide-react'

// Definizione dei campi per sezione, con badge origine (nazionale/gestore)
const N = '🏛️', G = '🏢'
const SEZIONI = [
  { t: 'Perdite di rete', campi: [
    { k: 'percentualePerdite', l: 'Percentuale perdite', o: N, ph: '0.1040' },
    { k: 'arrotondaPerdite', l: 'Arrotonda perdite (Excel)', o: N, tipo: 'bool' },
  ] },
  { t: 'Trasporto', campi: [
    { k: 'trasportoKwMese', l: 'EUR/kW/mese', o: N, ph: '1.866567' },
    { k: 'trasportoPodMese', l: 'EUR/pod/mese', o: N, ph: '1.84' },
    { k: 'trasportoKwh', l: 'EUR/kWh scaglione 1', o: N, ph: '0.0122' },
  ] },
  { t: 'Oneri di sistema', campi: [
    { k: 'asosQuotaFissa', l: 'Asos quota fissa', o: N, ph: '7.6302' },
    { k: 'asosQuotaVariabile', l: 'Asos quota variabile', o: N, ph: '0.029809' },
    { k: 'arimQuotaVariabile', l: 'Arim quota variabile', o: N, ph: '0.008828' },
  ] },
  { t: 'Imposte', campi: [
    { k: 'accisaDomestico', l: 'Accisa domestico', o: N, ph: '0.0227' },
    { k: 'applicaSogliaEsenzione', l: 'Applica soglia esenzione', o: N, tipo: 'bool' },
    { k: 'sogliaEsenzioneKwhAnno', l: 'Soglia esenzione kWh/anno', o: N, ph: '1800' },
    { k: 'sogliaMassimaKwhAnno', l: 'Soglia massima kWh/anno', o: N, ph: '2640' },
  ] },
  { t: 'IVA', campi: [
    { k: 'ivaDomestico', l: 'IVA domestico', o: N, ph: '0.10' },
    { k: 'ivaNonDomestico', l: 'IVA non domestico', o: N, ph: '0.22' },
    { k: 'usaAliquotaIvaBolletta', l: 'Usa aliquota della bolletta', o: N, tipo: 'bool' },
    { k: 'altrePartiteInImponibile', l: 'Altre partite nell\'imponibile', o: N, tipo: 'bool' },
  ] },
  { t: 'Corrispettivi di dispacciamento (base kWh con perdite)', campi: [
    { k: 'corrMercatoCapacita', l: 'Mercato capacità', o: N, ph: '0.009001' },
    { k: 'corrDisRtn', l: 'DIS / RTN', o: N, ph: '0.000558' },
    { k: 'corrInt', l: 'INT', o: N, ph: '0.000856' },
    { k: 'corrMsd', l: 'MSD', o: N, ph: '0.001953' },
    { k: 'corrUesSicurezza', l: 'UES / Sicurezza', o: N, ph: '0.002048' },
    { k: 'corrSal', l: 'SAL', o: N, ph: '0.00052' },
    { k: 'corrSbilanciamento', l: 'Sbilanciamento', o: G, ph: '0.01' },
    { k: 'corrAggregazioneMisure', l: 'Aggregazione misure', o: N, ph: '0.007' },
    { k: 'dispbt', l: 'DISPBT', o: N, ph: '0.109858' },
    { k: 'corrGestioneCapacita', l: 'Gestione capacità', o: N, ph: '0.01293' },
  ] },
  { t: 'Corrispettivi commerciali del gestore', campi: [
    { k: 'commercializzazioneMese', l: 'Commercializzazione/mese', o: G, ph: '8.95' },
    { k: 'pcvVariabile', l: 'PCV variabile', o: G, ph: '0.005' },
    { k: 'spreadEnergia', l: 'Spread energia', o: G, ph: '0.01' },
  ] },
]

function Parametri() {
  const toast = useToast()
  const [lista, setLista] = useState([])
  const [sel, setSel] = useState(null)
  const [anteprima, setAnteprima] = useState(null)

  const carica = () => client.get('/parametri-gestore').then((r) => setLista(r.data)).catch(toast.error)
  useEffect(() => { carica() }, [])

  const set = (k, v) => setSel((p) => ({ ...p, [k]: v }))

  const salva = () => {
    const req = sel.id ? client.put(`/parametri-gestore/${sel.id}`, sel) : client.post('/parametri-gestore', sel)
    req.then(() => { toast.success('Profilo salvato'); carica() }).catch(toast.error)
  }
  const setPredefinito = (id) => client.post(`/parametri-gestore/${id}/predefinito`)
    .then(() => { toast.success('Profilo predefinito aggiornato'); carica() }).catch(toast.error)
  const duplica = (id) => client.post(`/parametri-gestore/${id}/duplica`)
    .then((r) => { toast.success('Profilo duplicato'); carica(); setSel(r.data) }).catch(toast.error)
  const elimina = (id) => client.delete(`/parametri-gestore/${id}`)
    .then(() => { toast.info('Profilo eliminato'); setSel(null); carica() }).catch(toast.error)
  const vediAnteprima = (id) => client.get(`/parametri-gestore/${id}/anteprima`)
    .then((r) => setAnteprima(r.data)).catch(toast.error)

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Parametri del gestore</h1>
        <p className="text-gray-600">Sorgente B — configurazione: {N} parametri nazionali ARERA, {G} corrispettivi del gestore.</p>
      </div>

      <section className="bg-white rounded-xl shadow-sm border p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="font-semibold text-gray-800">Profili</h2>
          <button onClick={() => setSel({ nomeProfilo: 'Nuovo profilo', arrotondaPerdite: true, usaAliquotaIvaBolletta: true, altrePartiteInImponibile: true, applicaSogliaEsenzione: false, livelloTensioneDefault: 'BT' })}
            className="btn-sec">Nuovo profilo</button>
        </div>
        <ul className="divide-y">
          {lista.map((p) => (
            <li key={p.id} className="flex items-center justify-between py-2">
              <button onClick={() => setSel(p)} className="text-left flex items-center gap-2">
                {p.predefinito && <Star className="w-4 h-4 text-yellow-500 fill-yellow-400" />}
                <b>{p.nomeProfilo}</b> <span className="text-gray-400 text-sm">{p.descrizione}</span>
              </button>
              <span className="flex gap-2 text-gray-500">
                <button title="Anteprima" onClick={() => vediAnteprima(p.id)}><Eye className="w-4 h-4" /></button>
                <button title="Duplica" onClick={() => duplica(p.id)}><Copy className="w-4 h-4" /></button>
                {!p.predefinito && <button title="Predefinito" onClick={() => setPredefinito(p.id)}><Star className="w-4 h-4" /></button>}
                <button title="Elimina" onClick={() => elimina(p.id)} className="text-red-500"><Trash2 className="w-4 h-4" /></button>
              </span>
            </li>
          ))}
        </ul>
      </section>

      {anteprima && (
        <section className="bg-blue-50 border border-blue-200 rounded-xl p-5">
          <h2 className="font-semibold mb-2">Anteprima su consumo campione (24/37/33 kWh, 3 kW)</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
            <Info l="Imponibile" v={anteprima.imponibile} />
            <Info l="IVA" v={anteprima.iva} />
            <Info l="Totale" v={anteprima.totale} />
          </div>
        </section>
      )}

      {sel && (
        <section className="bg-white rounded-xl shadow-sm border p-5 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-800">Modifica profilo</h2>
            <button onClick={salva} className="btn-pri flex items-center gap-1"><Save className="w-4 h-4" /> Salva</button>
          </div>
          <div className="grid md:grid-cols-3 gap-3">
            <label className="text-sm block"><span className="text-gray-500">Nome profilo</span>
              <input className="input" value={sel.nomeProfilo || ''} onChange={(e) => set('nomeProfilo', e.target.value)} /></label>
            <label className="text-sm block"><span className="text-gray-500">Descrizione</span>
              <input className="input" value={sel.descrizione || ''} onChange={(e) => set('descrizione', e.target.value)} /></label>
            <label className="text-sm block"><span className="text-gray-500">Gestore</span>
              <input className="input" value={sel.nomeGestore || ''} onChange={(e) => set('nomeGestore', e.target.value)} /></label>
          </div>

          {SEZIONI.map((s) => (
            <details key={s.t} className="border rounded-lg" open>
              <summary className="cursor-pointer px-4 py-2 font-medium text-gray-700 bg-gray-50">{s.t}</summary>
              <div className="grid md:grid-cols-3 gap-3 p-4">
                {s.campi.map((c) => (
                  <div key={c.k}>
                    {c.tipo === 'bool' ? (
                      <label className="text-sm flex items-center gap-2 mt-5">
                        <input type="checkbox" checked={!!sel[c.k]} onChange={(e) => set(c.k, e.target.checked)} />
                        <span>{c.o} {c.l}</span>
                      </label>
                    ) : (
                      <label className="text-sm block">
                        <span className="text-gray-500">{c.o} {c.l}</span>
                        <input className="input" type="number" step="0.000001" placeholder={c.ph}
                          value={sel[c.k] ?? ''} onChange={(e) => set(c.k, e.target.value)} />
                      </label>
                    )}
                  </div>
                ))}
              </div>
            </details>
          ))}
        </section>
      )}
    </div>
  )
}

const Info = ({ l, v }) => (
  <div className="bg-white rounded-lg p-3 border">
    <div className="text-gray-500 text-xs">{l}</div>
    <div className="font-bold">{v != null ? Number(v).toFixed(2) : '-'} €</div>
  </div>
)

export default Parametri
