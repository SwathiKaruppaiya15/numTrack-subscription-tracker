import { Link } from "react-router-dom";
export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-surface text-center">
      <p className="text-6xl font-black text-brand-500">404</p>
      <p className="text-xl font-semibold text-white">Page not found</p>
      <p className="text-sm text-slate-400">The page you're looking for doesn't exist.</p>
      <Link to="/dashboard" className="btn-primary mt-2">Go to Dashboard</Link>
    </div>
  );
}
