/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: { 50: "#F0FDFA", 600: "#0F766E", 700: "#115E59" },
        accent: { 500: "#F59E0B" },
        success: { 50: "#F0FDF4", 700: "#15803D" },
        warning: { 50: "#FFFBEB", 700: "#B45309" },
        danger: { 50: "#FEF2F2", 600: "#DC2626" },
        info: { 50: "#EFF6FF", 600: "#2563EB" },
        ink: "#0F172A", muted: "#64748B", line: "#E2E8F0", canvas: "#F8FAFC",
        // Keep existing legacy tokens in case they're used elsewhere
        brand: { DEFAULT: '#E14A29' },
        action: { DEFAULT: '#1A1A1A' },
        surface: { DEFAULT: '#FFFFFF' },
        border: { DEFAULT: '#E5E7EB' },
        error: { DEFAULT: '#EF4444' }
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans Devanagari', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      }
    },
  },
  plugins: [],
}
