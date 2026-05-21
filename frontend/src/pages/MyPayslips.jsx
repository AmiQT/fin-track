import { useState, useEffect } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import {
  Wallet,
  FileText,
  Loader2,
  ChevronDown,
  ChevronUp,
  ArrowRight,
} from 'lucide-react';
import { format } from 'date-fns';

const DeductionRow = ({ label, value, sublabel }) => (
  <div className="flex items-center justify-between py-2">
    <div>
      <p className="text-xs font-semibold text-slate-600">{label}</p>
      {sublabel && <p className="text-[10px] text-slate-400">{sublabel}</p>}
    </div>
    <span className="text-xs font-bold text-rose-500">− RM {value}</span>
  </div>
);

const PayslipCard = ({ payroll }) => {
  const [expanded, setExpanded] = useState(false);

  const getMonthName = (month) => format(new Date(2024, month - 1, 1), 'MMMM');

  const epf = (payroll.epfEmployee || 0).toFixed(2);
  const socso = (payroll.socsoEmployee || 0).toFixed(2);
  const tax = (payroll.incomeTax || 0).toFixed(2);
  const unpaid = (payroll.unpaidLeaveDeduction || 0).toFixed(2);
  const totalDed = (payroll.totalDeductions || 0).toFixed(2);
  const gross = (payroll.grossSalary || 0).toFixed(2);
  const net = (payroll.netSalary || 0);

  return (
    <div className="bg-white rounded-[28px] border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden">
      
      {/* ── Header ── */}
      <div className="p-6 pb-4">
        <div className="flex items-start justify-between mb-5">
          <div className="w-12 h-12 rounded-2xl bg-primary-50 flex items-center justify-center text-primary-600">
            <FileText size={22} />
          </div>
          <div className="text-right">
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]">Period</p>
            <p className="text-sm font-bold text-slate-800">{getMonthName(payroll.month)} {payroll.year}</p>
          </div>
        </div>

        {/* Net Salary — hero number */}
        <div className="mb-5">
          <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Net Salary (Take Home)</p>
          <p className="text-3xl font-black text-slate-900">
            RM {net.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>

        {/* Gross → Net visual bar */}
        <div className="bg-slate-50 rounded-2xl p-4 mb-4 border border-slate-100">
          <div className="flex items-center justify-between text-xs mb-3">
            <div>
              <p className="text-slate-400 font-bold uppercase tracking-wider text-[10px]">Gross Salary</p>
              <p className="font-black text-slate-800 text-sm">RM {gross}</p>
            </div>
            <div className="flex items-center gap-1 text-slate-300">
              <div className="w-6 h-px bg-slate-200"></div>
              <ArrowRight size={14} className="text-slate-300" />
            </div>
            <div className="text-right">
              <p className="text-slate-400 font-bold uppercase tracking-wider text-[10px]">Total Deductions</p>
              <p className="font-black text-rose-500 text-sm">− RM {totalDed}</p>
            </div>
          </div>
          {/* Progress bar showing deduction ratio */}
          <div className="w-full bg-slate-200 rounded-full h-1.5 overflow-hidden">
            <div 
              className="h-full rounded-full bg-gradient-to-r from-emerald-400 to-emerald-500"
              style={{ width: `${Math.max(0, Math.min(100, (net / (payroll.grossSalary || 1)) * 100))}%` }}
            />
          </div>
          <div className="flex justify-between mt-1">
            <span className="text-[9px] text-emerald-500 font-bold">Take home {((net / (payroll.grossSalary || 1)) * 100).toFixed(1)}%</span>
            <span className="text-[9px] text-rose-400 font-bold">Deducted {(((payroll.totalDeductions || 0) / (payroll.grossSalary || 1)) * 100).toFixed(1)}%</span>
          </div>
        </div>

        {/* Expandable breakdown toggle */}
        <button
          onClick={() => setExpanded(!expanded)}
          className="w-full flex items-center justify-between py-2 px-3 rounded-xl hover:bg-slate-50 transition-colors text-xs font-bold text-slate-500 group"
        >
          <span className="group-hover:text-slate-800 transition-colors">
            {expanded ? 'Hide' : 'View'} deduction breakdown
          </span>
          {expanded 
            ? <ChevronUp size={15} className="text-slate-400" /> 
            : <ChevronDown size={15} className="text-slate-400" />
          }
        </button>
      </div>

      {/* ── Deduction Breakdown (Expandable) ── */}
      {expanded && (
        <div className="px-6 pb-4 animate-in slide-in-from-top-2 duration-200">
          <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100 divide-y divide-slate-100">
            <DeductionRow 
              label="EPF (Employees Provident Fund)" 
              sublabel="11% of Basic Salary — employee contribution"
              value={epf} 
            />
            <DeductionRow 
              label="SOCSO (Social Security)" 
              sublabel="0.5% of Basic Salary"
              value={socso} 
            />
            <DeductionRow 
              label="PCB / Income Tax" 
              sublabel="Monthly tax deduction based on gross"
              value={tax} 
            />
            {parseFloat(unpaid) > 0 && (
              <DeductionRow 
                label="Unpaid Leave Deduction" 
                sublabel="Deducted for approved unpaid leave days"
                value={unpaid} 
              />
            )}
            <div className="flex items-center justify-between pt-3 mt-1">
              <p className="text-xs font-black text-slate-800">Total Deductions</p>
              <p className="text-sm font-black text-rose-600">− RM {totalDed}</p>
            </div>
          </div>

          {/* EPF note */}
          <p className="text-[10px] text-slate-400 mt-3 leading-relaxed px-1">
            💡 <span className="font-semibold">Note:</span> EPF employer contribution (13%) of RM {(payroll.epfEmployer || 0).toFixed(2)} is paid separately by your employer and does not affect your take-home pay.
          </p>
        </div>
      )}

      {/* ── Download Button section temporarily removed ── */}
    </div>
  );
};

const MyPayslips = () => {
  const { showToast } = useToast();
  const [payrolls, setPayrolls] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMyPayroll = async () => {
      try {
        setLoading(true);
        const response = await api.get('/payroll/my');
        setPayrolls(response.data);
      } catch (error) {
        console.error('Failed to fetch payroll history', error);
        showToast('Failed to load payslips.', 'error');
      } finally {
        setLoading(false);
      }
    };
    fetchMyPayroll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDownload = async (payrollId) => {
    try {
      showToast('Generating your payslip...', 'info');
      const response = await api.get(`/payslip/${payrollId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `payslip-${payrollId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast('Payslip downloaded! 📄', 'success');
    } catch {
      showToast('Failed to download payslip. Please try again.', 'error');
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">My Payslips</h1>
        <p className="text-slate-500 text-sm mt-1">View your salary breakdown and download monthly statements.</p>
      </div>

      {loading ? (
        <div className="py-20 text-center">
          <Loader2 className="animate-spin text-primary-500 mx-auto" size={32} />
        </div>
      ) : payrolls.length === 0 ? (
        <div className="py-32 bg-white rounded-3xl border border-slate-100 text-center shadow-sm">
          <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <Wallet size={32} className="text-slate-300" />
          </div>
          <h3 className="text-slate-800 font-bold">No payslips yet</h3>
          <p className="text-slate-400 text-sm mt-1">Your salary statements will appear here once processed.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          {payrolls.map((payroll) => (
            <PayslipCard key={payroll.id} payroll={payroll} onDownload={handleDownload} />
          ))}
        </div>
      )}
    </div>
  );
};

export default MyPayslips;