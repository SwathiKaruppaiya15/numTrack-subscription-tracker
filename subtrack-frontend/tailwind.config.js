/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef2ff", 100: "#e0e7ff", 200: "#c7d2fe",
          300: "#a5b4fc", 400: "#818cf8", 500: "#6366f1",
          600: "#4f46e5", 700: "#4338ca", 800: "#3730a3", 900: "#312e81",
        },
        surface: {
          DEFAULT: "#0f172a", card: "#1e293b",
          hover: "#263348", border: "#334155",
        },
      },
      fontFamily: { sans: ["Inter", "system-ui", "sans-serif"] },
      boxShadow: {
        card: "0 1px 3px rgba(0,0,0,.12),0 1px 2px rgba(0,0,0,.08)",
        glow: "0 0 20px rgba(99,102,241,.3)",
        "glow-sm": "0 0 10px rgba(99,102,241,.2)",
      },
      backgroundImage: {
        "brand-gradient": "linear-gradient(135deg,#6366f1 0%,#8b5cf6 100%)",
      },
      animation: {
        "fade-in":  "fadeIn .25s ease-out",
        "slide-up": "slideUp .3s ease-out",
      },
      keyframes: {
        fadeIn:  { from: { opacity: 0 }, to: { opacity: 1 } },
        slideUp: { from: { opacity: 0, transform: "translateY(12px)" }, to: { opacity: 1, transform: "translateY(0)" } },
      },
    },
  },
  plugins: [],
};
