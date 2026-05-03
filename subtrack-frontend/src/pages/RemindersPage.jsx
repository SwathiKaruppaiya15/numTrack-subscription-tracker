import { useEffect, useState } from "react";
import { Bell, CheckCircle2, Clock, Mail, Send } from "lucide-react";
import { reminderAPI } from "../services/api";
import { useDispatch } from "react-redux";
import { showToast } from "../store/store";
import Badge from "../components/UI/Badge";
import Card from "../components/UI/Card";

function Toggle({ enabled, onChange }) {
  return (
    <button onClick={onChange} className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors ${enabled ? "bg-brand-500" : "bg-surface-border"}`}>
      <span className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition duration-200 ${enabled ? "translate-x-4" : "translate-x-0"}`} />
    </button>
  );
}

const SETTINGS = [
  { label: "3 days before billing", key: "3day",  enabled: true  },
  { label: "1 day before billing",  key: "1day",  enabled: true  },
  { label: "On billing day",        key: "today", enabled: false },
  { label: "After failed payment",  key: "fail",  enabled: true  },
];

export default function RemindersPage() {
  const dispatch = useDispatch();
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [settings, setSettings]   = useState(SETTINGS);
  const [testSent, setTestSent]   = useState(null);

  useEffect(() => {
    reminderAPI.getAll()
      .then((r) => setReminders(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const sendTest = async (subId, remId) => {
    setTestSent(remId);
    try {
      await reminderAPI.triggerTest(subId);
      dispatch(showToast({ type: "success", message: "Test reminder sent!" }));
    } catch {
      dispatch(showToast({ type: "error", message: "Failed to send test" }));
    } finally { setTimeout(() => setTestSent(null), 2000); }
  };

  const pending = reminders.filter((r) => !r.sent);
  const sent    = reminders.filter((r) => r.sent);

  return (
    <div className="space-y-6 animate-slide-up">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div><h1 className="page-title">Reminders</h1><p className="page-subtitle">{pending.length} pending · {sent.length} sent</p></div>
        <div className="flex items-center gap-2 rounded-xl border border-brand-500/20 bg-brand-500/10 px-4 py-2.5">
          <Bell size={15} className="text-brand-400" />
          <span className="text-sm font-medium text-brand-300">Auto-reminders active</span>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {[
          { label: "Pending",    value: pending.length, color: "text-amber-400",   bg: "bg-amber-500/10 border-amber-500/20"   },
          { label: "Sent",       value: sent.length,    color: "text-emerald-400", bg: "bg-emerald-500/10 border-emerald-500/20" },
          { label: "Total",      value: reminders.length, color: "text-brand-400", bg: "bg-brand-500/10 border-brand-500/20"   },
          { label: "Open Rate",  value: "—",            color: "text-violet-400",  bg: "bg-violet-500/10 border-violet-500/20" },
        ].map((s) => (
          <div key={s.label} className={`glass-card border p-4 ${s.bg}`}>
            <p className="text-xs text-slate-500">{s.label}</p>
            <p className={`mt-2 text-2xl font-bold ${s.color}`}>{s.value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          {/* Pending */}
          <Card>
            <div className="mb-4 flex items-center gap-2">
              <Clock size={16} className="text-amber-400" />
              <h3 className="font-semibold text-white">Pending Reminders</h3>
              <span className="badge bg-amber-500/15 text-amber-400 border border-amber-500/20">{pending.length}</span>
            </div>
            {loading ? <p className="text-sm text-slate-500">Loading...</p> :
             pending.length === 0 ? <p className="text-sm text-slate-500">All reminders sent.</p> :
             <div className="space-y-3">
               {pending.map((r) => (
                 <div key={r.id} className="flex items-center gap-4 rounded-xl border border-surface-border bg-surface/50 p-4 transition hover:border-brand-500/30">
                   <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-500/15 border border-amber-500/20">
                     <Bell size={16} className="text-amber-400" />
                   </div>
                   <div className="flex-1 min-w-0">
                     <p className="truncate text-sm font-semibold text-white">{r.subscriptionName}</p>
                     <p className="text-xs text-slate-500">{r.reminderType === "THREE_DAYS" ? "3 days" : "1 day"} before · {r.scheduledFor}</p>
                   </div>
                   <div className="flex items-center gap-3 shrink-0">
                     <span className="text-sm font-semibold text-white">₹{parseFloat(r.subscriptionAmount || 0).toLocaleString("en-IN", { maximumFractionDigits: 0 })}</span>
                     <button
                       onClick={() => sendTest(r.subscriptionId, r.id)}
                       className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-medium transition ${testSent === r.id ? "border-emerald-500/30 bg-emerald-500/15 text-emerald-400" : "border-surface-border text-slate-400 hover:border-brand-500/40 hover:text-brand-400"}`}
                     >
                       {testSent === r.id ? <><CheckCircle2 size={12} /> Sent!</> : <><Send size={12} /> Test</>}
                     </button>
                   </div>
                 </div>
               ))}
             </div>}
          </Card>

          {/* Sent */}
          <Card>
            <div className="mb-4 flex items-center gap-2">
              <CheckCircle2 size={16} className="text-emerald-400" />
              <h3 className="font-semibold text-white">Sent Reminders</h3>
              <span className="badge bg-emerald-500/15 text-emerald-400 border border-emerald-500/20">{sent.length}</span>
            </div>
            {sent.length === 0 ? <p className="text-sm text-slate-500">No reminders sent yet.</p> :
             <div className="space-y-3">
               {sent.map((r) => (
                 <div key={r.id} className="flex items-center gap-4 rounded-xl border border-surface-border bg-surface/30 p-4 opacity-70">
                   <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-emerald-500/10 border border-emerald-500/20">
                     <Mail size={16} className="text-emerald-400" />
                   </div>
                   <div className="flex-1 min-w-0">
                     <p className="truncate text-sm font-semibold text-white">{r.subscriptionName}</p>
                     <p className="text-xs text-slate-500">Sent {r.sentAt} · Email</p>
                   </div>
                   <Badge label="Sent" variant="SUCCESS" />
                 </div>
               ))}
             </div>}
          </Card>
        </div>

        {/* Settings */}
        <div className="space-y-4">
          <Card>
            <h3 className="mb-4 font-semibold text-white">Notification Settings</h3>
            <div className="space-y-4">
              {settings.map((s) => (
                <div key={s.key} className="flex items-center justify-between">
                  <p className="text-sm font-medium text-slate-300">{s.label}</p>
                  <Toggle enabled={s.enabled} onChange={() => setSettings((prev) => prev.map((x) => x.key === s.key ? { ...x, enabled: !x.enabled } : x))} />
                </div>
              ))}
            </div>
          </Card>

          {/* Email preview */}
          <Card>
            <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-500">Email Preview</p>
            <div className="rounded-xl border border-surface-border bg-surface p-4 text-xs">
              <p className="font-semibold text-white">📬 SubTrack Reminder</p>
              <p className="mt-2 text-slate-400">Hi there,</p>
              <p className="mt-1 text-slate-400">Your <span className="text-white font-medium">Netflix</span> subscription is due in <span className="text-brand-400 font-medium">3 days</span>.</p>
              <p className="mt-2 text-slate-500">Make sure your payment method is up to date.</p>
              <div className="mt-3 rounded-lg bg-brand-gradient px-3 py-1.5 text-center text-xs font-semibold text-white">View Dashboard →</div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
