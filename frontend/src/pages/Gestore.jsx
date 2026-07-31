import { useEffect, useState } from 'react'
import client from '../api/client'
import { useToast } from '../components/Toast'
import { Plus, Trash2, Star, Copy, Eye, Save, List, SlidersHorizontal } from 'lucide-react'

/**
 * Area unificata "Il mio gestore": raccoglie in un'unica pagina
 *  - le OFFERTE commerciali del gestore (ex pagina Offerte)
 *  - la CONFIGURAZIONE del gestore / profili parametri (ex pagina Parametri)
 *
 * Il Confronto (comparatore 2 bollette) e l'inserimento Bollette restano
 * pagine separate e invariati. I dati nazionali ARERA (badge 🏛️) restano
 * dentro il profilo per ora: la loro estrazione e' prevista in un intervento
 * successivo.
 */
function Gestore() {
  const [tab, setTab] = useState('offerte')

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl md:text-3xl font-bold text-gray-900">Il mio gestore</h1>
        <p className="text-gray-600">
          Le tue offerte e la configurazione del gestore in un unico posto.
        </p>
      </div>

      {/* Selettore vista */}
      <div className="inline-flex rounded-lg border bg-white p-1 shadow-sm">
        <button
          onClick={() => setTab('offerte')}
          className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
            tab === 'offerte' ? 'bg-energy-blue text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          <List className="w-4 h-4" /> Offerte
        </button>
        <button
          onClick={() => setTab('config')}
          className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors ${
            tab === 'config' ? 'bg-energy-blue text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          <SlidersHorizontal className="w-4 h-4" /> Configurazione
        </button>
      </div>

      {tab === 'offerte' ? <SezioneOfferte /> : <SezioneConfigurazione />}
    </div>
  )
}

/* ═══════════════════════ OFFERTE ═══════════════════════ */

function SezioneOfferte() {
  const toast = useToast()
  const [offerte, setOfferte] = useState([])
  const [showForm, setShowForm] = useState(false)
  const vuota = {
    nomeFornitore: '', nomeOfferta: '', tipoOfferta: 'PREZZO_FISSO',
    tipoTariffa: 'MONORARIA', prezzoFissoF0: '', spreadPunF0: '', pcvAnnuo: '',
  }
  const [formData, setFormData] = useState(vuota)

  const load = () => client.get('/offerte').then((r) => setOfferte(r.data)).catch(toast.error)
  useEffect(() => { load() }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    client.post('/offerte', {
      ...formData,
      prezzoFissoF0: formData.prezzoFissoF0 || null,
      spreadPunF0: formData.spreadPunF0 || null,
      pcvAnnuo: formData.pcvAnnuo || null,
    }).then(() => {
      toast.success('Offerta salvata')
      load()
      setShowForm(false)
      setFormData(vuota)
    }).catch(toast.error)
  }

  const deleteOfferta = (id) => {
    if (!confirm('Eliminare questa offerta?')) return
    client.delete(`/offerte/${id}`).then(() => { toast.info('Offerta eliminata'); load() }).catch(toast.error)
  }

  return (
    <div>
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-800">Offerte commerciali</h2>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary w-full md:w-auto">
          <Plus className="w-5 h-5 inline mr-2" /> Nuova offerta
        </button>
      </div>

      {showForm && (
        <div className="card mb-6 bg-gray-50">
          <form onSubmit={handleSubmit} className="grid md:grid-cols-3 gap-4">
            <input placeholder="Fornitore" value={formData.nomeFornitore}
              onChange={(e) => setFormData({ ...formData, nomeFornitore: e.target.value })}
              className="input-field" required />
            <input placeholder="Nome offerta" value={formData.nomeOfferta}
              onChange={(e) => setFormData({ ...formData, nomeOfferta: e.target.value })}
              className="input-field" required />
            <select value={formData.tipoOfferta}
              onChange={(e) => setFormData({ ...formData, tipoOfferta: e.target.value })}
              className="input-field">
              <option value="PREZZO_FISSO">Prezzo fisso</option>
              <option value="INDICIZZATA_PUN">Indicizzata PUN</option>
            </select>
            <select value={formData.tipoTariffa}
              onChange={(e) => setFormData({ ...formData, tipoTariffa: e.target.value })}
              className="input-field">
              <option value="MONORARIA">Monoraria</option>
              <option value="BIORARIA">Bioraria</option>
              <option value="TRIORARIA">Trioraria</option>
            </select>
            <input placeholder="Prezzo fisso €/kWh" type="number" step="0.0001"
              value={formData.prezzoFissoF0}
              onChange={(e) => setFormData({ ...formData, prezzoFissoF0: e.target.value })}
              className="input-field" />
            <input placeholder="Spread PUN €/kWh" type="number" step="0.0001"
              value={formData.spreadPunF0}
              onChange={(e) => setFormData({ ...formData, spreadPunF0: e.target.value })}
              className="input-field" />
            <input placeholder="PCV €/anno" type="number" step="0.01"
              value={formData.pcvAnnuo}
              onChange={(e) => setFormData({ ...formData, pcvAnnuo: e.target.value })}
              className="input-field" />
            <div className="md:col-span-3">
              <button type="submit" className="btn-primary">Salva offerta</button>
            </div>
          </form>
        </div>
      )}

      {offerte.length === 0 ? (
        <div className="card text-center text-gray-500">Nessuna offerta inserita.</div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {offerte.map((o) => (
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
                {o.prezzoFissoF0 != null && Number(o.prezzoFissoF0) > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Prezzo F0:</span>
                    <span className="font-mono">€{o.prezzoFissoF0}/kWh</span>
                  </div>
                )}
                {o.spreadPunF0 != null && Number(o.spreadPunF0) > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Spread PUN:</span>
                    <span className="font-mono">+€{o.spreadPunF0}/kWh</span>
                  </div>
                )}
                {o.pcvAnnuo != null && Number(o.pcvAnnuo) > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">PCV:</span>
                    <span className="font-mono">€{o.pcvAnnuo}/anno</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

/* ═══════════════════════ CONFIGURAZIONE (profili parametri) ═══════════════════════ */

const N = '🏛️', G = '🏢'
const SEZIONI = [
  { t: 'Flag di conformità (normativa vs Excel)', campi: [
    { k: 'applicaEsenzioneAccisaResidenti', l: 'Esenzione accisa residenti ≤3 kW', o: N, tipo: 'bool' },
    { k: 'applicaScaglioni', l: 'Applica scaglioni di consumo', o: N, tipo: 'bool' },
    { k: 'arrotondaPerdite', l: 'Arrotonda perdite (compat. Excel)', o: N, tipo: 'bool' },
    { k: 'quotaFissaSoloNonResidenti', l: 'Quota fissa oneri solo non residenti', o: N, tipo: 'bool' },
    { k: 'quotaPotenzaSoloNonDomestici', l: 'Quota potenza solo non domestici', o: N, tipo: 'bool' },
    { k: 'altrePartiteInImponibile', l: 'Altre partite nell\'imponibile', o: N, tipo: 'bool' },
    { k: 'supportaAliquoteMiste', l: 'Supporta aliquote IVA miste', o: N, tipo: 'bool' },
    { k: 'usaAliquotaIvaBolletta', l: 'Usa aliquota IVA della bolletta', o: N, tipo: 'bool' },
  ] },
  { t: 'Perdite di rete', campi: [
    { k: 'percentualePerdite', l: 'Percentuale perdite (BT 0,1040)', o: N, ph: '0.1040' },
  ] },
  { t: 'Trasporto (valori ANNUI, /12 nel calcolo)', campi: [
    { k: 'trasportoQuotaFissaAnnua', l: 'Quota fissa annua', o: N, ph: '22.08' },
    { k: 'trasportoQuotaPotenzaAnnua', l: 'Quota potenza annua', o: N, ph: '22.398804' },
  ] },
  { t: 'Oneri di sistema (quote fisse ANNUE)', campi: [
    { k: 'asosQuotaFissaAnnua', l: 'ASOS quota fissa annua', o: N, ph: '91.5624' },
    { k: 'arimQuotaFissaAnnua', l: 'ARIM quota fissa annua', o: N, ph: '3.8568' },
  ] },
  { t: 'Imposte (accisa con esenzione residenti)', campi: [
    { k: 'accisaDomestico', l: 'Accisa domestico', o: N, ph: '0.0227' },
    { k: 'accisaNonDomestico', l: 'Accisa non domestico', o: N, ph: '0.0227' },
    { k: 'sogliaEsenzioneKwhMese', l: 'Soglia esenzione kWh/mese', o: N, ph: '150' },
    { k: 'potenzaMaxEsenzioneKw', l: 'Potenza max esenzione kW', o: N, ph: '3' },
    { k: 'sogliaErosioneKwhMese1_5', l: 'Soglia erosione ≤1,5 kW', o: N, ph: '150' },
    { k: 'sogliaErosioneKwhMese3', l: 'Soglia erosione 1,5-3 kW', o: N, ph: '220' },
  ] },
  { t: 'IVA', campi: [
    { k: 'ivaDomestico', l: 'IVA domestico', o: N, ph: '0.10' },
    { k: 'ivaNonDomestico', l: 'IVA non domestico', o: N, ph: '0.22' },
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

function SezioneConfigurazione() {
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
    <div className="space-y-6">
      <p className="text-gray-600 text-sm">
        Configurazione del gestore: {N} parametri nazionali ARERA, {G} corrispettivi del gestore.
      </p>

      <section className="bg-white rounded-xl shadow-sm border p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="font-semibold text-gray-800">Profili</h2>
          <button onClick={() => setSel({ nomeProfilo: 'Nuovo profilo', applicaEsenzioneAccisaResidenti: true, applicaScaglioni: true, arrotondaPerdite: false, quotaFissaSoloNonResidenti: true, quotaPotenzaSoloNonDomestici: true, altrePartiteInImponibile: true, supportaAliquoteMiste: true, usaAliquotaIvaBolletta: true, percentualePerdite: '0.1040', livelloTensioneDefault: 'BT' })}
            className="btn-sec">Nuovo profilo</button>
        </div>
        <ul className="divide-y">
          {lista.map((p) => (
            <li key={p.id} className="flex items-center justify-between gap-2 py-2">
              <button onClick={() => setSel(p)} className="text-left flex items-center gap-2 min-w-0">
                {p.predefinito && <Star className="w-4 h-4 shrink-0 text-yellow-500 fill-yellow-400" />}
                <b className="shrink-0">{p.nomeProfilo}</b> <span className="text-gray-400 text-sm truncate">{p.descrizione}</span>
              </button>
              <span className="flex gap-2 text-gray-500 shrink-0">
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

export default Gestore
