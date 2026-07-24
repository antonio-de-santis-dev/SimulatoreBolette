import { useEffect, useState } from 'react'
import client from '../api/client'
import { useToast } from '../components/Toast'
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'
import { TrendingDown } from 'lucide-react'

const COLORI = ['#2563eb', '#16a34a', '#f59e0b', '#dc2626', '#7c3aed']
const eur = (v) => (v == null ? '-' : Number(v).toFixed(2) + ' €')
const origineBadge = (o) => (o === 'NAZIONALE' ? '🏛️' : o === 'GESTORE' ? '🏢' : '📄')

function Confronto() {
  const toast = useToast()
  const [bollette, setBollette] = useState([])
  const [offerte, setOfferte] = useState([])
  const [profili, setProfili] = useState([])
  const [bollettaId, setBollettaId] = useState('')
  const [offertaId, setOffertaId] = useState('')
  const [parametriId, setParametriId] = useState('')
  const [ris, setRis] = useState(null)

  useEffect(() => {
    client.get('/bollette-concorrenti').then((r) => setBollette(r.data)).catch(toast.error)
    client.get('/offerte').then((r) => setOfferte(r.data)).catch(toast.error)
    client.get('/parametri-gestore').then((r) => setProfili(r.data)).catch(toast.error)
  }, [])

  const esegui = () => {
    if (!bollettaId || !offertaId) { toast.error('Seleziona bolletta e offerta'); return }
    client.post('/confronti', {
      bollettaConcorrenteId: Number(bollettaId),
      offertaGestoreId: Number(offertaId),
      parametriGestoreId: parametriId ? Number(parametriId) : null,
    }).then((r) => { setRis(r.data); toast.success('Confronto eseguito') }).catch(toast.error)
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Confronto</h1>
      <p className="text-gray-600">kWh del concorrente ricalcolati con i corrispettivi del gestore.</p>

      <section className="bg-white rounded-xl shadow-sm border p-5 grid md:grid-cols-4 gap-3 items-end">
        <Sel l="Bolletta concorrente" v={bollettaId} on={setBollettaId}
          opts={bollette.map((b) => ({ v: b.id, t: `${b.nomeFornitore} — ${b.nomeOfferta || ''}` }))} />
        <Sel l="Offerta del gestore" v={offertaId} on={setOffertaId}
          opts={offerte.map((o) => ({ v: o.id, t: `${o.nomeFornitore} — ${o.nomeOfferta}` }))} />
        <Sel l="Profilo parametri" v={parametriId} on={setParametriId} vuoto="(predefinito)"
          opts={profili.map((p) => ({ v: p.id, t: p.nomeProfilo }))} />
        <button onClick={esegui} className="btn-pri">Confronta</button>
      </section>

      {ris && (
        <>
          <section className="bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-xl p-6 shadow">
            <div className="flex items-center gap-2 text-sm opacity-90"><TrendingDown className="w-5 h-5" /> IL TUO RISPARMIO</div>
            <div className="flex gap-8 mt-2">
              <div><div className="text-3xl font-bold">{eur(ris.risparmioBimestrale)}</div><div className="text-sm opacity-90">bimestrale</div></div>
              <div><div className="text-3xl font-bold">{eur(ris.risparmioAnnuale)}</div><div className="text-sm opacity-90">annuale (× 6)</div></div>
            </div>
          </section>

          <section className="bg-white rounded-xl shadow-sm border p-5 overflow-x-auto">
            <h2 className="font-semibold mb-3">Confronto per categoria</h2>
            <table className="w-full text-sm">
              <thead><tr className="text-left text-gray-500 border-b">
                <th className="py-2">Categoria</th><th className="text-right">Concorrente</th>
                <th className="text-right">Gestore</th><th className="text-right">Differenza €</th><th className="text-right">Diff. %</th>
              </tr></thead>
              <tbody>
                {ris.categorie.map((c) => (
                  <tr key={c.categoria} className="border-b">
                    <td className="py-2">{c.descrizione}</td>
                    <td className="text-right">{eur(c.fatturatoConcorrente)}</td>
                    <td className="text-right">{eur(c.calcolatoGestore)}</td>
                    <td className={`text-right ${Number(c.differenza) >= 0 ? 'text-green-600' : 'text-red-600'}`}>{eur(c.differenza)}</td>
                    <td className="text-right text-gray-500">{c.differenzaPercentuale}%</td>
                  </tr>
                ))}
                <tr className="font-semibold">
                  <td className="py-2">Totale</td>
                  <td className="text-right">{eur(ris.concorrenteTotale)}</td>
                  <td className="text-right">{eur(ris.gestoreTotale)}</td>
                  <td className="text-right text-gray-600" colSpan="2">Imponibile {eur(ris.gestoreImponibile)} + IVA {eur(ris.gestoreIva)} (aliq. {ris.aliquotaIvaApplicata})</td>
                </tr>
              </tbody>
            </table>
          </section>

          <div className="grid md:grid-cols-2 gap-4">
            <section className="bg-white rounded-xl shadow-sm border p-5">
              <h2 className="font-semibold mb-3">Concorrente vs Gestore</h2>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={ris.categorie.map((c) => ({ nome: c.descrizione.split(' ')[0], Concorrente: Number(c.fatturatoConcorrente), Gestore: Number(c.calcolatoGestore) }))}>
                  <XAxis dataKey="nome" fontSize={11} /><YAxis fontSize={11} /><Tooltip /><Legend />
                  <Bar dataKey="Concorrente" fill="#94a3b8" /><Bar dataKey="Gestore" fill="#2563eb" />
                </BarChart>
              </ResponsiveContainer>
            </section>
            <section className="bg-white rounded-xl shadow-sm border p-5">
              <h2 className="font-semibold mb-3">Ripartizione gestore</h2>
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={ris.categorie.filter((c) => Number(c.calcolatoGestore) > 0).map((c) => ({ name: c.descrizione, value: Number(c.calcolatoGestore) }))}
                    dataKey="value" nameKey="name" outerRadius={90} label={(e) => e.name.split(' ')[0]}>
                    {ris.categorie.map((_, i) => <Cell key={i} fill={COLORI[i % COLORI.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </section>
          </div>

          {ris.mesi.map((m) => (
            <section key={m.numeroMese} className="bg-white rounded-xl shadow-sm border p-5 overflow-x-auto">
              <h2 className="font-semibold mb-1">Mese {m.numeroMese} {m.nomeMese && `— ${m.nomeMese}`}</h2>
              <p className="text-sm text-gray-500 mb-3">Netti {m.kwhNetti} · perdite {m.kwhPerdite} · con perdite {m.kwhConPerdite} kWh</p>
              <table className="w-full text-sm">
                <thead><tr className="text-left text-gray-500 border-b">
                  <th className="py-1">Orig.</th><th>Voce</th><th className="text-right">Corrispettivo</th>
                  <th className="text-right">Quantità</th><th className="text-right">Importo</th>
                </tr></thead>
                <tbody>
                  {m.righe.map((r, i) => (
                    <tr key={i} className="border-b last:border-0">
                      <td className="py-1" title={r.origineDescrizione}>{origineBadge(r.origine)}</td>
                      <td>{r.descrizione}</td>
                      <td className="text-right text-gray-500">{r.corrispettivo}</td>
                      <td className="text-right">{r.quantita}</td>
                      <td className="text-right">{eur(r.importo)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          ))}
        </>
      )}
    </div>
  )
}

const Sel = ({ l, v, on, opts, vuoto }) => (
  <label className="text-sm block">
    <span className="text-gray-500">{l}</span>
    <select className="input" value={v} onChange={(e) => on(e.target.value)}>
      <option value="">{vuoto || 'Seleziona...'}</option>
      {opts.map((o) => <option key={o.v} value={o.v}>{o.t}</option>)}
    </select>
  </label>
)

export default Confronto
