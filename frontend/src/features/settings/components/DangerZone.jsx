import { useState } from 'react'
import { useDispatch } from 'react-redux'
import { logout } from '../../auth/authSlice'
import { useNavigate } from 'react-router-dom'
import api    from '../../../lib/axios'
import Button from '../../../components/ui/Button'
import Modal  from '../../../components/ui/Modal'
import Input  from '../../../components/ui/Input'
import { useToast } from '../../../hooks/useToast'

export default function DangerZone() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const toast     = useToast()

  const [modalOpen,   setModalOpen]   = useState(false)
  const [confirmation,setConfirmation]= useState('')
  const [deleting,    setDeleting]    = useState(false)

  const CONFIRM_PHRASE = 'delete my account'
  const isConfirmed = confirmation.toLowerCase() === CONFIRM_PHRASE

  const handleDelete = async () => {
    if (!isConfirmed) return
    setDeleting(true)
    try {
      await api.delete('/users/account')
      dispatch(logout())
      navigate('/login', { replace: true })
      toast.success('Your account has been deleted.')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to delete account')
      setDeleting(false)
    }
  }

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-red-200 dark:border-red-900/50 p-6">
      <h2 className="text-base font-semibold text-red-700 dark:text-red-400 mb-1">
        Danger zone
      </h2>
      <p className="text-xs text-slate-500 dark:text-slate-400 mb-5">
        Irreversible actions that permanently affect your account.
      </p>

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-900/30">
        <div>
          <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">Delete account</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Permanently delete your account and all associated data — progress, notes, bookmarks. This cannot be undone.
          </p>
        </div>
        <Button variant="danger" size="sm" className="shrink-0" onClick={() => setModalOpen(true)}>
          Delete account
        </Button>
      </div>

      <Modal
        isOpen={modalOpen}
        onClose={() => { setModalOpen(false); setConfirmation('') }}
        title="Delete your account"
        size="sm"
        showFooter
        confirmLabel="Permanently delete account"
        confirmVariant="danger"
        onConfirm={handleDelete}
        confirmLoading={deleting}
        cancelLabel="Cancel"
      >
        <div className="space-y-4">
          <div className="p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
            <p className="text-sm font-semibold text-red-700 dark:text-red-400 mb-1">⚠ This action is permanent</p>
            <p className="text-xs text-red-600 dark:text-red-400">
              Deleting your account will immediately remove all your progress, notes, bookmarks, and settings. You cannot undo this.
            </p>
          </div>

          <div>
            <p className="text-sm text-slate-600 dark:text-slate-400 mb-2">
              Type <strong className="text-slate-800 dark:text-slate-200 font-mono">delete my account</strong> to confirm:
            </p>
            <Input
              value={confirmation}
              onChange={e => setConfirmation(e.target.value)}
              placeholder="delete my account"
              autoFocus
            />
          </div>
        </div>
      </Modal>
    </div>
  )
}
