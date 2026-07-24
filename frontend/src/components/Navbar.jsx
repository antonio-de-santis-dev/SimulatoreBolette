import { Link, useLocation } from 'react-router-dom'
import { Zap, BarChart3, History, List, FileText, SlidersHorizontal } from 'lucide-react'

function Navbar() {
  const location = useLocation()

  const navItems = [
    { path: '/', label: 'Home', icon: Zap },
    { path: '/bollette', label: 'Bollette', icon: FileText },
    { path: '/confronto', label: 'Confronto', icon: BarChart3 },
    { path: '/offerte', label: 'Offerte', icon: List },
    { path: '/parametri', label: 'Parametri', icon: SlidersHorizontal },
    { path: '/storico', label: 'Storico', icon: History },
  ]

  return (
    <nav className="bg-white shadow-sm border-b">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2">
            <Zap className="w-8 h-8 text-energy-blue" />
            <span className="text-xl font-bold text-gray-900">SimulaLuce</span>
          </Link>

          <div className="flex gap-1">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = location.pathname === item.path
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    isActive 
                      ? 'bg-energy-blue text-white' 
                      : 'text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span className="hidden md:inline">{item.label}</span>
                </Link>
              )
            })}
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
