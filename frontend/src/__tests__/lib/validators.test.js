import { describe, it, expect } from 'vitest'
import {
  validateEmail,
  validatePassword,
  validateUsername,
  validateRequired,
  validateUrl,
  validateMinLength,
  validateMaxLength,
} from '../../lib/validators'

describe('validateEmail', () => {
  it('returns empty string for valid email', () => {
    expect(validateEmail('user@example.com')).toBe('')
    expect(validateEmail('user.name+tag@domain.co.uk')).toBe('')
  })
  it('returns error for empty input', () => {
    expect(validateEmail('')).not.toBe('')
    expect(validateEmail(null)).not.toBe('')
  })
  it('returns error for invalid format', () => {
    expect(validateEmail('not-an-email')).not.toBe('')
    expect(validateEmail('@domain.com')).not.toBe('')
    expect(validateEmail('user@')).not.toBe('')
  })
})

describe('validatePassword', () => {
  it('returns empty string for strong password', () => {
    expect(validatePassword('Secure@123')).toBe('')
    expect(validatePassword('MyP@ssw0rd!')).toBe('')
  })
  it('returns error for too short password', () => {
    expect(validatePassword('Sh@1')).not.toBe('')
  })
  it('returns error for missing uppercase', () => {
    expect(validatePassword('secure@123')).not.toBe('')
  })
  it('returns error for missing lowercase', () => {
    expect(validatePassword('SECURE@123')).not.toBe('')
  })
  it('returns error for missing number', () => {
    expect(validatePassword('Secure@abc')).not.toBe('')
  })
  it('returns error for missing special character', () => {
    expect(validatePassword('Secure1234')).not.toBe('')
  })
  it('returns error for empty input', () => {
    expect(validatePassword('')).not.toBe('')
  })
})

describe('validateUsername', () => {
  it('returns empty string for valid username', () => {
    expect(validateUsername('johndoe')).toBe('')
    expect(validateUsername('john_doe_123')).toBe('')
    expect(validateUsername('abc')).toBe('')
  })
  it('returns error for too short username', () => {
    expect(validateUsername('ab')).not.toBe('')
  })
  it('returns error for too long username', () => {
    expect(validateUsername('a'.repeat(51))).not.toBe('')
  })
  it('returns error for username with spaces', () => {
    expect(validateUsername('john doe')).not.toBe('')
  })
  it('returns error for username with special chars', () => {
    expect(validateUsername('john-doe')).not.toBe('')
    expect(validateUsername('john@doe')).not.toBe('')
  })
})

describe('validateRequired', () => {
  it('returns empty string for non-empty value', () => {
    expect(validateRequired('some value')).toBe('')
    expect(validateRequired(0)).toBe('')
  })
  it('returns error for empty string', () => {
    expect(validateRequired('')).not.toBe('')
    expect(validateRequired('   ')).not.toBe('')
  })
  it('returns error for null/undefined', () => {
    expect(validateRequired(null)).not.toBe('')
    expect(validateRequired(undefined)).not.toBe('')
  })
  it('includes field name in error message', () => {
    expect(validateRequired('', 'Email')).toContain('Email')
  })
})

describe('validateUrl', () => {
  it('returns empty string for empty input (optional field)', () => {
    expect(validateUrl('')).toBe('')
    expect(validateUrl(null)).toBe('')
  })
  it('returns empty string for valid https URL', () => {
    expect(validateUrl('https://leetcode.com/problems/two-sum/')).toBe('')
  })
  it('returns empty string for valid http URL', () => {
    expect(validateUrl('http://example.com')).toBe('')
  })
  it('returns error for invalid URL', () => {
    expect(validateUrl('not a url')).not.toBe('')
    expect(validateUrl('ftp://invalid')).not.toBe('')
  })
})

describe('validateMinLength', () => {
  it('returns empty string when length is sufficient', () => {
    expect(validateMinLength('hello', 3)).toBe('')
    expect(validateMinLength('hello', 5)).toBe('')
  })
  it('returns error when too short', () => {
    expect(validateMinLength('hi', 3)).not.toBe('')
  })
  it('returns empty string for null (field is optional)', () => {
    expect(validateMinLength(null, 3)).toBe('')
  })
})

describe('validateMaxLength', () => {
  it('returns empty string when within limit', () => {
    expect(validateMaxLength('hello', 10)).toBe('')
  })
  it('returns error when too long', () => {
    expect(validateMaxLength('a'.repeat(101), 100)).not.toBe('')
  })
})
