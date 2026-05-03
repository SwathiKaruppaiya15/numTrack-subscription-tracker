const V = {
  ACTIVE:    "bg-emerald-500/15 text-emerald-400 border border-emerald-500/20",
  CANCELLED: "bg-slate-500/15 text-slate-400 border border-slate-500/20",
  AT_RISK:   "bg-amber-500/15 text-amber-400 border border-amber-500/20",
  SUCCESS:   "bg-emerald-500/15 text-emerald-400 border border-emerald-500/20",
  FAILED:    "bg-red-500/15 text-red-400 border border-red-500/20",
  PENDING:   "bg-blue-500/15 text-blue-400 border border-blue-500/20",
  MONTHLY:   "bg-brand-500/15 text-brand-400 border border-brand-500/20",
  YEARLY:    "bg-violet-500/15 text-violet-400 border border-violet-500/20",
};
export default function Badge({ label, variant = "default" }) {
  return <span className={`badge ${V[variant?.toUpperCase()] ?? "bg-slate-500/15 text-slate-400 border border-slate-500/20"}`}>{label}</span>;
}
