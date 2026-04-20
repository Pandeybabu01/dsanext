// ── Field validators — return error string or empty string ─

export const validateEmail = (email) => {
  if (!email) return 'Email is required'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 'Must be a valid email address'
  return ''
}

export const validatePassword = (password) => {
  if (!password) return 'Password is required'
  if (password.length < 8) return 'Password must be at least 8 characters'
  if (!/(?=.*[a-z])/.test(password)) return 'Must contain at least one lowercase letter'
  if (!/(?=.*[A-Z])/.test(password)) return 'Must contain at least one uppercase letter'
  if (!/(?=.*\d)/.test(password)) return 'Must contain at least one number'
  if (!/(?=.*[@$!%*?&])/.test(password)) return 'Must contain at least one special character (@$!%*?&)'
  return ''
}

export const validateUsername = (username) => {
  if (!username) return 'Username is required'
  if (username.length < 3) return 'Username must be at least 3 characters'
  if (username.length > 50) return 'Username must not exceed 50 characters'
  if (!/^[a-zA-Z0-9_]+$/.test(username)) return 'Only letters, numbers, and underscores allowed'
  return ''
}

export const validateRequired = (value, fieldName = 'This field') => {
  if (!value || (typeof value === 'string' && !value.trim())) {
    return `${fieldName} is required`
  }
  return ''
}

export const validateUrl = (url) => {
  if (!url) return ''
  try {
    new URL(url)
    return ''
  } catch {
    return 'Must be a valid URL (including https://)'
  }
}

export const validateMinLength = (value, min, fieldName = 'Field') => {
  if (value && value.length < min) return `${fieldName} must be at least ${min} characters`
  return ''
}

export const validateMaxLength = (value, max, fieldName = 'Field') => {
  if (value && value.length > max) return `${fieldName} must not exceed ${max} characters`
  return ''
}
