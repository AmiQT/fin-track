import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import { 
  FileText, 
  Download, 
  PlayCircle, 
  Search, 
  Calendar,
  Loader2,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

const Payroll = () => {
  const { showToast } = useToast();
  const [payrolls, setPayrolls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [year, setYear] = useState(new Date().getFullYear());
  const [processing, setProcessing] = useState(false);

  const fetchPayroll = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/payroll/summary/${month}/${year}`);
      setPayrolls(response.data);
    } catch (error) {
      console.error('Failed to fetch payroll', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPayroll(); }, [month, year]);

  const handleProcessPayroll = async () => {
    try {
      setProcessing(true);
      await api.post('/payroll/process', { month, year });
      showToast('Payroll processed successfully! 💸', 'success');
      fetchPayroll();
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to process payroll.';
      showToast(msg, 'error');
    } finally {
      setProcessing(false);
    }
  };

  const handleDownloadPdf = async (payrollId) => {
    try {
      const response = await api.get(`/payslip/${payrollId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `payslip-${payrollId}.pdf`);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      showToast('Failed to download PDF.', 'error');
    }
  };

  const handleExportCsv = async () => {
    try {
      showToast('Exporting payroll data...', 'loading');
      const response = await api.get(`/payroll/export/${month}/${year}`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `payroll_${month}_${year}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast('Payroll data exported! 📊', 'success');
    } catch (error) {
      showToast('Failed to export payroll data.', 'error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Payroll Processing</h1>
          <p className="text-slate-500 text-sm mt-1">Manage monthly salary disbursements and generate payslips.</p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button 
            onClick={handleExportCsv}
            disabled={payrolls.length === 0}
            className="flex items-center justify-center gap-2 px-6 py-3 bg-white border border-slate-200 text-slate-700 rounded-2xl font-bold shadow-sm hover:bg-slate-50 transition-all active:scale-95 disabled:opacity-50"
          >
            <Download size={20} />
            Export CSV
          </button>
          <div className="flex bg-white border border-slate-200 p-1 rounded-xl shadow-sm">
            <select 
              value={month} 
              onChange={(e) => setMonth(parseInt(e.target.value))}
              className="px-3 py-2 text-sm font-bold text-slate-700 bg-transparent outline-none cursor-pointer"
            >
              {Array.from({length: 12}, (_, i) => (
                <option key={i+1} value={i+1}>{new Date(0, i).toLocaleString('en', {month: 'long'})}</option>
              ))}
            </select>
            <select 
              value={year} 
              onChange={(e) => setYear(parseInt(e.target.value))}
              className="px-3 py-2 text-sm font-bold text-slate-700 bg-transparent border-l border-slate-100 outline-none cursor-pointer"
            >
              {[2024, 2025, 2026].map(y => <option key={y} value={y}>{y}</option>)}
            </select>
          </div>
          <button 
            onClick={handleProcessPayroll}
            disabled={processing}
            className="flex items-center gap-2 px-6 py-3 bg-emerald-600 text-white rounded-2xl font-bold shadow-lg shadow-emerald-600/20 hover:bg-emerald-700 transition-all disabled:opacity-50"
          >
            {processing ? <Loader2 className="animate-spin" size={20} /> : <PlayCircle size={20} />}
            Process Payroll
          </button>
        </div>
      </div>

      <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-slate-50/50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <th className="px-6 py-4">Employee</th>
                <th className="px-6 py-4 text-right">Gross (RM)</th>
                <th className="px-6 py-4 text-right">Deductions (RM)</th>
                <th className="px-6 py-4 text-right">Net Salary (RM)</th>
                <th className="px-6 py-4 text-center">Status</th>
                <th className="px-6 py-4 text-center">Payslip</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center">
                    <Loader2 className="animate-spin text-primary-500 mx-auto" size={32} />
                  </td>
                </tr>
              ) : payrolls.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center text-slate-400 font-medium">
                    No payroll data found for this period.
                  </td>
                </tr>
              ) : (
                payrolls.map((payroll) => (
                  <tr key={payroll.id} className="hover:bg-slate-50/50 transition-colors">
                    <td className="px-6 py-4">
                      <p className="text-sm font-bold text-slate-800">{payroll.employeeName}</p>
                      <p className="text-[10px] text-slate-400 font-medium uppercase tracking-tight">ID: {payroll.id}</p>
                    </td>
                    <td className="px-6 py-4 text-right font-bold text-slate-700">
                      {payroll.grossSalary.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 text-right font-bold text-rose-500">
                      -{payroll.totalDeductions.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span className="inline-block px-3 py-1 bg-emerald-50 text-emerald-700 rounded-lg font-black">
                        {payroll.netSalary.toLocaleString()}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className="px-2 py-0.5 bg-blue-50 text-blue-600 rounded-md text-[10px] font-bold">PROCESSED</span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <button 
                        onClick={() => handleDownloadPdf(payroll.id)}
                        className="p-2 text-primary-600 hover:bg-primary-50 rounded-xl transition-all"
                        title="Download PDF"
                      >
                        <Download size={18} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Payroll;