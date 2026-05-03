import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { clearToast } from "../../store/store";
import { CheckCircle2, XCircle, Info } from "lucide-react";

const icons = { success: CheckCircle2, error: XCircle, info: Info };
const colors = {
  success: "border-emerald-500/30 bg-emerald-500/10 text-emerald-300",
  error:   "border-red-500/30 bg-red-500/10 text-red-300",
  info:    "border-brand-500/30 bg-brand-500/10 text-brand-300",
};

export default function Toast() {
  const dispatch = useDispatch();
  const toast    = useSelector((s) => s.ui.toast);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => dispatch(clearToast()), 3500);
    return () => clearTimeout(t);
  }, [toast, dispatch]);

  if (!toast) return null;
  const Icon = icons[toast.type] ?? Info;

  return (
    <div className={`fixed bottom-6 right-6 z-[100] flex items-center gap-3 rounded-xl border px-4 py-3 shadow-lg animate-slide-up ${colors[toast.type] ?? colors.info}`}>
      <Icon size={18} />
      <span className="text-sm font-medium">{toast.message}</span>
    </div>
  );
}
