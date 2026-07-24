import { useEffect, useState } from 'react'
import client from '../api/client'
import { useToast } from '../components/Toast'
import { Plus, Trash2, Copy, FileText, CheckCircle, XCircle } from 'lucide-react'

const COEFF_BT = 0.104

const nuovoMese = (n) => ({
  numeroMese: n, nomeMese: '', mese: '', anno: '',
  consumoF1: '', consumoF2: '', consumoF3: '',
})

const bollettaVuota = () => ({
  numeroFattura: '', dataFattura: '', periodoDal: '', periodoAl: '',
  nomeFornitore: '', nomeOfferta: '', codiceOfferta: '',
  ragioneSociale: '', indirizzoFornitura: '', pod: '',
  tipologiaCliente: 'NON_RESIDENTE', opzioneTariffaria: '',
  potenzaImpegnata: '', potenzaDisponibile: '', livelloTensione: 'BT',
  mesi: [nuovoMese(1), nuovoMese(2)],
  altrePartite: [],
  fatturatoMateriaEnergia: '', fatturatoTrasporto: '', fatturatoOneriSistema: '',
  fatturatoAltrePartite: '', fatturatoImposte: '', fatturatoIva: '',
  fatturatoImponibile: '', fatturatoTotale: '', aliquotaIvaApplicata: '0.10',
  note: '',
})

const num = (v) => (v === '' || v == null ? 0 : parseFloat(v))
const round0 = (v) => Math.round(v)

function BolletteConcorrenti() {
  const toast = useToast()
  const [lista, setLista] = useState([])
  const [b, setB] = useState(bollettaVuota())

  const carica = () => client.get('/bollette-concorrenti').then((r) => setLista(r.data)).catch(toast.error)
  useEffect(() => { carica() }, [])

  const set = (campo, valore) => setB((p) => ({ ...p, [campo]: valore }))
  const setMese = (i, campo, valore) => setB((p) => {
    const mesi = [...p.mesi]; mesi[i] = { ...mesi[i], [campo]: valore }; return { ...p, mesi }
  })

  const addPartita = () => setB((p) => ({
    ...p, altrePartite: [...p.altrePartite,
      { descrizione: '', dal: '', al: '', importo: '', aliquotaIva: '0.10', soggettaIva: true, ordine: p.altrePartite.length + 1 }],
  }))
  const setPartita = (i, campo, valore) => setB((p) => {
    const ap = [...p.altrePartite]; ap[i] = { ...ap[i], [campo]: valore }; return { ...p, altrePartite: ap }
  })
  const delPartita = (i) => setB((p) => ({ ...p, altrePartite: p.altrePartite.filter((_, x) => x !== i) }))

  const subtotalePartite = b.altrePartite.reduce((s, a) => s + num(a.importo), 0)

  // Quadratura live
  const sommaComponenti = num(b.fatturatoMateriaEnergia) + num(b.fatturatoTrasporto)
    + num(b.fatturatoOneriSistema) + num(b.fatturatoAltrePartite) + num(b.fatturatoImposte)
  const quadraImponibile = Math.abs(sommaComponenti - num(b.fatturatoImponibile)) <= 0.01
  const quadraTotale = Math.abs(num(b.fatturatoImponibile) + num(b.fatturatoIva) - num(b.fatturatoTotale)) <= 0.01
  const quadra = quadraImponibile && quadraTotale && num(b.fatturatoTotale) > 0

  const salva = () => {
    const payload = {
      ...b,
      mesi: b.mesi.filter((m) => m.consumoF1 !== '' || m.consumoF2 !== '' || m.consumoF3 !== ''),
    }
    client.post('/bollette-concorrenti', payload)
      .then(() => { toast.success('Bolletta salvata'); setB(bollettaVuota()); carica() })
      .catch(toast.error)
  }

  const elimina = (id) => client.delete(`/bollette-concorrenti/${id}`)
    .then(() => { toast.info('Bolletta eliminata'); carica() }).catch(toast.error)
  const duplica = (id) => client.post(`/bollette-concorrenti/${id}/duplica`)
    .then(() => { toast.success('Bolletta duplicata'); carica() }).catch(toast.error)

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Bollette da comparare</h1>
        <p className="text-gray-600">Sorgente A — dati del concorrente letti dal PDF del cliente.</p>
      </div>

      {/* Sezione 1 */}
      <Sezione titolo="1. Dati fattura">
        <Grid>
          <Campo l="Numero fattura" v={b.numeroFattura} on={(v) => set('numeroFattura', v)} />
          <Campo l="Data fattura" t="date" v={b.dataFattura} on={(v) => set('dataFattura', v)} />
          <Campo l="Periodo dal" t="date" v={b.periodoDal} on={(v) => set('periodoDal', v)} />
          <Campo l="Periodo al" t="date" v={b.periodoAl} on={(v) => set('periodoAl', v)} />
          <Campo l="Fornitore" v={b.nomeFornitore} on={(v) => set('nomeFornitore', v)} />
          <Campo l="Nome offerta" v={b.nomeOfferta} on={(v) => set('nomeOfferta', v)} />
          <Campo l="Codice offerta" v={b.codiceOfferta} on={(v) => set('codiceOfferta', v)} />
        </Grid>
      </Sezione>

      {/* Sezione 2 */}
      <Sezione titolo="2. Dati fornitura">
        <Grid>
          <Campo l="Ragione sociale" v={b.ragioneSociale} on={(v) => set('ragioneSociale', v)} />
          <Campo l="Indirizzo fornitura" v={b.indirizzoFornitura} on={(v) => set('indirizzoFornitura', v)} />
          <Campo l="POD" v={b.pod} on={(v) => set('pod', v)} />
          <Select l="Tipologia cliente" v={b.tipologiaCliente} on={(v) => set('tipologiaCliente', v)}
            opts={['RESIDENTE', 'NON_RESIDENTE', 'DOMESTICO_USI_DIVERSI', 'ATTIVITA_PRODUTTIVE', 'PMI_RESIDENZIALE']} />
          <Campo l="Opzione tariffaria" v={b.opzioneTariffaria} on={(v) => set('opzioneTariffaria', v)} />
          <Campo l="Potenza impegnata (kW)" t="number" v={b.potenzaImpegnata} on={(v) => set('potenzaImpegnata', v)} />
          <Campo l="Potenza disponibile (kW)" t="number" v={b.potenzaDisponibile} on={(v) => set('potenzaDisponibile', v)} />
          <Select l="Livello tensione" v={b.livelloTensione} on={(v) => set('livelloTensione', v)}
            opts={['BT', 'MT', 'AT_150', 'AT_220', 'AT_370']} />
        </Grid>
      </Sezione>

      {/* Sezione 3 — consumi */}
      <Sezione titolo="3. Consumi (celle gialle dell'Excel)">
        <div className="grid md:grid-cols-2 gap-4">
          {b.mesi.map((m, i) => {
            const netti = num(m.consumoF1) + num(m.consumoF2) + num(m.consumoF3)
            const perdite = round0(num(m.consumoF1) * COEFF_BT) + round0(num(m.consumoF2) * COEFF_BT) + round0(num(m.consumoF3) * COEFF_BT)
            return (
              <div key={i} className="border rounded-lg p-4 bg-yellow-50/40">
                <div className="flex gap-2 mb-3">
                  <Campo l="Nome mese" v={m.nomeMese} on={(v) => setMese(i, 'nomeMese', v)} />
                  <Campo l="Mese" t="number" v={m.mese} on={(v) => setMese(i, 'mese', v)} />
                  <Campo l="Anno" t="number" v={m.anno} on={(v) => setMese(i, 'anno', v)} />
                </div>
                <div className="grid grid-cols-3 gap-2">
                  <CampoGiallo l="F1 kWh" v={m.consumoF1} on={(v) => setMese(i, 'consumoF1', v)} />
                  <CampoGiallo l="F2 kWh" v={m.consumoF2} on={(v) => setMese(i, 'consumoF2', v)} />
                  <CampoGiallo l="F3 kWh" v={m.consumoF3} on={(v) => setMese(i, 'consumoF3', v)} />
                </div>
                <div className="mt-3 text-sm text-gray-700 flex gap-4">
                  <span>Netti: <b>{netti}</b></span>
                  <span>Perdite (BT): <b>{perdite}</b></span>
                  <span>Con perdite: <b>{netti + perdite}</b></span>
                </div>
              </div>
            )
          })}
        </div>
      </Sezione>

      {/* Sezione 4 — altre partite */}
      <Sezione titolo="4. Altre partite">
        <table className="w-full text-sm">
          <thead><tr className="text-left text-gray-500">
            <th className="pb-2">Descrizione</th><th>Importo</th><th>IVA</th><th>Soggetta</th><th></th>
          </tr></thead>
          <tbody>
            {b.altrePartite.map((a, i) => (
              <tr key={i}>
                <td className="pr-2 py-1"><input className="input" value={a.descrizione} onChange={(e) => setPartita(i, 'descrizione', e.target.value)} /></td>
                <td className="pr-2"><input className="input w-24" type="number" step="0.01" value={a.importo} onChange={(e) => setPartita(i, 'importo', e.target.value)} /></td>
                <td className="pr-2"><input className="input w-16" type="number" step="0.01" value={a.aliquotaIva} onChange={(e) => setPartita(i, 'aliquotaIva', e.target.value)} /></td>
                <td className="pr-2 text-center"><input type="checkbox" checked={a.soggettaIva} onChange={(e) => setPartita(i, 'soggettaIva', e.target.checked)} /></td>
                <td><button onClick={() => delPartita(i)} className="text-red-500"><Trash2 className="w-4 h-4" /></button></td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="flex items-center justify-between mt-2">
          <button onClick={addPartita} className="btn-sec flex items-center gap-1"><Plus className="w-4 h-4" /> Aggiungi riga</button>
          <span className="text-sm">Subtotale: <b>{subtotalePartite.toFixed(2)} €</b></span>
        </div>
      </Sezione>

      {/* Sezione 5 — importi fatturati + quadratura */}
      <Sezione titolo="5. Importi fatturati dal concorrente">
        <Grid>
          <Campo l="Materia energia" t="number" v={b.fatturatoMateriaEnergia} on={(v) => set('fatturatoMateriaEnergia', v)} />
          <Campo l="Trasporto" t="number" v={b.fatturatoTrasporto} on={(v) => set('fatturatoTrasporto', v)} />
          <Campo l="Oneri di sistema" t="number" v={b.fatturatoOneriSistema} on={(v) => set('fatturatoOneriSistema', v)} />
          <Campo l="Altre partite" t="number" v={b.fatturatoAltrePartite} on={(v) => set('fatturatoAltrePartite', v)} />
          <Campo l="Imposte" t="number" v={b.fatturatoImposte} on={(v) => set('fatturatoImposte', v)} />
          <Campo l="Imponibile" t="number" v={b.fatturatoImponibile} on={(v) => set('fatturatoImponibile', v)} />
          <Campo l="IVA" t="number" v={b.fatturatoIva} on={(v) => set('fatturatoIva', v)} />
          <Campo l="Totale" t="number" v={b.fatturatoTotale} on={(v) => set('fatturatoTotale', v)} />
          <Campo l="Aliquota IVA" t="number" v={b.aliquotaIvaApplicata} on={(v) => set('aliquotaIvaApplicata', v)} />
        </Grid>
        <div className={`mt-4 flex items-center gap-2 rounded-lg px-4 py-3 border ${quadra ? 'bg-green-50 border-green-300 text-green-800' : 'bg-red-50 border-red-300 text-red-800'}`}>
          {quadra ? <CheckCircle className="w-5 h-5" /> : <XCircle className="w-5 h-5" />}
          {quadra
            ? <span>Quadratura OK: le componenti sommano al totale.</span>
            : <span>Quadratura KO — somma componenti {sommaComponenti.toFixed(2)} vs imponibile {num(b.fatturatoImponibile).toFixed(2)} (Δ {(sommaComponenti - num(b.fatturatoImponibile)).toFixed(2)})</span>}
        </div>
      </Sezione>

      <div className="flex justify-end">
        <button onClick={salva} className="btn-pri">Salva bolletta</button>
      </div>

      {/* Elenco */}
      <Sezione titolo="Bollette inserite">
        {lista.length === 0 && <p className="text-gray-500 text-sm">Nessuna bolletta inserita.</p>}
        <ul className="divide-y">
          {lista.map((x) => (
            <li key={x.id} className="flex items-center justify-between py-2">
              <span className="flex items-center gap-2"><FileText className="w-4 h-4 text-gray-400" />
                <b>{x.nomeFornitore}</b> — {x.nomeOfferta} ({x.numeroFattura || 's.n.'}) · {x.fatturatoTotale} €</span>
              <span className="flex gap-2">
                <button onClick={() => duplica(x.id)} className="text-gray-500"><Copy className="w-4 h-4" /></button>
                <button onClick={() => elimina(x.id)} className="text-red-500"><Trash2 className="w-4 h-4" /></button>
              </span>
            </li>
          ))}
        </ul>
      </Sezione>
    </div>
  )
}

const Sezione = ({ titolo, children }) => (
  <section className="bg-white rounded-xl shadow-sm border p-5">
    <h2 className="font-semibold text-gray-800 mb-4">{titolo}</h2>
    {children}
  </section>
)
const Grid = ({ children }) => <div className="grid md:grid-cols-3 gap-3">{children}</div>
const Campo = ({ l, v, on, t = 'text' }) => (
  <label className="text-sm block">
    <span className="text-gray-500">{l}</span>
    <input className="input" type={t} step="0.01" value={v} onChange={(e) => on(e.target.value)} />
  </label>
)
const CampoGiallo = ({ l, v, on }) => (
  <label className="text-sm block">
    <span className="text-gray-600 font-medium">{l}</span>
    <input className="input bg-yellow-100 border-yellow-400 font-semibold" type="number" step="0.01" value={v} onChange={(e) => on(e.target.value)} />
  </label>
)
const Select = ({ l, v, on, opts }) => (
  <label className="text-sm block">
    <span className="text-gray-500">{l}</span>
    <select className="input" value={v} onChange={(e) => on(e.target.value)}>
      {opts.map((o) => <option key={o} value={o}>{o}</option>)}
    </select>
  </label>
)

export default BolletteConcorrenti
