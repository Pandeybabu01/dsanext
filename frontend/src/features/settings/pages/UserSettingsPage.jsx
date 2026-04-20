import { useState } from 'react'
import clsx from 'clsx'
import ProfileForm       from '../components/ProfileForm'
import SecurityForm      from '../components/SecurityForm'
import AppearanceForm    from '../components/AppearanceForm'
import NotificationPrefs from '../components/NotificationPrefs'
import PlatformConnect   from '../components/PlatformConnect'
import DangerZone        from '../components/DangerZone'

const TABS = [
  { id: 'profile',      label: 'Profile',       icon: '👤' },
  { id: 'security',     label: 'Security',      icon: '🔒' },
  { id: 'appearance',   label: 'Appearance',    icon: '🎨' },
  { id: 'notifications',label: 'Notifications', icon: '🔔' },
  { id: 'platforms',    label: 'Platforms',     icon: '🌐' },
  { id: 'danger',       label: 'Danger zone',   icon: '⚠️' },
]

export default function UserSettingsPage() {
  const [activeTab, setActiveTab] = useState('profile')

  return (
    <div className="max-w-3xl">
      {/* Page header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Settings</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
          Manage your account, preferences, and connected platforms
        </p>
      </div>

      <div className="flex flex-col lg:flex-row gap-6">

        {/* Tab navigation sidebar */}
        <nav className="lg:w-48 shrink-0">
          <div className="flex lg:flex-col gap-1 overflow-x-auto lg:overflow-visible pb-2 lg:pb-0">
            {TABS.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={clsx(
                  'flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 whitespace-nowrap',
                  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500',
                  activeTab === tab.id
                    ? 'bg-brand-50 dark:bg-brand-900/20 text-brand-700 dark:text-brand-400'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100',
                  tab.id === 'danger' && activeTab !== 'danger' && 'hover:text-red-600 dark:hover:text-red-400'
                )}
              >
                <span className="text-base leading-none">{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </div>
        </nav>

        {/* Tab content */}
        <div className="flex-1 min-w-0 space-y-5 animate-fade-in">
          {activeTab === 'profile'       && <ProfileForm />}
          {activeTab === 'security'      && <SecurityForm />}
          {activeTab === 'appearance'    && <AppearanceForm />}
          {activeTab === 'notifications' && <NotificationPrefs />}
          {activeTab === 'platforms'     && <PlatformConnect />}
          {activeTab === 'danger'        && <DangerZone />}
        </div>

      </div>
    </div>
  )
}
