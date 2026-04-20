import toast from 'react-hot-toast'

/**
 * Thin wrapper around react-hot-toast with DSANext-specific helpers.
 * Usage: const { success, error, info, promise } = useToast()
 */
export function useToast() {
  return {
    success: (msg)         => toast.success(msg),
    error:   (msg)         => toast.error(msg),
    info:    (msg)         => toast(msg, { icon: 'ℹ️' }),
    loading: (msg)         => toast.loading(msg),
    dismiss: (id)          => toast.dismiss(id),
    promise: (prom, msgs)  => toast.promise(prom, msgs),
  }
}
