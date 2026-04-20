import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { selectUser, updateProfile } from '../../auth/authSlice'
import { useToast } from '../../../hooks/useToast'
import Button from '../../../components/ui/Button'
import Input  from '../../../components/ui/Input'
import api    from '../../../lib/axios'

export default function ProfileForm() {
  const dispatch = useDispatch()
  const user     = useSelector(selectUser)
  const toast    = useToast()

  const [form, setForm] = useState({
    fullName:        user?.fullName        ?? '',
    username:        user?.username        ?? '',
    email:           user?.email           ?? '',
    profileImageUrl: user?.profileImageUrl ?? '',
  })
  const [errors,    setErrors]    = useState({})
  const [saving,    setSaving]    = useState(false)
  const [uploading, setUploading] = useState(false)

  const set = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.fullName.trim()) e.fullName = 'Full name is required'
    if (!form.username.trim()) e.username = 'Username is required'
    if (form.username.length < 3) e.username = 'Username must be at least 3 characters'
    if (!/^[a-zA-Z0-9_]+$/.test(form.username)) e.username = 'Only letters, numbers, and underscores allowed'
    if (!form.email.trim()) e.email = 'Email is required'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Must be a valid email'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      await dispatch(updateProfile(form)).unwrap()
      toast.success('Profile updated successfully')
    } catch (err) {
      toast.error(err || 'Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  // Profile image upload (multipart form)
  const handleImageUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    const allowed = ['image/jpeg', 'image/png', 'image/webp']
    if (!allowed.includes(file.type)) {
      toast.error('Only JPEG, PNG, or WebP images are allowed')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image must be smaller than 5MB')
      return
    }

    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      const res = await api.post('/users/profile/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      const url = res.data.data?.profileImageUrl
      setForm(f => ({ ...f, profileImageUrl: url }))
      await dispatch(updateProfile({ profileImageUrl: url }))
      toast.success('Profile image updated')
    } catch {
      toast.error('Image upload failed')
    } finally {
      setUploading(false)
    }
  }

  const initials = user?.fullName
    ?.split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) ?? 'U'

  return (
    <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 p-6">
      <h2 className="text-base font-semibold text-slate-800 dark:text-slate-200 mb-5">
        Profile information
      </h2>

      {/* Avatar upload */}
      <div className="flex items-center gap-5 mb-6 pb-6 border-b border-slate-100 dark:border-slate-700">
        <div className="relative shrink-0">
          {form.profileImageUrl ? (
            <img
              src={form.profileImageUrl}
              alt="Profile"
              className="w-16 h-16 rounded-full object-cover border-2 border-slate-200 dark:border-slate-600"
            />
          ) : (
            <div className="w-16 h-16 rounded-full bg-brand-600 text-white flex items-center justify-center text-xl font-bold">
              {initials}
            </div>
          )}
          {uploading && (
            <div className="absolute inset-0 rounded-full bg-black/40 flex items-center justify-center">
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            </div>
          )}
        </div>
        <div>
          <p className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Profile picture</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mb-2">JPEG, PNG, or WebP — max 5MB</p>
          <label className="cursor-pointer">
            <span className="inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-medium border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
              {uploading ? 'Uploading…' : '📷 Change photo'}
            </span>
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="hidden"
              onChange={handleImageUpload}
              disabled={uploading}
            />
          </label>
        </div>
      </div>

      {/* Form fields */}
      <div className="space-y-4">
        <div className="grid sm:grid-cols-2 gap-4">
          <Input
            label="Full name"
            value={form.fullName}
            onChange={set('fullName')}
            error={errors.fullName}
            required
            placeholder="John Doe"
          />
          <Input
            label="Username"
            value={form.username}
            onChange={set('username')}
            error={errors.username}
            required
            placeholder="johndoe"
            leftIcon={<span className="text-slate-400 text-sm">@</span>}
          />
        </div>

        <Input
          label="Email address"
          type="email"
          value={form.email}
          onChange={set('email')}
          error={errors.email}
          required
          placeholder="you@example.com"
        />
      </div>

      <div className="flex justify-end mt-5 pt-5 border-t border-slate-100 dark:border-slate-700">
        <Button size="sm" loading={saving} onClick={handleSave}>
          Save profile
        </Button>
      </div>
    </div>
  )
}
