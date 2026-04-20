import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    globals:     true,
    environment: 'jsdom',
    setupFiles:  ['./src/__tests__/setup.js'],
    coverage: {
      provider:    'v8',
      reporter:    ['text', 'json', 'html'],
      include:     ['src/**/*.{js,jsx}'],
      exclude:     ['src/__tests__/**', 'src/main.jsx'],
      thresholds: {
        lines:     70,
        functions: 70,
        branches:  65,
        statements:70,
      },
    },
  },
  resolve: {
    alias: {
      '@':           resolve(__dirname, './src'),
      '@components': resolve(__dirname, './src/components'),
      '@features':   resolve(__dirname, './src/features'),
      '@hooks':      resolve(__dirname, './src/hooks'),
      '@lib':        resolve(__dirname, './src/lib'),
    },
  },
})
