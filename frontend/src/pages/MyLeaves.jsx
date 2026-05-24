import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import { 
  Plus,
  Calendar,
  Clock,
  CheckCircle2,
  XCircle,
  Loader2,
  AlertCircle,
  X
} from 'lucide-react';
import { format } from 'date-fns';

const LeaveStatusBadge = ({ status }) => {
  const styles = {
    'PENDING': 'bg-amber-50 text-amber-600 border-amber-100',
    'APPROVED': 'bg-emerald-50 text-emerald-600 border-emerald-100',
    'REJECTED': 'bg-rose-50 text-rose-600 border-rose-100'
  };

  return (
    <span className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-widest uppercase border ${styles[status] || styles.PENDING}`}>
      {status}
    </span>
  );
};

const MyLeaves = () => {
  const { showToast } = useToast();
  const [leaves, setLeaves] = useState([]);
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    leaveType: 'ANNUAL',
    startDate: '',
    endDate: '',
    reason: ''
  });

  const fetchMyLeaves = async () => {
    try {
      setLoading(true);
      const [leavesRes, balanceRes] = await Promise.all([
        api.get('/leaves/my'),
        api.get('/leaves/balance')
      ]);
      setLeaves(leavesRes.data);
      setBalance(balanceRes.data);
    } catch (error) {
      console.error('Failed to fetch leaves', error);
      showToast('Failed to load leave history.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyLeaves();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    
    // Client side validation
    if (balance) {
      const start = new Date(formData.startDate);
      const end = new Date(formData.endDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

      if (formData.leaveType === 'ANNUAL' && diffDays > balance.annualBalance) {
        showToast(`Insufficient Annual Leave. You only have ${balance.annualBalance} days left.`, 'error');
        setSubmitting(false);
        return;
      }
      if (formData.leaveType === 'SICK' && diffDays > balance.sickBalance) {
        showToast(`Insufficient Sick Leave. You only have ${balance.sickBalance} days left.`, 'error');
        setSubmitting(false);
        return;
      }
    }

    try {
      await api.post('/leaves', formData);
      showToast('Leave request submitted successfully.', 'success');
      setShowModal(false);
      setFormData({ leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '' });
      fetchMyLeaves();
    } catch (error) {
      showToast(error.response?.data?.message || 'Failed to apply for leave', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">My Leaves</h1>
          <p className="text-slate-500 text-sm mt-1">Track your time-off history and requests.</p>
        </div>
        <button 
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary-600 text-white px-6 py-3 rounded-2xl font-bold text-sm shadow-lg shadow-primary-600/20 hover:bg-primary-700 transition-all active:scale-95"
        >
          <Plus size={18} />
          Request Leave
        </button>
      </div>

      {/* Balance Cards */}
      {!loading && balance && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex items-center gap-6">
            <div className="w-14 h-14 bg-sky-50 text-sky-500 rounded-2xl flex items-center justify-center">
              <Calendar size={24} />
            </div>
            <div className="flex-1">
              <p className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-1">Annual Leave</p>
              <div className="flex items-baseline gap-2">
                <span className="text-3xl font-black text-slate-900">{balance.annualBalance}</span>
                <span className="text-sm font-bold text-slate-400">/ {balance.annualEntitlement} days left</span>
              </div>
            </div>
            <div className="text-right">
              <p className="text-xs font-bold text-slate-400 mb-1">Taken</p>
              <p className="text-lg font-bold text-slate-700">{balance.annualTaken}</p>
            </div>
          </div>
          
          <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex items-center gap-6">
            <div className="w-14 h-14 bg-rose-50 text-rose-500 rounded-2xl flex items-center justify-center">
              <Plus size={24} />
            </div>
            <div className="flex-1">
              <p className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-1">Medical Leave</p>
              <div className="flex items-baseline gap-2">
                <span className="text-3xl font-black text-slate-900">{balance.sickBalance}</span>
                <span className="text-sm font-bold text-slate-400">/ {balance.sickEntitlement} days left</span>
              </div>
            </div>
            <div className="text-right">
              <p className="text-xs font-bold text-slate-400 mb-1">Taken</p>
              <p className="text-lg font-bold text-slate-700">{balance.sickTaken}</p>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="py-20 text-center">
          <Loader2 className="animate-spin text-primary-500 mx-auto" size={32} />
        </div>
      ) : leaves.length === 0 ? (
        <div className="py-32 bg-white rounded-3xl border border-slate-100 text-center">
          <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <Calendar size={32} className="text-slate-300" />
          </div>
          <h3 className="text-slate-800 font-bold">No leave history</h3>
          <p className="text-slate-400 text-sm mt-1">You haven't requested any leaves yet.</p>
        </div>
      ) : (
        <div className="bg-white rounded-3xl border border-slate-100 overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-slate-50/50 border-b border-slate-100">
                  <th className="px-6 py-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Type</th>
                  <th className="px-6 py-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Duration</th>
                  <th className="px-6 py-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Days</th>
                  <th className="px-6 py-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Status</th>
                  <th className="px-6 py-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Applied On</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {leaves.map((leave) => (
                  <tr key={leave.id} className="hover:bg-slate-50/30 transition-colors group">
                    <td className="px-6 py-4">
                      <span className="text-xs font-bold text-slate-700">{leave.leaveType}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col">
                        <span className="text-xs font-bold text-slate-800">
                          {format(new Date(leave.startDate), 'dd MMM')} - {format(new Date(leave.endDate), 'dd MMM yyyy')}
                        </span>
                        {leave.reason && (
                          <span className="text-[10px] text-slate-400 mt-0.5 truncate max-w-[200px] group-hover:text-slate-500">
                            {leave.reason}
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-black text-primary-600 bg-primary-50 px-2 py-0.5 rounded-lg border border-primary-100">
                        {leave.totalDays}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <LeaveStatusBadge status={leave.status} />
                    </td>
                    <td className="px-6 py-4 text-[11px] font-medium text-slate-400">
                      {format(new Date(leave.appliedAt), 'dd/MM/yyyy')}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !submitting && setShowModal(false)}></div>
          <div className="bg-white rounded-[32px] w-full max-w-lg relative z-10 shadow-2xl border border-slate-100 overflow-hidden animate-in slide-in-from-bottom-8 duration-500">
            <div className="p-8">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-2xl font-bold text-slate-900">Apply for Leave</h2>
                <button onClick={() => setShowModal(false)} className="p-2 hover:bg-slate-50 rounded-xl transition-colors">
                  <X size={20} className="text-slate-400" />
                </button>
              </div>
              <p className="text-slate-500 text-sm mb-8">Fill in the details for your time-off request.</p>

              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">Leave Type</label>
                  <select 
                    required
                    className="w-full bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all"
                    value={formData.leaveType}
                    onChange={(e) => setFormData({...formData, leaveType: e.target.value})}
                  >
                    <option value="ANNUAL">Annual Leave</option>
                    <option value="SICK">Medical/Sick Leave</option>
                    <option value="UNPAID">Unpaid Leave</option>
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">Start Date</label>
                    <input 
                      type="date" 
                      required
                      className="w-full bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all"
                      value={formData.startDate}
                      onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">End Date</label>
                    <input 
                      type="date" 
                      required
                      className="w-full bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all"
                      value={formData.endDate}
                      onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">Reason (Optional)</label>
                  <textarea 
                    rows="3"
                    placeholder="Tell us why you need this break..."
                    className="w-full bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3 text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 outline-none transition-all resize-none"
                    value={formData.reason}
                    onChange={(e) => setFormData({...formData, reason: e.target.value})}
                  ></textarea>
                </div>

                <div className="flex gap-3 mt-4">
                  <button 
                    type="button"
                    disabled={submitting}
                    onClick={() => setShowModal(false)}
                    className="flex-1 py-3 px-6 rounded-2xl font-bold text-sm text-slate-500 hover:bg-slate-50 transition-all disabled:opacity-50"
                  >
                    Cancel
                  </button>
                  <button 
                    type="submit"
                    disabled={submitting}
                    className="flex-1 py-3 px-6 bg-primary-600 text-white rounded-2xl font-bold text-sm shadow-lg shadow-primary-600/20 hover:bg-primary-700 transition-all active:scale-95 disabled:opacity-50 flex items-center justify-center gap-2"
                  >
                    {submitting ? (
                      <Loader2 className="animate-spin" size={18} />
                    ) : (
                      'Submit Request'
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyLeaves;