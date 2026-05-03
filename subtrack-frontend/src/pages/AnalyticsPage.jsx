import { useEffect, useState } from "react";
import { AlertTriangle, CheckCircle2, Lightbulb, TrendingDown, TrendingUp, Zap } from "lucide-react";
import { analyticsAPI } from "../services/api";
import Card from "../components/UI/Card";
import Badge from "../components/UI/Badge";

function DonutChart({ data }) {
  if (!data?.length) return <p className="text-sm text-slate-500">No data</p>;
  const total = data.reduce((a, d) => a + d.percentage, 0);
  const colors = ["#6366f1","#8b5cf6","#10b981","#f59e0b","#64748b","#ef4444"];
  const r = 60, cx = 80, cy = 80, circ = 2 * Math.PI * r;
  let cum = 0;
  const segs = data.map((d, i) => {
    const pct = total > 0 ? d.percentage / total : 0;
    const offset = circ - pct * circ;
    const rot = (cum / total) * 360 - 90;
    cum += d.percentage;
    return { ...d, offset, rot, color: colors[i % colors.length] };
  });
  return (
    <div className="flex flex-col items-center gap-6 sm:flex-row">
      <svg width="160" height="160" className="shrink-0">
        {segs.map((s, i) => (
          <circle key={i} cx={cx} cy={cy} r={r} fill="none" stroke={s.color} strokeWidth="20"
            strokeDasharray={`${circ - s.offset} ${s.offset}`} strokeDashoffset={circ / 4}
            transform={`rotate(${s.rot} ${cx} ${cy})`} />
        ))}
        <circle cx={cx} cy={cy} r={50} fill="#1e293b" />
        <text x={cx} y={cy - 4} textAnchor="middle" fill="white" fontSize="11" fontWeight="700">Total</text>
        <text x={cx} y={cy + 12} textAnchor="middle" fill="#64748b" fontSize="9">spend</text>
      </svg>
      <div className="flex-1 space-y-2.5">
        {segs.map((s, i) => (
          <div key={i} className="flex items-center gap-3">
            <div className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ background: s.color }} />
            <span className="flex-1 text-sm text-slate-400">{s.category}</span>
            <span className="text-sm font-semibold text-white">₹{parseFloat(s.amount).toLocaleString("en-IN", { maximumFractionDigits: 0 })}</span>
            <span className="text-xs font-medium text-slate-500">{s.percentage}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState(null);
  const [insights,  setInsights]  = useState(null);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    Promise.all([analyticsAPI.get(), analyticsAPI.getInsights()])
      .then(([a, i]) => { setAnalytics(a.data); setInsights(i.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const fmt = (n) => n != null ? `₹${parseFloat(n).toLocaleString("en-IN", { maximumFractionDigits: 0 })}` : "—";

  if (loading) return <div className="flex h-64 items-center justify-center text-slate-500">Loading analytics...</div>;

  const insightCards = [
    insights?.unusedSubscriptions?.length && {
      icon: AlertTriangle, color: "text-amber-400", bg: "bg-amber-500/10 border-amber-500/20",
      title: `${insights.unusedSubscriptions.length} unused subscription(s)`,
      desc: `Costing ${fmt(insights.totalWastedMonthly)}/month with no usage in 30+ days.`,
    },
    ...(insights?.savingTips?.map((tip) => ({
      icon: Lightbulb, color: "text-brand-400", bg: "bg-brand-500/10 border-brand-500/20",
      title: "Saving Tip", desc: tip,
    })) ?? []),
    ...(insights?.spendingAlerts?.map((alert) => ({
      icon: Zap, color: "text-violet-400", bg: "bg-violet-500/10 border-violet-500/20",
      title: "Spending Alert", desc: alert,
    })) ?? []),
  ].filter(Boolean);

  return (
    <div className="space-y-6 animate-slide-up">
      <div><h1 className="page-title">Analytics</h1><p className="page-subtitle">Understand your spend patterns and find savings.</p></div>

      {/* KPIs */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          { label: "Monthly Spend",     value: fmt(analytics?.totalMonthlySpend),    icon: TrendingUp,   color: "text-brand-400"   },
          { label: "Annual Projection", value: fmt(analytics?.totalYearlyProjection), icon: TrendingUp,   color: "text-violet-400"  },
          { label: "Potential Savings", value: fmt(analytics?.potentialSavings),      icon: TrendingDown, color: "text-amber-400"   },
          { label: "At Risk Subs",      value: analytics?.atRiskSubscriptions ?? 0,   icon: AlertTriangle,color: "text-red-400"     },
        ].map((k) => (
          <Card key={k.label} className="p-5">
            <div className="flex items-start justify-between">
              <p className="text-xs font-medium text-slate-500">{k.label}</p>
              <k.icon size={15} className={k.color} />
            </div>
            <p className="mt-3 text-2xl font-bold text-white">{k.value}</p>
          </Card>
        ))}
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <h3 className="mb-4 font-semibold text-white">Spend Trend</h3>
          {analytics?.monthlyTrend?.length ? (
            <div className="space-y-2">
              {analytics.monthlyTrend.map((m) => {
                const max = Math.max(...analytics.monthlyTrend.map((x) => parseFloat(x.amount)));
                const pct = max > 0 ? (parseFloat(m.amount) / max) * 100 : 0;
                return (
                  <div key={m.month} className="flex items-center gap-4">
                    <span className="w-16 shrink-0 text-xs text-slate-500">{m.month}</span>
                    <div className="flex-1 h-5 overflow-hidden rounded-full bg-surface-border">
                      <div className="h-full rounded-full bg-brand-gradient transition-all duration-700" style={{ width: `${pct}%` }} />
                    </div>
                    <span className="w-20 text-right text-xs font-semibold text-white">{fmt(m.amount)}</span>
                  </div>
                );
              })}
            </div>
          ) : <p className="text-sm text-slate-500">No payment history yet.</p>}
        </Card>

        <Card>
          <h3 className="mb-6 font-semibold text-white">Category Breakdown</h3>
          <DonutChart data={analytics?.categoryBreakdown} />
        </Card>
      </div>

      {/* Unused subscriptions */}
      {insights?.unusedSubscriptions?.length > 0 && (
        <Card>
          <div className="mb-4 flex items-center justify-between">
            <h3 className="font-semibold text-white">Unused Subscriptions</h3>
            <div className="rounded-xl border border-red-500/20 bg-red-500/10 px-3 py-1.5">
              <span className="text-sm font-semibold text-red-400">Wasting {fmt(insights.totalWastedMonthly)}/mo</span>
            </div>
          </div>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {insights.unusedSubscriptions.map((s) => (
              <div key={s.subscriptionId} className="flex items-center gap-4 rounded-xl border border-amber-500/20 bg-amber-500/5 p-4">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-500/20 text-lg font-bold text-amber-400">{s.name[0]}</div>
                <div className="flex-1 min-w-0">
                  <p className="truncate text-sm font-semibold text-white">{s.name}</p>
                  <p className="text-xs text-slate-500">Last used: {s.lastUsedAt}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-bold text-amber-400">₹{parseFloat(s.amount).toLocaleString("en-IN", { maximumFractionDigits: 0 })}/mo</p>
                  <Badge label="At Risk" variant="AT_RISK" />
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Smart insights */}
      {insightCards.length > 0 && (
        <Card>
          <h3 className="mb-4 font-semibold text-white">Smart Insights</h3>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {insightCards.map((ins, i) => (
              <div key={i} className={`rounded-xl border p-4 ${ins.bg}`}>
                <div className="flex items-start gap-3">
                  <ins.icon size={18} className={`mt-0.5 shrink-0 ${ins.color}`} />
                  <div>
                    <p className="text-sm font-semibold text-white">{ins.title}</p>
                    <p className="mt-1 text-xs text-slate-400 leading-relaxed">{ins.desc}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
