import { createContext, useContext, useState, useCallback } from 'react'
import { CheckCircle, AlertCircle, Info, X } from 'lucide-react'

const ToastContext = createContext(null)

/**
 * Provider dei toast: sostituisce i console.error silenziosi con messaggi visibili.
 * Uso: const toast = useToast(); toast.success('...'); toast.error(err).
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const remove = useCallback((id) => {
    setToasts((t) => t.filter((x) => x.id !== id))
  }, [])

  const push = useCallback((tipo, messaggio) => {
    const id = Date.now() + Math.random()
    setToasts((t) => [...t, { id, tipo, messaggio }])
    setTimeout(() => remove(id), 5000)
  }, [remove])

  const api = {
    success: (m) => push('success', m),
    info: (m) => push('info', m),
    error: (e) => {
      const msg = e?.response?.data?.message || e?.message || String(e)
      push('error', msg)
    },
  }

  const stile = {
    success: 'bg-green-50 border-green-300 text-green-800',
    error: 'bg-red-50 border-red-300 text-red-800',
    info: 'bg-blue-50 border-blue-300 text-blue-800',
  }
  const Icona = { success: CheckCircle, error: AlertCircle, info: Info }

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 w-80">
        {toasts.map((t) => {
          const I = Icona[t.tipo]
          return (
            <div key={t.id} className={`flex items-start gap-2 border rounded-lg px-4 py-3 shadow ${stile[t.tipo]}`}>
              <I className="w-5 h-5 mt-0.5 flex-shrink-0" />
              <span className="text-sm flex-1">{t.messaggio}</span>
              <button onClick={() => remove(t.id)} className="opacity-60 hover:opacity-100">
                <X className="w-4 h-4" />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast deve essere usato dentro ToastProvider')
  return ctx
}
