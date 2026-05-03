const accents = {
  brand:   { bg: "bg-brand-500/15",   text: "text-brand-400",   border: "border-brand-500/20" },
  emerald: { bg: "bg-emerald-500/15", text: "text-emerald-400", border: "border-emerald-500/20" },
  amber:   { bg: "bg-amber-500/15",   text: "text-amber-400",   border: "border-amber-500/20" },
  violet:  { bg: "bg-violet-500/15",  text: "text-violet-400",  border: "border-violet-500/20" },
  red:     { bg: "bg-red-500/15",     text: "text-red-400",     border: "border-red-500/20" },
};

export default function StatCard({ title, value, note, icon: Icon, accent = "brand", trend }) {
  const a = accents[accent] ?? accents.brand;
  return (
    <div className="glass-card p-5 flex flex-col gap-3 animate-slide-up">
      <div className="flex items-start justify-between">
        <p className="text-sm font-medium text-slate-400">{title}</p>
        {Icon && <div className={`rounded-xl border p-2.5 ${a.bg} ${a.border}`}><Icon size={18} className={a.text} /></div>}
      </div>
      <p className="text-3xl font-bold tracking-tight text-white">{value}</p>
      <div className="flex items-center gap-2">
        {trend !== undefined && trend !== null && (
          <span className={`text-xs font-semibold ${trend >= 0 ? "text-emerald-400" : "text-red-400"}`}>
            {trend >= 0 ? "▲" : "▼"} {Math.abs(trend)}%
          </span>
        )}
        {note && <p className="text-xs text-slate-500">{note}</p>}
      </div>
    </div>
  );
}
