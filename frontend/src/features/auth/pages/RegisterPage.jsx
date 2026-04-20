import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../../hooks/useAuth'
import Button from '../../../components/ui/Button'
import Input  from '../../../components/ui/Input'
import { validateEmail, validatePassword, validateUsername, validateRequired } from '../../../lib/validators'

export default function RegisterPage() {
  const { register, isLoading, error, dismissError } = useAuth()

  const [form, setForm] = useState({
    username: '', email: '', password: '', confirmPassword: '', fullName: '',
  })
  const [errors, setErrors] = useState({})

  const validate = () => {
    const e = {}
    const nameErr  = validateRequired(form.fullName, 'Full name')
    const userErr  = validateUsername(form.username)
    const emailErr = validateEmail(form.email)
    const passErr  = validatePassword(form.password)
    if (nameErr)  e.fullName = nameErr
    if (userErr)  e.username = userErr
    if (emailErr) e.email    = emailErr
    if (passErr)  e.password = passErr
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    dismissError()
    if (!validate()) return
    const { confirmPassword, ...payload } = form
    await register(payload)
  }

  const handleChange = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-brand-50 dark:from-slate-950 dark:to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">

        <div className="text-center mb-8">
          <div className="text-5xl mb-3">⚡</div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">DSANext</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Start your DSA journey today</p>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-card border border-slate-200 dark:border-slate-700 p-8">
          <h2 className="text-xl font-semibold text-slate-800 dark:text-slate-100 mb-6">Create your account</h2>

          {error && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-sm text-red-700 dark:text-red-400 flex items-start gap-2">
              <span>⚠</span><span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            <Input label="Full name"    type="text"     placeholder="John Doe"         value={form.fullName}        onChange={handleChange('fullName')}        error={errors.fullName}        required autoFocus />
            <Input label="Username"     type="text"     placeholder="johndoe"          value={form.username}        onChange={handleChange('username')}        error={errors.username}        required />
            <Input label="Email"        type="email"    placeholder="you@example.com"  value={form.email}           onChange={handleChange('email')}           error={errors.email}           required />
            <Input label="Password"     type="password" placeholder="Min 8 chars"      value={form.password}        onChange={handleChange('password')}        error={errors.password}        required hint="Must contain uppercase, lowercase, number, and special character" />
            <Input label="Confirm password" type="password" placeholder="••••••••"    value={form.confirmPassword} onChange={handleChange('confirmPassword')} error={errors.confirmPassword} required />

            <Button type="submit" fullWidth size="lg" loading={isLoading} className="mt-2">
              Create account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-600 dark:text-brand-400 hover:underline font-medium">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
