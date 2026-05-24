import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import { 
  Plus, 
  Search, 
  Edit2, 
  Trash2, 
  MoreVertical,
  Loader2,
  Filter,
  UserPlus,
  X,
  Save,
  AlertCircle,
  Download,
  ChevronDown,
  FileSpreadsheet,
  FileText
} from 'lucide-react';
import { format } from 'date-fns';

const EmployeeModal = ({ isOpen, onClose, employee, onSave }) => {
  const [formData, setFormData] = useState({
    employeeCode: '', fullName: '', email: '', phone: '',
    department: '', position: '', basicSalary: '',
    housingAllowance: 0, transportAllowance: 0,
    joinDate: format(new Date(), 'yyyy-MM-dd'), password: ''
  });

  useEffect(() => {
    if (employee) {
      setFormData({ ...employee, joinDate: format(new Date(employee.joinDate), 'yyyy-MM-dd'), password: '' });
    } else {
      setFormData({
        employeeCode: '', fullName: '', email: '', phone: '',
        department: '', position: '', basicSalary: '',
        housingAllowance: 0, transportAllowance: 0,
        joinDate: format(new Date(), 'yyyy-MM-dd'), password: ''
      });
    }
  }, [employee, isOpen]);

  if (!isOpen) return null;

  const inputClass = "w-full bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary-500 transition-all";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300">
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={onClose}></div>
      <div className="bg-white rounded-[32px] w-full max-w-2xl relative z-10 shadow-2xl border border-slate-100 overflow-hidden animate-in zoom-in duration-300 max-h-[90vh] overflow-y-auto">
        <div className="p-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-slate-900">{employee ? 'Edit Employee' : 'Add New Employee'}</h2>
            <button onClick={onClose} className="p-2 hover:bg-slate-50 rounded-xl transition-colors">
              <X size={20} className="text-slate-400" />
            </button>
          </div>

          <form onSubmit={(e) => { e.preventDefault(); onSave(formData); }} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Employee Code</label>
                <input required placeholder="e.g. EMP001" className={inputClass} value={formData.employeeCode || ''} onChange={(e) => setFormData({...formData, employeeCode: e.target.value})} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Full Name</label>
                <input required className={inputClass} value={formData.fullName || ''} onChange={(e) => setFormData({...formData, fullName: e.target.value})} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Email Address</label>
                <input type="email" required className={inputClass} value={formData.email || ''} onChange={(e) => setFormData({...formData, email: e.target.value})} disabled={!!employee} />
              </div>
              {!employee && (
                <div>
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Initial Password</label>
                  <input type="password" placeholder="Leave blank for 'password123'" className={inputClass} value={formData.password || ''} onChange={(e) => setFormData({...formData, password: e.target.value})} />
                </div>
              )}
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Phone</label>
                <input className={inputClass} value={formData.phone || ''} onChange={(e) => setFormData({...formData, phone: e.target.value})} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Department</label>
                <select className={inputClass} value={formData.department || ''} onChange={(e) => setFormData({...formData, department: e.target.value})}>
                  <option value="">Select Dept</option>
                  <option value="IT">IT</option>
                  <option value="HR">HR</option>
                  <option value="Sales">Sales</option>
                  <option value="Marketing">Marketing</option>
                  <option value="Finance">Finance</option>
                </select>
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Position</label>
                <input className={inputClass} value={formData.position || ''} onChange={(e) => setFormData({...formData, position: e.target.value})} />
              </div>
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-2">Join Date</label>
                <input type="date" required className={inputClass} value={formData.joinDate || ''} onChange={(e) => setFormData({...formData, joinDate: e.target.value})} />
              </div>
            </div>

            <div className="pt-6 border-t border-slate-100">
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-4 font-black">Financial Details (RM)</label>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <p className="text-[10px] text-slate-500 mb-1 ml-1 font-bold">Basic Salary</p>
                  <input type="number" required className={`${inputClass} font-bold`} value={formData.basicSalary || ''} onChange={(e) => setFormData({...formData, basicSalary: e.target.value})} />
                </div>
                <div>
                  <p className="text-[10px] text-slate-500 mb-1 ml-1 font-bold">Housing</p>
                  <input type="number" className={inputClass} value={formData.housingAllowance ?? 0} onChange={(e) => setFormData({...formData, housingAllowance: e.target.value})} />
                </div>
                <div>
                  <p className="text-[10px] text-slate-500 mb-1 ml-1 font-bold">Transport</p>
                  <input type="number" className={inputClass} value={formData.transportAllowance ?? 0} onChange={(e) => setFormData({...formData, transportAllowance: e.target.value})} />
                </div>
              </div>
            </div>

            <div className="flex gap-4 pt-4">
              <button type="button" onClick={onClose} className="flex-1 py-4 px-6 rounded-2xl font-bold text-sm text-slate-500 hover:bg-slate-50 transition-all">
                Cancel
              </button>
              <button type="submit" className="flex-1 py-4 px-6 bg-primary-600 text-white rounded-2xl font-bold text-sm shadow-lg shadow-primary-600/20 hover:bg-primary-700 transition-all flex items-center justify-center gap-2">
                <Save size={18} />
                {employee ? 'Update Employee' : 'Create Employee'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

const Employees = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('query') || '';
  const { showToast } = useToast();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState(null);

  useEffect(() => {
    const q = searchParams.get('query') || '';
    setSearchQuery(q);
  }, [searchParams]);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const url = searchQuery 
        ? `/employees/search?query=${searchQuery}&page=${page}&size=10`
        : `/employees?page=${page}&size=10`;
      const response = await api.get(url);
      setEmployees(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Failed to fetch employees', error);
      showToast('Failed to load employees.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => { fetchEmployees(); }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery, page]);

  const handleSaveEmployee = async (formData) => {
    try {
      if (selectedEmployee) {
        await api.put(`/employees/${selectedEmployee.id}`, formData);
        showToast('Employee updated successfully.', 'success');
      } else {
        await api.post('/employees', formData);
        showToast('New employee added successfully.', 'success');
      }
      setIsModalOpen(false);
      fetchEmployees();
    } catch (error) {
      showToast(error.response?.data?.message || 'Failed to save employee', 'error');
    }
  };

  const handleDeleteEmployee = async (id) => {
    if (window.confirm('Are you sure you want to deactivate this employee?')) {
      try {
        await api.delete(`/employees/${id}`);
        showToast('Employee deactivated successfully.', 'success');
        fetchEmployees();
      } catch (error) {
        showToast('Failed to delete employee', 'error');
      }
    }
  };

  const [exportDropdownOpen, setExportDropdownOpen] = useState(false);

  const handleExportCsv = async () => {
    setExportDropdownOpen(false);
    try {
      showToast('Preparing CSV export...', 'loading');
      const response = await api.get('/employees/export', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'employees.csv');
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast('CSV exported successfully.', 'success');
    } catch (error) {
      showToast('Failed to export CSV.', 'error');
    }
  };

  const handleExportExcel = async () => {
    setExportDropdownOpen(false);
    try {
      showToast('Generating Excel file...', 'loading');
      const response = await api.get('/employees/export/excel', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'employees.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
      showToast('Excel exported successfully.', 'success');
    } catch (error) {
      showToast('Failed to export Excel.', 'error');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Employee Directory</h1>
          <p className="text-slate-500 text-sm mt-1">Manage and monitor your workforce efficiently.</p>
        </div>
        <div className="flex items-center gap-3">
          {/* Export Dropdown */}
          <div className="relative">
            <button
              onClick={() => setExportDropdownOpen(o => !o)}
              className="flex items-center gap-2 px-5 py-3 bg-white border border-slate-200 text-slate-700 rounded-2xl font-bold shadow-sm hover:bg-slate-50 transition-all active:scale-95"
            >
              <Download size={18} />
              Export
              <ChevronDown size={14} className={`transition-transform ${exportDropdownOpen ? 'rotate-180' : ''}`} />
            </button>
            {exportDropdownOpen && (
              <div className="absolute right-0 mt-2 w-44 bg-white border border-slate-100 rounded-2xl shadow-xl z-20 overflow-hidden animate-in fade-in zoom-in duration-200">
                <button
                  onClick={handleExportCsv}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition-colors"
                >
                  <FileText size={16} className="text-slate-400" />
                  Export CSV
                </button>
                <button
                  onClick={handleExportExcel}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm font-bold text-emerald-700 hover:bg-emerald-50 transition-colors border-t border-slate-50"
                >
                  <FileSpreadsheet size={16} className="text-emerald-500" />
                  Export Excel
                </button>
              </div>
            )}
          </div>
          <button onClick={() => { setSelectedEmployee(null); setIsModalOpen(true); }} className="flex items-center justify-center gap-2 px-6 py-3 bg-primary-600 text-white rounded-2xl font-bold shadow-lg shadow-primary-600/20 hover:bg-primary-700 transition-all active:scale-95">
            <UserPlus size={20} />
            Add New Employee
          </button>
        </div>
      </div>

      <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-50 flex flex-col md:flex-row gap-4 items-center justify-between">
          <div className="relative w-full md:w-96">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
            <input 
              type="text" 
              placeholder="Search by name or department..." 
              className="w-full pl-11 pr-4 py-2.5 bg-slate-50 border border-slate-100 rounded-xl text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 transition-all outline-none"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div className="flex items-center gap-2">
            <button className="p-2.5 bg-slate-50 text-slate-500 rounded-xl hover:bg-slate-100 transition-colors">
              <Filter size={18} />
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-slate-50/50 text-slate-500 text-[11px] font-bold uppercase tracking-wider">
                <th className="px-6 py-4">Employee</th>
                <th className="px-6 py-4">ID & Dept</th>
                <th className="px-6 py-4">Position</th>
                <th className="px-6 py-4 text-right">Basic Salary</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center">
                    <Loader2 className="animate-spin text-primary-500 mx-auto" size={32} />
                    <p className="text-sm text-slate-400 mt-4 font-medium">Loading team members...</p>
                  </td>
                </tr>
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-20 text-center">
                    <p className="text-slate-500 font-medium">No employees found.</p>
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.id} className="hover:bg-slate-50/50 transition-colors group">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 font-bold">
                          {emp.fullName.charAt(0)}
                        </div>
                        <div>
                          <p className="text-sm font-bold text-slate-800">{emp.fullName}</p>
                          <p className="text-xs text-slate-400">{emp.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <p className="text-xs font-bold text-slate-700">{emp.employeeCode}</p>
                      <p className="text-[10px] font-medium text-primary-600 bg-primary-50 px-2 py-0.5 rounded-full inline-block mt-1">
                        {emp.department}
                      </p>
                    </td>
                    <td className="px-6 py-4">
                      <p className="text-xs text-slate-600 font-medium">{emp.position}</p>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <p className="text-sm font-bold text-slate-800">RM {emp.basicSalary.toLocaleString()}</p>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider uppercase ${
                        emp.status === 'ACTIVE' 
                          ? 'bg-emerald-50 text-emerald-600' 
                          : 'bg-rose-50 text-rose-600'
                      }`}>
                        {emp.status}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-center gap-1">
                        <button onClick={() => { setSelectedEmployee(emp); setIsModalOpen(true); }} className="p-2 text-slate-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-all">
                          <Edit2 size={16} />
                        </button>
                        <button onClick={() => handleDeleteEmployee(emp.id)} className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-all">
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="p-6 bg-slate-50/50 border-t border-slate-50 flex items-center justify-between">
          <p className="text-xs text-slate-500 font-medium">
            Showing <span className="text-slate-900">{employees.length}</span> employees
          </p>
          <div className="flex gap-2">
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-4 py-2 text-xs font-bold text-slate-600 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 disabled:opacity-50 transition-all">
              Previous
            </button>
            <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-4 py-2 text-xs font-bold text-white bg-primary-600 rounded-xl shadow-md shadow-primary-600/10 hover:bg-primary-700 disabled:opacity-50 transition-all">
              Next
            </button>
          </div>
        </div>
      </div>

      <EmployeeModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        employee={selectedEmployee}
        onSave={handleSaveEmployee}
      />
    </div>
  );
};

export default Employees;