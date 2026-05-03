import { useEffect, useState } from "react";
import { AlertTriangle, ArrowRight, Bell, CalendarDays, CreditCard, TrendingDown, TrendingUp, Wallet } from "lucide-react";
import { Link } from "react-router-dom";
import { analyticsAPI } from "../services/api";
import StatCard from "../components/UI/StatCard";
import Badge from "../components/UI/Badge";
import Card from "../components/UI/Card";

// ── Mini bar chart ────────────────────────────────────────────
function SpendChart({ trend }) {
  if (!trend?.length) return <div className="h-32 flex items-center justify-center text-sm text-slate-500">No data yet</div>;
  const max = Math.max(...trend.map((d) => parseFloat(d.amount)));
  return (
    <div className="flex h-32 items-end gap-2">
      {trend.map((d, i) => {
        const pct = max > 0 ? (parseFloat(d.amount) / max) * 100 : 0;
        const isLast = i === trend.length - 1;
        return (
          <div key={d.month} className="group flex flex-1 flex-col items-center gap-1.5">
            <div className="relative w-full" style={{ height: "100px" }}>
              <div className={`absolute bottom-0 w-full rounded-t-md transition-all duration-700 ${isLast ? "bg-brand-gradient shadow-glow-sm" : "bg-surface-border group-hover:bg-brand-500/40"}`} style={{ height: `${pct}%` }} />
            </div>
            <span className={`text-[10px] font-medium ${isLast ? "text-brand-400" : "text-slate-600"}`}>{d.month?.slice(5)}</span>
          </div>
        );
      })}
    </div>
  );
}

export default function DashboardPage() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    analyticsAPI.get()
      .then((r) => setAnalytics(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const fmt = (n) => n != null ? `₹${parseFloat(n).toLocaleString("en-IN", { maximumFractionDigits: 0 })}` : "—";

  const stats = analytics ? [
    { title: "Monthly Spend",        value: fmt(analytics.totalMonthlySpend),    note: "This month",          icon: Wallet,      accent: "brand"   },
    { title: "Active Subscriptions", value: analytics.activeSubscriptions ?? 0,  note: "Currently active",    icon: CreditCard,  accent: "emerald" },
    { title: "Upcoming Bills (30d)", value: analytics.upcomingBills?.length ?? 0,note: "Next 30 days",        icon: CalendarDays,accent: "amber"   },
    { title: "Potential Savings",    value: fmt(analytics.potentialSavings),      note: "From unused subs",    icon: TrendingDown,accent: "violet"  },
  ] : [];

  if (loading) return (
    <div className="space-y-6 animate-pulse">
      <div className="h-8 w-64 rounded-xl bg-surface-card" />
      <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">{[...Array(4)].map((_, i) => <div key={i} className="h-32 rounded-2xl bg-surface-card" />)}</div>
    </div>
  );

  return (
    <div className="space-y-6 animate-slide-up">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Your subscription snapshot.</p>
        </div>
        {analytics?.atRiskSubscriptions > 0 && (
          <div className="flex items-center gap-2 rounded-xl border border-amber-500/20 bg-amber-500/10 px-4 py-2.5">
            <AlertTriangle size={15} className="text-amber-400" />
            <span className="text-sm font-medium text-amber-300">{analytics.atRiskSubscriptions} subscription{analytics.atRiskSubscriptions > 1 ? "s" : ""} at risk</span>
          </div>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((s) => <StatCard key={s.title} {...s} />)}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <div><h3 className="font-semibold text-white">Spend Trend</h3><p className="text-xs text-slate-500">Last 6 months</p></div>
            <div className="flex items-center gap-1.5 rounded-lg border border-brand-500/20 bg-brand-500/10 px-2.5 py-1">
              <TrendingUp size={13} className="text-brand-400" />
              <span className="text-xs font-semibold text-brand-400">Monthly</span>
            </div>
          </div>
          <SpendChart trend={analytics?.monthlyTrend} />
        </Card>

        <Card>
          <h3 className="mb-4 font-semibold text-white">Category Breakdown</h3>
          {analytics?.categoryBreakdown?.length ? (
            <div className="space-y-3">
              {analytics.categoryBreakdown.map((c) => (
                <div key={c.category}>
                  <div className="mb-1 flex items-center justify-between text-xs">
                    <span className="text-slate-400">{c.category}</span>
                    <span className="font-semibold text-white">{fmt(c.amount)}</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-surface-border">
                    <div className="h-full rounded-full bg-brand-gradient transition-all duration-700" style={{ width: `${c.percentage}%` }} />
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-500">No payment data yet.</p>}
        </Card>
      </div>

      {/* Upcoming bills */}
      <Card>
        <div className="mb-4 flex items-center justify-between">
          <h3 className="font-semibold text-white">Upcoming Bills</h3>
          <Link to="/subscriptions" className="flex items-center gap-1 text-xs text-brand-400 hover:text-brand-300 transition">View all <ArrowRight size={12} /></Link>
        </div>
        {analytics?.upcomingBills?.length ? (
          <div className="space-y-3">
            {analytics.upcomingBills.slice(0, 5).map((b) => (
              <div key={b.subscriptionId} className="flex items-center gap-3 rounded-xl border border-surface-border bg-surface/50 p-3 transition hover:border-brand-500/30 hover:bg-surface-hover">
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-brand-500/20 text-sm font-bold text-brand-400">{b.name[0]}</div>
                <div className="flex-1 min-w-0">
                  <p className="truncate text-sm font-medium text-white">{b.name}</p>
                  <p className="text-xs text-slate-500">{b.category}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-semibold text-white">{fmt(b.amount)}</p>
                  <p className={`text-xs font-medium ${b.daysUntilBilling <= 3 ? "text-red-400" : b.daysUntilBilling <= 7 ? "text-amber-400" : "text-slate-500"}`}>
                    {b.daysUntilBilling <= 0 ? "Today" : `in ${b.daysUntilBilling}d`}
                  </p>
                </div>
              </div>
            ))}
          </div>
        ) : <p className="text-sm text-slate-500">No upcoming bills in the next 30 days.</p>}
      </Card>

      {/* Insight banner */}
      {analytics?.potentialSavings > 0 && (
        <Card className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-brand-500/20 bg-brand-500/5">
          <div className="flex items-start gap-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-gradient shadow-glow-sm">
              <TrendingDown size={18} className="text-white" />
            </div>
            <div>
              <p className="font-semibold text-white">You could save {fmt(analytics.potentialSavings)}/month</p>
              <p className="mt-0.5 text-sm text-slate-400">{analytics.atRiskSubscriptions} unused subscription(s) detected. Consider cancelling them.</p>
            </div>
          </div>
          <Link to="/analytics" className="btn-primary shrink-0 whitespace-nowrap">View Insights <ArrowRight size={15} /></Link>
        </Card>
      )}
    </div>
  );
}
