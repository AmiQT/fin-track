import { useState, useEffect } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import {
  Check,
  X,
  Calendar,
  Loader2,
  CheckCircle2,
} from 'lucide-react';
import { format } from 'date-fns';

const LeaveCard = ({ leave, onAction }) => (
  <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm hover:shadow-md transition-all animate-in fade-in zoom-in duration-300">
    <div className="flex items-start justify-between mb-4">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 font-bold">
          {leave.employeeName.charAt(0)}
        </div>
        <div>
          <h4 className="text-sm font-bold text-slate-800">{leave.employeeName}</h4>
          <p className="text-[10px] text-slate-400 font-medium uppercase tracking-wider">{leave.leaveType}</p>
        </div>
      </div>
      <div className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-widest uppercase border ${
        leave.status === 'PENDING' ? 'bg-amber-50 text-amber-600 border-amber-100' : 
        leave.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 
        'bg-rose-50 text-rose-600 border-rose-100'
      }`}>
        {leave.status}
      </div>
    </div>

    <div className="space-y-3 mb-6">
      <div className="flex items-center gap-2 text-xs text-slate-600 bg-slate-50 p-2 rounded-xl border border-transparent">
        <Calendar size={14} className="text-slate-400" />
        <span className="font-bold">{format(new Date(leave.startDate), 'dd MMM')}</span>
        <span className="text-slate-300">-</span>
        <span className="font-bold">{format(new Date(leave.endDate), 'dd MMM yyyy')}</span>
        <span className="ml-auto bg-white px-2 py-0.5 rounded-lg border border-slate-100 text-primary-600 font-black">
          {leave.totalDays} Days
        </span>
      </div>
      <div className="p-3 bg-slate-50 rounded-xl border border-transparent">
        <p className="text-[11px] font-bold text-slate-400 uppercase mb-1">Reason</p>
        <p className="text-xs text-slate-600 italic">"{leave.reason || 'No reason provided'}"</p>
      </div>
    </div>

    {leave.status === 'PENDING' && (
      <div className="grid grid-cols-2 gap-3">
        <button 
          onClick={() => onAction(leave.id, 'approve')}
          className="flex items-center justify-center gap-2 py-2.5 px-4 bg-emerald-600 text-white rounded-xl text-xs font-bold hover:bg-emerald-700 transition-all shadow-lg shadow-emerald-600/10 active:scale-95"
        >
          <Check size={14} />
          Approve
        </button>
        <button 
          onClick={() => onAction(leave.id, 'reject')}
          className="flex items-center justify-center gap-2 py-2.5 px-4 bg-white border border-rose-100 text-rose-500 rounded-xl text-xs font-bold hover:bg-rose-50 transition-all active:scale-95"
        >
          <X size={14} />
          Reject
        </button>
      </div>
    )}
  </div>
);

const Leaves = () => {
  const { showToast } = useToast();
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('PENDING');

  const fetchLeaves = async () => {
    try {
      setLoading(true);
      const url = activeTab === 'PENDING' ? '/leaves/pending' : '/leaves/my';
      const response = await api.get(url);
      setLeaves(response.data);
    } catch (error) {
      console.error('Failed to fetch leaves', error);
      showToast('Failed to load leave requests.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchLeaves();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const handleAction = async (id, action) => {
    try {
      if (action === 'approve') {
        await api.put(`/leaves/${id}/approve`);
        showToast('Leave request approved! ✅', 'success');
      } else {
        const reason = window.prompt('Please provide a reason for rejection:');
        if (reason === null) return;
        if (!reason.trim()) {
          showToast('Rejection reason is required.', 'error');
          return;
        }
        await api.put(`/leaves/${id}/reject`, { reason });
        showToast('Leave request rejected.', 'info');
      }
      fetchLeaves();
    } catch (error) {
      console.error('Failed to update leave', error);
      showToast('Failed to update leave request.', 'error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Leave Requests</h1>
          <p className="text-slate-500 text-sm mt-1">Review and manage employee time-off applications.</p>
        </div>
        <div className="flex bg-white p-1 rounded-xl border border-slate-200 shadow-sm">
          <button 
            onClick={() => setActiveTab('PENDING')}
            className={`px-6 py-2 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'PENDING' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            Pending
          </button>
          <button 
            onClick={() => setActiveTab('ALL')}
            className={`px-6 py-2 rounded-lg text-xs font-bold transition-all ${
              activeTab === 'ALL' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            All Requests
          </button>
        </div>
      </div>

      {loading ? (
        <div className="py-20 text-center">
          <Loader2 className="animate-spin text-primary-500 mx-auto" size={32} />
        </div>
      ) : leaves.length === 0 ? (
        <div className="py-32 bg-white rounded-3xl border border-slate-100 border-dashed text-center">
          <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle2 size={32} className="text-slate-300" />
          </div>
          <h3 className="text-slate-800 font-bold">All caught up!</h3>
          <p className="text-slate-400 text-sm mt-1">No pending leave requests to review.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {leaves.map(leave => (
            <LeaveCard key={leave.id} leave={leave} onAction={handleAction} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Leaves;