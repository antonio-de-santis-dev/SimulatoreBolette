/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'energy-green': '#10b981',
        'energy-blue': '#3b82f6',
        'energy-orange': '#f59e0b',
        'energy-red': '#ef4444',
        'energy-purple': '#8b5cf6',
      }
    },
  },
  plugins: [],
}
