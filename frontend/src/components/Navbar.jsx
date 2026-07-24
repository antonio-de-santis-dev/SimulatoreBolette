import { Link, useLocation } from 'react-router-dom'
import { Zap, BarChart3, History, List, FileText, SlidersHorizontal } from 'lucide-react'

const navItems = [
  { path: '/', label: 'Home', icon: Zap },
  { path: '/bollette', label: 'Bollette', icon: FileText },
  { path: '/confronto', label: 'Confronto', icon: BarChart3 },
  { path: '/offerte', label: 'Offerte', icon: List },
  { path: '/parametri', label: 'Parametri', icon: SlidersHorizontal },
  { path: '/storico', label: 'Storico', icon: History },
]

function Navbar() {
  const location = useLocation()
  const isActive = (path) => location.pathname === path

  return (
    <>
      {/* ── Barra superiore ──────────────────────────────────────────────
          Mobile: solo il logo (h-14). Desktop (md+): logo + 6 link con etichetta. */}
      <nav className="bg-white shadow-sm border-b">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between h-14 md:h-16">
            <Link to="/" className="flex items-center gap-2">
              <Zap className="w-7 h-7 md:w-8 md:h-8 text-energy-blue" />
              <span className="text-lg md:text-xl font-bold text-gray-900">SimulaLuce</span>
            </Link>

            <div className="hidden md:flex gap-1">
              {navItems.map((item) => {
                const Icon = item.icon
                const active = isActive(item.path)
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    aria-current={active ? 'page' : undefined}
                    className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                      active ? 'bg-energy-blue text-white' : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{item.label}</span>
                  </Link>
                )
              })}
            </div>
          </div>
        </div>
      </nav>

      {/* ── Bottom tab bar (solo mobile, sotto md) ───────────────────────
          Stile app nativa: barra fissa in basso, 6 celle uguali, icona sopra
          ed etichetta sotto. La voce attiva si distingue per colore e tratto
          piu' marcato dell'icona (non per sfondo pieno). Rispetta la safe area. */}
      <nav
        className="md:hidden fixed bottom-0 inset-x-0 z-40 bg-white border-t shadow-[0_-1px_4px_rgba(0,0,0,0.06)] pb-[env(safe-area-inset-bottom)]"
        aria-label="Navigazione principale"
      >
        <div className="grid grid-cols-6">
          {navItems.map((item) => {
            const Icon = item.icon
            const active = isActive(item.path)
            return (
              <Link
                key={item.path}
                to={item.path}
                aria-current={active ? 'page' : undefined}
                className={`flex flex-col items-center justify-center gap-1 min-h-[56px] px-1 text-[10px] font-medium leading-none transition-colors ${
                  active ? 'text-energy-blue' : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <Icon className="w-5 h-5" strokeWidth={active ? 2.6 : 1.9} />
                <span>{item.label}</span>
              </Link>
            )
          })}
        </div>
      </nav>
    </>
  )
}

export default Navbar
