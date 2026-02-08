export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'], // DM Sans would be closer but Inter is safe
        display: ['Inter', 'sans-serif'],
      },
      colors: {
        primary: {
          50: '#eef2ff',
          100: '#e0e7ff',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4318FF', // Hireism Blue
          700: '#2B36D1', // Darker Blue
          800: '#1e1b4b',
        },
        // Using 'clay' tokens as aliases for the new theme to avoid breaking existing classes immediately,
        // but remapping them to the new aesthetic.
        clay: {
          bg: '#F4F7FE',  // Light Lavender Grey
          card: '#FFFFFF',
          text: '#2B3674', // Navy Blue Text
          subtext: '#A3AED0',
        }
      },
      boxShadow: {
        'clay-card': '0px 18px 40px rgba(112, 144, 176, 0.12)', // Soft diffuse shadow
        'clay-btn': '0px 8px 16px rgba(67, 24, 255, 0.2)',
        'clay-float': '0px 20px 50px rgba(112, 144, 176, 0.2)',
        'clay-inner': 'inset 0px 4px 4px rgba(0, 0, 0, 0.05)',
      },
      borderRadius: {
        '3xl': '20px',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        shine: {
          '0%': { transform: 'translateX(-150%) skewX(-12deg)' },
          '100%': { transform: 'translateX(150%) skewX(-12deg)' },
        },
        'slide-up': {
          '0%': { transform: 'translateY(20px)', opacity: 0 },
          '100%': { transform: 'translateY(0)', opacity: 1 },
        },
        'fade-in': {
          '0%': { opacity: 0 },
          '100%': { opacity: 1 },
        },
        'loading-bar': {
          '0%': { transform: 'translateX(-100%)' },
          '50%': { transform: 'translateX(-20%)' },
          '100%': { transform: 'translateX(0%)' },
        },
        wiggle: {
          '0%, 100%': { transform: 'rotate(-6deg)' },
          '50%': { transform: 'rotate(6deg)' },
        },
        'spin-slow': {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        'fade-in-up': {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        }
      },
      animation: {
        float: 'float 3s ease-in-out infinite',
        shine: 'shine 2.5s infinite',
        'slide-up': 'slide-up 0.8s ease-out forwards',
        'fade-in-delayed': 'fade-in 1s ease-out 0.3s forwards',
        'bounce-slight': 'bounce 3s infinite',
        'loading-bar': 'loading-bar 2.5s ease-out forwards',
        wiggle: 'wiggle 2s ease-in-out infinite',
        'spin-slow': 'spin-slow 12s linear infinite',
        'fade-in-up': 'fade-in-up 0.8s ease-out forwards',
      }
    },
  },
  plugins: [],
}
