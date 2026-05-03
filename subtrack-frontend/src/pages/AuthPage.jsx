import { Eye, EyeOff, Zap } from "lucide-react";
import { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { authAPI } from "../services/api";
import { setCredentials } from "../store/store";

const features = [
  { e: "📊", t: "Spend Analytics",  d: "Monthly breakdowns and trend charts." },
  { e: "🔔", t: "Smart Reminders",  d: "Notified 1 and 3 days before billing." },
  { e: "🚨", t: "Waste Detection",  d: "Flags subscriptions you haven't used." },
  { e: "💡", t: "AI Insights",      d: "Tips to cut unnecessary recurring costs." },
];

export default function AuthPage() {
  const dispatch  = useDispatch();
  const navigate  = useNavigate();
  const [isReg, setIsReg]     = useState(false);
  const [showPw, setShowPw]   = useState(false);
  const [form, setForm]       = useState({ fullName: "", email: "", password: "" });
  const [errors, setErrors]   = useState({});
  const [apiErr, setApiErr]   = useState("");
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const e = {};
    if (isReg && !form.fullName.trim()) e.fullName = "Full name is required.";
    if (!form.email.trim())             e.email    = "Email is required.";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = "Invalid email.";
    if (!form.password)                 e.password = "Password is required.";
    else if (form.password.length < 6)  e.password = "Minimum 6 characters.";
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    if (!validate()) return;
    setLoading(true); setApiErr("");
    try {
      if (isReg) {
        await authAPI.register({ fullName: form.fullName, email: form.email, password: form.password });
        setIsReg(false);
        setForm((p) => ({ ...p, fullName: "", password: "" }));
      } else {
        const { data } = await authAPI.login({ email: form.email, password: form.password });
        dispatch(setCredentials(data));
        navigate("/dashboard");
      }
    } catch (err) {
      setApiErr(err.response?.data?.message || "Something went wrong.");
    } finally { setLoading(false); }
  };

  return (
    <div className="flex min-h-screen bg-surface">
      {/* Left hero */}
      <div className="relative hidden flex-col justify-between overflow-hidden bg-surface-card p-12 lg:flex lg:w-1/2">
        <div className="pointer-events-none absolute -left-32 -top-32 h-96 w-96 rounded-full bg-brand-600/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-32 -right-32 h-96 w-96 rounded-full bg-violet-600/15 blur-3xl" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-gradient shadow-glow"><Zap size={20} className="text-white" /></div>
          <div><span className="text-xl font-bold text-white">SubTrack</span><p className="text-[10px] font-medium uppercase tracking-widest text-slate-500">Smart Billing</p></div>
        </div>
        <div className="relative">
          <h1 className="text-4xl font-extrabold leading-tight text-white">
            Take control of your<br />
            <span className="bg-brand-gradient bg-clip-text text-transparent">subscriptions.</span>
          </h1>
          <p className="mt-4 max-w-sm text-base leading-relaxed text-slate-400">
            SubTrack tracks every recurring payment, predicts upcoming bills, and surfaces money you're wasting.
          </p>
          <div className="mt-10 grid gap-4">
            {features.map((f) => (
              <div key={f.t} className="flex items-start gap-4 rounded-xl border border-surface-border bg-surface/60 p-4">
                <span className="text-2xl">{f.e}</span>
                <div><p className="text-sm font-semibold text-white">{f.t}</p><p className="mt-0.5 text-xs text-slate-400">{f.d}</p></div>
              </div>
            ))}
          </div>
        </div>
        <div className="relative flex items-center gap-4">
          <div className="flex -space-x-2">
            {["A","B","C","D"].map((l) => (
              <div key={l} className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-surface-card bg-brand-gradient text-xs font-bold text-white">{l}</div>
            ))}
          </div>
          <p className="text-sm text-slate-400"><span className="font-semibold text-white">2,400+</span> users saving money every month</p>
        </div>
      </div>

      {/* Right form */}
      <div className="flex flex-1 items-center justify-center px-6 py-12">
        <div className="w-full max-w-md animate-slide-up">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient shadow-glow-sm"><Zap size={18} className="text-white" /></div>
            <span className="text-lg font-bold text-white">SubTrack</span>
          </div>
          <h2 className="text-2xl font-bold text-white">{isReg ? "Create your account" : "Welcome back"}</h2>
          <p className="mt-2 text-sm text-slate-400">{isReg ? "Start managing your subscriptions." : "Sign in to your SubTrack dashboard."}</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-4">
            {isReg && (
              <div>
                <input className={`input-field ${errors.fullName ? "border-red-500/60" : ""}`} placeholder="Full name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                {errors.fullName && <p className="mt-1 text-xs text-red-400">{errors.fullName}</p>}
              </div>
            )}
            <div>
              <input className={`input-field ${errors.email ? "border-red-500/60" : ""}`} placeholder="Email address" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              {errors.email && <p className="mt-1 text-xs text-red-400">{errors.email}</p>}
            </div>
            <div>
              <div className="relative">
                <input className={`input-field pr-11 ${errors.password ? "border-red-500/60" : ""}`} placeholder="Password" type={showPw ? "text" : "password"} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
                <button type="button" onClick={() => setShowPw((v) => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <p className="mt-1 text-xs text-red-400">{errors.password}</p>}
            </div>
            {apiErr && <div className="rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">{apiErr}</div>}
            <button type="submit" disabled={loading} className="btn-primary w-full py-3 text-base">
              {loading ? <span className="flex items-center gap-2"><span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />Please wait...</span>
                       : isReg ? "Create account" : "Sign in"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500">
            {isReg ? "Already have an account? " : "New here? "}
            <button onClick={() => { setIsReg((v) => !v); setErrors({}); setApiErr(""); }} className="font-semibold text-brand-400 hover:text-brand-300 transition">
              {isReg ? "Sign in" : "Create an account"}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
