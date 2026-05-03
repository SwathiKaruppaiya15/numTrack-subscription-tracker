import { BarChart3, Bell, CreditCard, LayoutDashboard, Sparkles, X, Zap } from "lucide-react";
import { NavLink } from "react-router-dom";

const navItems = [
  { to: "/dashboard",     label: "Dashboard",     icon: LayoutDashboard },
  { to: "/subscriptions", label: "Subscriptions", icon: CreditCard },
  { to: "/analytics",     label: "Analytics",     icon: BarChart3 },
  { to: "/reminders",     label: "Reminders",     icon: Bell },
];

function SidebarContent({ onClose }) {
  return (
    <div className="flex h-full flex-col px-4 py-5">
      {/* Logo */}
      <div className="flex items-center justify-between pb-8 pt-2 px-2">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-gradient shadow-glow-sm">
            <Zap size={18} className="text-white" />
          </div>
          <div>
            <span className="text-base font-bold text-white">SubTrack</span>
            <p className="text-[10px] font-medium text-slate-500 uppercase tracking-widest">Smart Billing</p>
          </div>
        </div>
        {onClose && (
          <button onClick={onClose} className="rounded-lg p-1.5 text-slate-400 hover:text-white lg:hidden">
            <X size={18} />
          </button>
        )}
      </div>

      {/* Nav */}
      <nav className="flex-1 space-y-1">
        <p className="mb-2 px-3 text-[10px] font-semibold uppercase tracking-widest text-slate-600">Menu</p>
        {navItems.map((item) => (
          <NavLink
            key={item.to} to={item.to} onClick={onClose}
            className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
          >
            <item.icon size={17} />
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* Pro nudge */}
      <div className="mt-6 rounded-xl border border-brand-500/20 bg-brand-500/10 p-4">
        <div className="flex items-center gap-2 text-brand-400">
          <Sparkles size={15} />
          <span className="text-xs font-semibold">Pro Plan</span>
        </div>
        <p className="mt-1.5 text-xs text-slate-400 leading-relaxed">
          Unlock AI insights, export reports, and unlimited subscriptions.
        </p>
        <button className="mt-3 w-full rounded-lg bg-brand-gradient py-1.5 text-xs font-semibold text-white shadow-glow-sm hover:shadow-glow transition-all">
          Upgrade Now
        </button>
      </div>
    </div>
  );
}

export default function Sidebar({ mobileOpen, setMobileOpen }) {
  return (
    <>
      <aside className="hidden h-screen w-64 shrink-0 border-r border-surface-border bg-surface-card lg:fixed lg:block">
        <SidebarContent />
      </aside>
      {mobileOpen && (
        <div className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden" onClick={() => setMobileOpen(false)} />
      )}
      <aside className={`fixed left-0 top-0 z-50 h-screen w-64 border-r border-surface-border bg-surface-card transition-transform duration-300 lg:hidden ${mobileOpen ? "translate-x-0" : "-translate-x-full"}`}>
        <SidebarContent onClose={() => setMobileOpen(false)} />
      </aside>
    </>
  );
}
