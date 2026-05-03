import { useEffect, useState } from "react";
import { Edit2, Filter, Plus, Search, Trash2, X } from "lucide-react";
import { subscriptionAPI } from "../services/api";
import { useDispatch } from "react-redux";
import { showToast } from "../store/store";
import Badge from "../components/UI/Badge";
import Button from "../components/UI/Button";
import Modal from "../components/UI/Modal";
import Card from "../components/UI/Card";

const CATS = ["All","Entertainment","Music","Design","Dev Tools","Productivity","Cloud","Books","Professional","Other"];
const STATUSES = ["All","ACTIVE","AT_RISK","CANCELLED"];
const EMPTY = { name:"", category:"Entertainment", amount:"", currency:"INR", billingCycle:"MONTHLY", startDate:"", notes:"" };

function SubForm({ form, setForm, onSubmit, onCancel, loading, isEdit }) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Service Name *</label>
          <input className="input-field" placeholder="e.g. Netflix" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>
        <div>
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Category *</label>
          <select className="input-field" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
            {CATS.filter((c) => c !== "All").map((c) => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div>
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Billing Cycle *</label>
          <select className="input-field" value={form.billingCycle} onChange={(e) => setForm({ ...form, billingCycle: e.target.value })}>
            <option value="MONTHLY">Monthly</option>
            <option value="YEARLY">Yearly</option>
          </select>
        </div>
        <div>
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Amount *</label>
          <div className="flex gap-2">
            <select className="input-field w-20 shrink-0" value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value })}>
              {["INR","USD","EUR","GBP"].map((c) => <option key={c}>{c}</option>)}
            </select>
            <input className="input-field" type="number" min="0.01" step="0.01" placeholder="0.00" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
          </div>
        </div>
        <div>
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Start Date *</label>
          <input className="input-field" type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} required />
        </div>
        <div className="col-span-2">
          <label className="mb-1.5 block text-xs font-medium text-slate-400">Notes</label>
          <textarea className="input-field resize-none" rows={2} placeholder="Optional notes..." value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
        </div>
      </div>
      <div className="flex gap-3 pt-2">
        <Button type="submit" disabled={loading} className="flex-1">{loading ? "Saving..." : isEdit ? "Save Changes" : "Add Subscription"}</Button>
        <Button type="button" variant="ghost" onClick={onCancel}>Cancel</Button>
      </div>
    </form>
  );
}

export default function SubscriptionsPage() {
  const dispatch = useDispatch();
  const [subs, setSubs]           = useState([]);
  const [loading, setLoading]     = useState(true);
  const [saving, setSaving]       = useState(false);
  const [search, setSearch]       = useState("");
  const [catFilter, setCatFilter] = useState("All");
  const [statusFilter, setStatusFilter] = useState("All");
  const [addOpen, setAddOpen]     = useState(false);
  const [editSub, setEditSub]     = useState(null);
  const [deleteSub, setDeleteSub] = useState(null);
  const [form, setForm]           = useState(EMPTY);

  const load = () => {
    setLoading(true);
    subscriptionAPI.getAll()
      .then((r) => setSubs(r.data))
      .catch(() => dispatch(showToast({ type: "error", message: "Failed to load subscriptions" })))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const filtered = subs.filter((s) => {
    const q = search.toLowerCase();
    return (s.name.toLowerCase().includes(q) || s.category.toLowerCase().includes(q)) &&
           (catFilter === "All" || s.category === catFilter) &&
           (statusFilter === "All" || s.status === statusFilter);
  });

  const handleAdd = async (e) => {
    e.preventDefault(); setSaving(true);
    try {
      await subscriptionAPI.create({ ...form, amount: parseFloat(form.amount) });
      dispatch(showToast({ type: "success", message: "Subscription added!" }));
      setAddOpen(false); setForm(EMPTY); load();
    } catch (err) {
      dispatch(showToast({ type: "error", message: err.response?.data?.message || "Failed to add" }));
    } finally { setSaving(false); }
  };

  const handleEdit = async (e) => {
    e.preventDefault(); setSaving(true);
    try {
      await subscriptionAPI.update(editSub.id, { ...form, amount: parseFloat(form.amount) });
      dispatch(showToast({ type: "success", message: "Subscription updated!" }));
      setEditSub(null); load();
    } catch (err) {
      dispatch(showToast({ type: "error", message: err.response?.data?.message || "Failed to update" }));
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      await subscriptionAPI.delete(deleteSub.id);
      dispatch(showToast({ type: "success", message: "Subscription deleted" }));
      setDeleteSub(null); load();
    } catch { dispatch(showToast({ type: "error", message: "Failed to delete" })); }
  };

  const handleCancel = async (sub) => {
    try {
      await subscriptionAPI.cancel(sub.id);
      dispatch(showToast({ type: "success", message: `${sub.name} cancelled` }));
      load();
    } catch { dispatch(showToast({ type: "error", message: "Failed to cancel" })); }
  };

  const openEdit = (sub) => {
    setEditSub(sub);
    setForm({ name: sub.name, category: sub.category, amount: sub.amount, currency: sub.currency,
              billingCycle: sub.billingCycle, startDate: sub.startDate || "", notes: sub.notes || "" });
  };

  const fmt = (n) => `₹${parseFloat(n).toLocaleString("en-IN", { maximumFractionDigits: 0 })}`;

  return (
    <div className="space-y-6 animate-slide-up">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="page-title">Subscriptions</h1>
          <p className="page-subtitle">{subs.filter((s) => s.status !== "CANCELLED").length} active subscriptions</p>
        </div>
        <Button onClick={() => { setForm(EMPTY); setAddOpen(true); }}><Plus size={16} /> Add Subscription</Button>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <div className="relative flex-1">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
            <input className="input-field pl-9" placeholder="Search..." value={search} onChange={(e) => setSearch(e.target.value)} />
            {search && <button onClick={() => setSearch("")} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-white"><X size={14} /></button>}
          </div>
          <div className="flex items-center gap-2">
            <Filter size={14} className="shrink-0 text-slate-500" />
            <select className="input-field w-auto min-w-[140px]" value={catFilter} onChange={(e) => setCatFilter(e.target.value)}>
              {CATS.map((c) => <option key={c}>{c}</option>)}
            </select>
          </div>
          <select className="input-field w-auto min-w-[120px]" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            {STATUSES.map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
      </Card>

      {/* Table */}
      <Card className="overflow-hidden p-0">
        {loading ? (
          <div className="p-8 text-center text-sm text-slate-500">Loading subscriptions...</div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-16 text-center">
            <Search size={22} className="text-slate-600" />
            <p className="font-medium text-slate-300">No subscriptions found</p>
            <p className="text-sm text-slate-500">Try adjusting your filters or add a new subscription.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-surface-border">
                  {["Service","Category","Amount","Cycle","Next Billing","Days Left","Status",""].map((h) => (
                    <th key={h} className="px-5 py-3.5 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {filtered.map((sub) => (
                  <tr key={sub.id} className="group transition hover:bg-surface-hover">
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-brand-500/20 text-xs font-bold text-brand-400">{sub.name[0]}</div>
                        <span className="text-sm font-medium text-white">{sub.name}</span>
                      </div>
                    </td>
                    <td className="px-5 py-4 text-sm text-slate-400">{sub.category}</td>
                    <td className="px-5 py-4 text-sm font-semibold text-white">{fmt(sub.amount)}</td>
                    <td className="px-5 py-4"><Badge label={sub.billingCycle} variant={sub.billingCycle} /></td>
                    <td className="px-5 py-4 text-sm text-slate-400">{sub.nextBillingDate}</td>
                    <td className="px-5 py-4">
                      <span className={`text-sm font-medium ${sub.daysUntilBilling <= 3 ? "text-red-400" : sub.daysUntilBilling <= 7 ? "text-amber-400" : "text-slate-400"}`}>
                        {sub.daysUntilBilling <= 0 ? "Today" : `${sub.daysUntilBilling}d`}
                      </span>
                    </td>
                    <td className="px-5 py-4"><Badge label={sub.status === "AT_RISK" ? "At Risk" : sub.status.charAt(0) + sub.status.slice(1).toLowerCase()} variant={sub.status} /></td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-1 opacity-0 transition group-hover:opacity-100">
                        <button onClick={() => openEdit(sub)} className="rounded-lg p-1.5 text-slate-500 hover:bg-brand-500/15 hover:text-brand-400 transition"><Edit2 size={14} /></button>
                        {sub.status !== "CANCELLED" && (
                          <button onClick={() => handleCancel(sub)} className="rounded-lg p-1.5 text-slate-500 hover:bg-amber-500/15 hover:text-amber-400 transition text-xs font-medium px-2">Cancel</button>
                        )}
                        <button onClick={() => setDeleteSub(sub)} className="rounded-lg p-1.5 text-slate-500 hover:bg-red-500/15 hover:text-red-400 transition"><Trash2 size={14} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {filtered.length > 0 && (
          <div className="flex items-center justify-between border-t border-surface-border px-5 py-3">
            <p className="text-xs text-slate-500">Showing {filtered.length} of {subs.length}</p>
          </div>
        )}
      </Card>

      <Modal open={addOpen} onClose={() => setAddOpen(false)} title="Add New Subscription">
        <SubForm form={form} setForm={setForm} onSubmit={handleAdd} onCancel={() => setAddOpen(false)} loading={saving} isEdit={false} />
      </Modal>
      <Modal open={!!editSub} onClose={() => setEditSub(null)} title={`Edit — ${editSub?.name}`}>
        <SubForm form={form} setForm={setForm} onSubmit={handleEdit} onCancel={() => setEditSub(null)} loading={saving} isEdit />
      </Modal>
      <Modal open={!!deleteSub} onClose={() => setDeleteSub(null)} title="Delete Subscription">
        <p className="text-sm text-slate-400">Delete <span className="font-semibold text-white">{deleteSub?.name}</span>? This cannot be undone.</p>
        <div className="mt-6 flex gap-3">
          <button onClick={handleDelete} className="flex-1 rounded-xl bg-red-500/20 border border-red-500/30 py-2.5 text-sm font-semibold text-red-400 hover:bg-red-500/30 transition">Delete</button>
          <Button variant="ghost" onClick={() => setDeleteSub(null)} className="flex-1">Cancel</Button>
        </div>
      </Modal>
    </div>
  );
}
