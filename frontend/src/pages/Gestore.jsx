import { useEffect, useState } from 'react'
import client from '../api/client'
import { useToast } from '../components/Toast'
import {
  Building2, Plus, Trash2, Pencil, Star, Copy, Eye, ArrowLeft, Save, X, Info,
} from 'lucide-react'

// Flag di conformita: chiave -> etichetta (sola parte gestore/commerciale)
const FLAG = [
  { key: 'applicaEsenzioneAccisaResidenti', label: 'Esenzione accisa residenti' },
  { key: 'applicaScaglioni', label: 'Applica scaglioni' },
  { key: 'arrotondaPerdite', label: 'Arrotonda perdite' },
  { key: 'quotaFissaSoloNonResidenti', label: 'Quota fissa solo non residenti' },
  { key: 'altrePartiteInImponibile', label: 'Altre partite in imponibile' },
  { key: 'supportaAliquoteMiste', label: 'Supporta aliquote miste' },
  { key: 'usaAliquotaIvaBolletta', label: 'Usa aliquota IVA bolletta' },
]

const OFFERTA_VUOTA = {
  nomeFornitore: '', nomeOfferta: '', tipoOfferta: 'PREZZO_FISSO', tipoTariffa: 'MONORARIA',
  prezzoFissoF0: '', prezzoFissoF1: '', prezzoFissoF23: '', spreadPunF0: '', pcvAnnuo: '',
  condizioniSpeciali: '',
}

function Gestore() {
  const toast = useToast()
  const [vista, setVista] = useState('lista') // 'lista' | 'dettaglio'
  const [gestori, setGestori] = useState([])
  const [dett, setDett] = useState(null)
  const [editMode, setEditMode] = useState(false)
  const [editData, setEditData] = useState(null)
  const [offertaForm, setOffertaForm] = useState(null)

  useEffect(() => { loadLista() }, [])

  const loadLista = () => client.get('/gestori').then((r) => setGestori(r.data)).catch(toast.error)

  const apri = (id) => client.get(`/gestori/${id}`).then((r) => {
    setDett(r.data); setEditMode(false); setVista('dettaglio')
  }).catch(toast.error)

  const tornaLista = () => { setVista('lista'); setDett(null); setEditMode(false); loadLista() }

  const nuovo = () => client.post('/gestori', { nomeProfilo: `Nuovo gestore ${Date.now()}`, nomeGestore: 'Nuovo gestore' })
    .then((r) => { toast.success('Gestore creato'); apri(r.data.id).then(() => attivaModifica(r.data)) })
    .catch(toast.error)

  const elimina = (id) => {
    if (!confirm('Eliminare questo gestore? Le offerte collegate vengono scollegate, non eliminate.')) return
    client.delete(`/gestori/${id}`).then(() => { toast.info('Gestore eliminato'); tornaLista() }).catch(toast.error)
  }
  const setPredefinito = (id) => client.post(`/gestori/${id}/predefinito`)
    .then(() => { toast.success('Impostato come predefinito'); apri(id) }).catch(toast.error)
  const duplica = (id) => client.post(`/gestori/${id}/duplica`)
    .then((r) => { toast.success('Gestore duplicato'); apri(r.data.id) }).catch(toast.error)

  // ── modifica gestore ──
  const attivaModifica = (d = dett) => {
    setEditData({
      nomeProfilo: d.nomeProfilo ?? '', nomeGestore: d.nomeGestore ?? '', descrizione: d.descrizione ?? '',
      commercializzazioneMese: d.commercializzazioneMese ?? '', pcvVariabile: d.pcvVariabile ?? '',
      spreadEnergia: d.spreadEnergia ?? '',
      ...FLAG.reduce((a, f) => ({ ...a, [f.key]: !!d[f.key] }), {}),
    })
    setEditMode(true)
  }
  const annulla = () => { setEditMode(false); setEditData(null) }
  const salva = () => {
    const payload = { ...editData }
    ;['commercializzazioneMese', 'pcvVariabile', 'spreadEnergia'].forEach((k) => {
      payload[k] = payload[k] === '' ? null : payload[k]
    })
    client.put(`/gestori/${dett.id}`, payload).then((r) => {
      toast.success('Modifiche salvate'); setDett(r.data); setEditMode(false); setEditData(null)
    }).catch(toast.error)
  }

  // ── offerte ──
  const salvaOfferta = (e) => {
    e.preventDefault()
    const payload = { ...offertaForm }
    ;['prezzoFissoF0', 'prezzoFissoF1', 'prezzoFissoF23', 'spreadPunF0', 'pcvAnnuo'].forEach((k) => {
      payload[k] = payload[k] === '' ? null : payload[k]
    })
    const gid = dett.id
    const req = offertaForm.id
      ? client.put(`/gestori/${gid}/offerte/${offertaForm.id}`, payload)
      : client.post(`/gestori/${gid}/offerte`, payload)
    req.then(() => { toast.success(offertaForm.id ? 'Offerta aggiornata' : 'Offerta aggiunta'); setOffertaForm(null); apri(gid) })
      .catch(toast.error)
  }
  const eliminaOfferta = (offertaId) => {
    if (!confirm('Eliminare questa offerta?')) return
    client.delete(`/gestori/${dett.id}/offerte/${offertaId}`)
      .then(() => { toast.info('Offerta eliminata'); apri(dett.id) }).catch(toast.error)
  }

  // ═══════════════ LISTA ═══════════════
  if (vista === 'lista') {
    return (
      <div className="max-w-5xl mx-auto">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between mb-6">
          <h1 className="text-2xl md:text-3xl font-bold flex items-center gap-3">
            <Building2 className="w-8 h-8 text-energy-blue" /> Il mio gestore
          </h1>
          <button onClick={nuovo} className="btn-primary w-full md:w-auto">
            <Plus className="w-5 h-5 inline mr-2" /> Nuovo gestore
          </button>
        </div>
        <p className="text-gray-600 mb-6">Configurazione del gestore e relative offerte, in un'unica area.</p>

        {gestori.length === 0 ? (
          <div className="card text-center text-gray-500 py-12">Nessun gestore configurato.</div>
        ) : (
          <div className="space-y-3">
            {gestori.map((g) => (
              <div key={g.id} className="card flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold text-lg truncate">{g.nomeGestore || 'Senza nome'}</h3>
                    {g.predefinito && (
                      <span className="px-2 py-0.5 rounded-full text-xs bg-energy-blue/10 text-energy-blue flex items-center gap-1 shrink-0">
                        <Star className="w-3 h-3" /> predefinito
                      </span>
                    )}
                  </div>
                  <p className="text-gray-500 text-sm truncate">{g.nomeProfilo}</p>
                  <p className="text-gray-500 text-sm mt-1">
                    {g.numeroOfferte} {g.numeroOfferte === 1 ? 'offerta' : 'offerte'}
                    {g.commercializzazioneMese != null && ` · commercializzazione €${g.commercializzazioneMese}/mese`}
                  </p>
                </div>
                <button onClick={() => apri(g.id)} className="btn-sec flex items-center gap-2 shrink-0">
                  <Eye className="w-4 h-4" /> <span className="hidden sm:inline">Visualizza dettaglio</span>
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    )
  }

  // ═══════════════ DETTAGLIO ═══════════════
  if (!dett) return <div className="text-center py-12">Caricamento...</div>

  return (
    <div className="max-w-4xl mx-auto">
      <button onClick={tornaLista} className="text-gray-600 hover:text-gray-900 flex items-center gap-2 mb-4">
        <ArrowLeft className="w-4 h-4" /> Torna alla lista
      </button>

      <div className="flex flex-col gap-3 md:flex-row md:justify-between md:items-start mb-6">
        <h1 className="text-2xl md:text-3xl font-bold flex items-center gap-3">
          <Building2 className="w-8 h-8 text-energy-blue" />
          {dett.nomeGestore || 'Gestore'}
          {dett.predefinito && (
            <span className="px-2 py-0.5 rounded-full text-xs bg-energy-blue/10 text-energy-blue flex items-center gap-1">
              <Star className="w-3 h-3" /> predefinito
            </span>
          )}
        </h1>
        {!editMode ? (
          <div className="flex gap-2">
            <button onClick={() => attivaModifica()} className="btn-primary flex items-center gap-2">
              <Pencil className="w-4 h-4" /> Modifica
            </button>
            {!dett.predefinito && (
              <button onClick={() => setPredefinito(dett.id)} className="btn-sec" title="Imposta predefinito"><Star className="w-4 h-4" /></button>
            )}
            <button onClick={() => duplica(dett.id)} className="btn-sec" title="Duplica"><Copy className="w-4 h-4" /></button>
            <button onClick={() => elimina(dett.id)} className="btn-sec text-red-600" title="Elimina"><Trash2 className="w-4 h-4" /></button>
          </div>
        ) : (
          <div className="flex gap-2">
            <button onClick={salva} className="btn-primary flex items-center gap-2"><Save className="w-4 h-4" /> Salva</button>
            <button onClick={annulla} className="btn-sec flex items-center gap-2"><X className="w-4 h-4" /> Annulla</button>
          </div>
        )}
      </div>

      {/* Anagrafica */}
      <Sez titolo="Anagrafica">
        {editMode ? (
          <div className="grid md:grid-cols-2 gap-4">
            <Campo label="Nome gestore"><input className="input" value={editData.nomeGestore} onChange={(e) => setEditData({ ...editData, nomeGestore: e.target.value })} /></Campo>
            <Campo label="Nome profilo"><input className="input" value={editData.nomeProfilo} onChange={(e) => setEditData({ ...editData, nomeProfilo: e.target.value })} /></Campo>
            <Campo label="Descrizione" full><textarea className="input" rows={2} value={editData.descrizione} onChange={(e) => setEditData({ ...editData, descrizione: e.target.value })} /></Campo>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 gap-4">
            <Ro label="Nome gestore" v={dett.nomeGestore} />
            <Ro label="Nome profilo" v={dett.nomeProfilo} />
            <Ro label="Descrizione" v={dett.descrizione} full />
            <Ro label="Predefinito" v={dett.predefinito ? 'Sì' : 'No'} />
          </div>
        )}
      </Sez>

      {/* Config commerciale */}
      <Sez titolo="Configurazione commerciale">
        {editMode ? (
          <div className="grid md:grid-cols-3 gap-4">
            <Campo label="Commercializzazione (€/mese)"><input type="number" step="0.0001" className="input" value={editData.commercializzazioneMese} onChange={(e) => setEditData({ ...editData, commercializzazioneMese: e.target.value })} /></Campo>
            <Campo label="PCV variabile (€/kWh)"><input type="number" step="0.0001" className="input" value={editData.pcvVariabile} onChange={(e) => setEditData({ ...editData, pcvVariabile: e.target.value })} /></Campo>
            <Campo label="Spread energia (€/kWh)"><input type="number" step="0.000001" className="input" value={editData.spreadEnergia} onChange={(e) => setEditData({ ...editData, spreadEnergia: e.target.value })} /></Campo>
          </div>
        ) : (
          <div className="grid md:grid-cols-3 gap-4">
            <Ro label="Commercializzazione (€/mese)" v={dett.commercializzazioneMese} mono />
            <Ro label="PCV variabile (€/kWh)" v={dett.pcvVariabile} mono />
            <Ro label="Spread energia (€/kWh)" v={dett.spreadEnergia} mono />
          </div>
        )}
      </Sez>

      {/* Flag */}
      <Sez titolo="Flag di conformità">
        <div className="grid md:grid-cols-2 gap-3">
          {FLAG.map((f) => (
            <div key={f.key} className="flex items-center justify-between border-b border-gray-100 py-2">
              <span className="text-sm text-gray-700">{f.label}</span>
              {editMode ? (
                <input type="checkbox" checked={!!editData[f.key]} onChange={(e) => setEditData({ ...editData, [f.key]: e.target.checked })} />
              ) : (
                <span className={`px-2 py-0.5 rounded text-xs font-medium ${dett[f.key] ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                  {dett[f.key] ? 'Attivo' : 'Disattivo'}
                </span>
              )}
            </div>
          ))}
        </div>
      </Sez>

      {/* Placeholder parametri nazionali (prompt 2) */}
      <div className="card mb-6 bg-blue-50 border border-blue-100 flex items-start gap-3">
        <Info className="w-5 h-5 text-blue-500 shrink-0 mt-0.5" />
        <p className="text-sm text-blue-800">
          I parametri nazionali ARERA (accise, trasporto, oneri, dispacciamento, IVA, perdite)
          si gestiscono nell'area dedicata.
        </p>
      </div>

      {/* Offerte */}
      <Sez titolo="Offerte" azione={!editMode && (
        <button onClick={() => setOffertaForm({ ...OFFERTA_VUOTA })} className="btn-pri text-sm flex items-center gap-2">
          <Plus className="w-4 h-4" /> Aggiungi offerta
        </button>
      )}>
        {dett.offerte?.length ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left">Fornitore</th>
                  <th className="px-3 py-2 text-left">Offerta</th>
                  <th className="px-3 py-2 text-left">Tipo</th>
                  <th className="px-3 py-2 text-left">Tariffa</th>
                  <th className="px-3 py-2 text-right">Prezzo/Spread</th>
                  <th className="px-3 py-2 text-right">PCV/anno</th>
                  <th className="px-3 py-2 text-right">Azioni</th>
                </tr>
              </thead>
              <tbody>
                {dett.offerte.map((o) => (
                  <tr key={o.id} className="border-t hover:bg-gray-50">
                    <td className="px-3 py-2 font-medium">{o.nomeFornitore}</td>
                    <td className="px-3 py-2">{o.nomeOfferta}</td>
                    <td className="px-3 py-2">{o.tipoOfferta === 'PREZZO_FISSO' ? 'Fisso' : o.tipoOfferta === 'INDICIZZATA_PUN' ? 'PUN' : 'Mista'}</td>
                    <td className="px-3 py-2">{o.tipoTariffa}</td>
                    <td className="px-3 py-2 text-right font-mono">
                      {o.prezzoFissoF0 ? `€${o.prezzoFissoF0}` : o.prezzoFissoF1 ? `€${o.prezzoFissoF1}` : o.spreadPunF0 ? `+€${o.spreadPunF0}` : '-'}
                    </td>
                    <td className="px-3 py-2 text-right font-mono">{o.pcvAnnuo != null ? `€${o.pcvAnnuo}` : '-'}</td>
                    <td className="px-3 py-2 text-right whitespace-nowrap">
                      <button onClick={() => setOffertaForm({ ...OFFERTA_VUOTA, ...o })} className="text-gray-500 hover:text-energy-blue mr-3" title="Modifica"><Pencil className="w-4 h-4 inline" /></button>
                      <button onClick={() => eliminaOfferta(o.id)} className="text-red-500 hover:text-red-700" title="Elimina"><Trash2 className="w-4 h-4 inline" /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-500 text-sm">Nessuna offerta collegata a questo gestore.</p>
        )}
      </Sez>

      {/* Modale offerta */}
      {offertaForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-40 p-4">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold">{offertaForm.id ? 'Modifica offerta' : 'Nuova offerta'}</h3>
              <button onClick={() => setOffertaForm(null)}><X className="w-5 h-5" /></button>
            </div>
            <form onSubmit={salvaOfferta} className="grid md:grid-cols-2 gap-4">
              <input placeholder="Fornitore" className="input" required value={offertaForm.nomeFornitore} onChange={(e) => setOffertaForm({ ...offertaForm, nomeFornitore: e.target.value })} />
              <input placeholder="Nome offerta" className="input" required value={offertaForm.nomeOfferta} onChange={(e) => setOffertaForm({ ...offertaForm, nomeOfferta: e.target.value })} />
              <select className="input" value={offertaForm.tipoOfferta} onChange={(e) => setOffertaForm({ ...offertaForm, tipoOfferta: e.target.value })}>
                <option value="PREZZO_FISSO">Prezzo fisso</option>
                <option value="INDICIZZATA_PUN">Indicizzata PUN</option>
                <option value="MISTA">Mista</option>
              </select>
              <select className="input" value={offertaForm.tipoTariffa} onChange={(e) => setOffertaForm({ ...offertaForm, tipoTariffa: e.target.value })}>
                <option value="MONORARIA">Monoraria</option>
                <option value="BIORARIA">Bioraria</option>
                <option value="TRIORARIA">Trioraria</option>
              </select>
              <input placeholder="Prezzo fisso F0 (€/kWh)" type="number" step="0.0001" className="input" value={offertaForm.prezzoFissoF0} onChange={(e) => setOffertaForm({ ...offertaForm, prezzoFissoF0: e.target.value })} />
              <input placeholder="Spread PUN F0 (€/kWh)" type="number" step="0.0001" className="input" value={offertaForm.spreadPunF0} onChange={(e) => setOffertaForm({ ...offertaForm, spreadPunF0: e.target.value })} />
              <input placeholder="Prezzo fisso F1 (€/kWh)" type="number" step="0.0001" className="input" value={offertaForm.prezzoFissoF1} onChange={(e) => setOffertaForm({ ...offertaForm, prezzoFissoF1: e.target.value })} />
              <input placeholder="Prezzo fisso F23 (€/kWh)" type="number" step="0.0001" className="input" value={offertaForm.prezzoFissoF23} onChange={(e) => setOffertaForm({ ...offertaForm, prezzoFissoF23: e.target.value })} />
              <input placeholder="PCV annuo (€/anno)" type="number" step="0.01" className="input" value={offertaForm.pcvAnnuo} onChange={(e) => setOffertaForm({ ...offertaForm, pcvAnnuo: e.target.value })} />
              <input placeholder="Condizioni speciali" className="input md:col-span-2" value={offertaForm.condizioniSpeciali || ''} onChange={(e) => setOffertaForm({ ...offertaForm, condizioniSpeciali: e.target.value })} />
              <div className="md:col-span-2 flex gap-2 justify-end">
                <button type="button" onClick={() => setOffertaForm(null)} className="btn-sec">Annulla</button>
                <button type="submit" className="btn-pri">Salva offerta</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

const Sez = ({ titolo, azione, children }) => (
  <div className="card mb-6">
    <div className="flex justify-between items-center mb-4">
      <h2 className="text-lg font-semibold">{titolo}</h2>
      {azione}
    </div>
    {children}
  </div>
)
const Campo = ({ label, children, full }) => (
  <div className={full ? 'md:col-span-2' : ''}>
    <label className="label">{label}</label>
    {children}
  </div>
)
const Ro = ({ label, v, mono, full }) => (
  <div className={full ? 'md:col-span-2' : ''}>
    <p className="text-xs text-gray-500 uppercase tracking-wide">{label}</p>
    <p className={`text-gray-900 ${mono ? 'font-mono' : ''}`}>{v != null && v !== '' ? v : '—'}</p>
  </div>
)

export default Gestore
