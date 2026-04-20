import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../../hooks/useAuth'
import Button from '../../../components/ui/Button'
import Input  from '../../../components/ui/Input'
import { validateEmail, validateRequired } from '../../../lib/validators'

export default function LoginPage() {
  const { login, isLoading, error, dismissError } = useAuth()

  const [form, setForm]     = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})

  const validate = () => {
    const e = {}
    const emailErr = validateEmail(form.email)
    const passErr  = validateRequired(form.password, 'Password')
    if (emailErr) e.email    = emailErr
    if (passErr)  e.password = passErr
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    dismissError()
    if (!validate()) return
    await login(form)
  }

  const handleChange = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    if (errors[field]) setErrors(er => ({ ...er, [field]: '' }))
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-brand-50 dark:from-slate-950 dark:to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="text-5xl mb-3">⚡</div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">DSANext</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Master DSA. Land the job.</p>
        </div>

        {/* Card */}
        <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-card border border-slate-200 dark:border-slate-700 p-8">
          <h2 className="text-xl font-semibold text-slate-800 dark:text-slate-100 mb-6">Welcome back</h2>

          {error && (
            <div className="mb-4 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-sm text-red-700 dark:text-red-400 flex items-start gap-2">
              <span>⚠</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            <Input
              label="Email address"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange('email')}
              error={errors.email}
              required
              autoComplete="email"
              autoFocus
            />

            <div>
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={handleChange('password')}
                error={errors.password}
                required
                autoComplete="current-password"
              />
            </div>

            <Button
              type="submit"
              fullWidth
              size="lg"
              loading={isLoading}
              className="mt-6"
            >
              Sign in
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
            Don't have an account?{' '}
            <Link to="/register" className="text-brand-600 dark:text-brand-400 hover:underline font-medium">
              Create one free
            </Link>
          </p>
        </div>

        {/* Demo credentials hint */}
{/*         <div className="mt-4 p-3 rounded-lg bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 text-xs text-blue-700 dark:text-blue-400 text-center"> */}
{/*           Admin: <strong>admin@dsanext.com</strong> / <strong>Admin@123</strong> */}
{/*         </div> */}
      </div>
    </div>
  )
}
