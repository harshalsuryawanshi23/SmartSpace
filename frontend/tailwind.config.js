/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#E14A29', // Saffron Red
        },
        action: {
          DEFAULT: '#1A1A1A', // Action Black
        },
        surface: {
          DEFAULT: '#FFFFFF', // Surface White
        },
        border: {
          DEFAULT: '#E5E7EB', // Border Gray
        },
        success: {
          DEFAULT: '#10B981', // Success
        },
        warning: {
          DEFAULT: '#F59E0B', // Warning
        },
        error: {
          DEFAULT: '#EF4444', // Error
        }
      },
      fontFamily: {
        sans: ['Inter', 'Noto Sans Devanagari', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      }
    },
  },
  plugins: [],
}
