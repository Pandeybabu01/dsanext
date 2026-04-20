import { NavLink } from 'react-router-dom'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard',  label: 'Dashboard',  icon: '📊' },
  { to: '/problems',   label: 'Problems',   icon: '💻' },
  { to: '/progress',   label: 'Progress',   icon: '📈' },
  { to: '/notes',      label: 'Notes',      icon: '📝' },
  { to: '/bookmarks',  label: 'Bookmarks',  icon: '🔖' },
  { to: '/analytics',  label: 'Analytics',  icon: '📉' },
  { to: '/settings',   label: 'Settings',   icon: '⚙️' },
]

export default function Sidebar({ isOpen, onClose }) {
  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-20 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside className={clsx(
        'fixed top-16 left-0 bottom-0 w-64 z-20',
        'bg-white dark:bg-slate-900',
        'border-r border-slate-200 dark:border-slate-700',
        'flex flex-col transition-transform duration-300',
        'lg:translate-x-0',
        isOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {navItems.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onClose}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                isActive
                  ? 'bg-brand-50 dark:bg-brand-900/20 text-brand-700 dark:text-brand-400'
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100'
              )}
            >
              <span className="text-base w-5 flex justify-center">{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Footer */}
        <div className="px-4 py-3 border-t border-slate-100 dark:border-slate-700">
          <p className="text-xs text-slate-400 dark:text-slate-600 text-center">
            DSANext v1.0.0
          </p>
        </div>
      </aside>
    </>
  )
}
