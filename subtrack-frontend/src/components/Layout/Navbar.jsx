import { Bell, LogOut, Menu, Search } from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { logout } from "../../store/store";

export default function Navbar({ onMenuClick }) {
  const dispatch  = useDispatch();
  const navigate  = useNavigate();
  const user      = useSelector((s) => s.auth.user);
  const initial   = user?.fullName?.[0]?.toUpperCase() ?? "U";

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-surface-border bg-surface-card/80 px-4 backdrop-blur-md sm:px-6">
      <div className="flex items-center gap-3">
        <button onClick={onMenuClick} className="rounded-lg p-2 text-slate-400 hover:bg-surface-hover hover:text-white transition lg:hidden">
          <Menu size={20} />
        </button>
        <div className="hidden items-center gap-2 rounded-xl border border-surface-border bg-surface px-3 py-1.5 text-sm text-slate-500 sm:flex">
          <Search size={15} />
          <span>Search...</span>
          <kbd className="ml-2 rounded border border-surface-border px-1.5 py-0.5 text-[10px] text-slate-600">⌘K</kbd>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <button className="relative rounded-lg p-2 text-slate-400 hover:bg-surface-hover hover:text-white transition">
          <Bell size={18} />
          <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-brand-500 ring-2 ring-surface-card" />
        </button>
        <div className="hidden items-center gap-2 rounded-xl border border-surface-border bg-surface px-3 py-1.5 sm:flex">
          <div className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-gradient text-xs font-bold text-white">{initial}</div>
          <span className="text-sm font-medium text-slate-300">{user?.fullName?.split(" ")[0] ?? "User"}</span>
        </div>
        <button
          onClick={() => { dispatch(logout()); navigate("/"); }}
          className="flex items-center gap-1.5 rounded-xl border border-surface-border px-3 py-1.5 text-sm text-slate-400 transition hover:border-red-500/40 hover:bg-red-500/10 hover:text-red-400"
        >
          <LogOut size={15} />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
}
