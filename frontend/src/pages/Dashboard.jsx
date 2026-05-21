import { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { 
  Users, 
  Wallet, 
  CalendarClock, 
  TrendingUp,
  ArrowUpRight,
  ArrowDownRight,
  Loader2,
  Calendar,
  FileText,
  Clock,
  CheckCircle2
} from 'lucide-react';
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  BarChart,
  Bar,
  Cell
} from 'recharts';
import { format } from 'date-fns';

const StatCard = ({ label, value, icon: Icon, color, trend }) => (
  <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
    <div className="flex items-start justify-between">
      <div className={`p-3 rounded-2xl ${color} bg-opacity-10 text-opacity-100`}>
        <Icon size={24} className={color.replace('bg-', 'text-')} />
      </div>
      {trend !== undefined && (
        <span className={`flex items-center gap-1 text-xs font-bold ${trend > 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
          {trend > 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
          {Math.abs(trend)}%
        </span>
      )}
    </div>
    <div className="mt-4">
      <h3 className="text-slate-500 text-sm font-medium">{label}</h3>
      <p className="text-2xl font-bold text-slate-900 mt-1">{value}</p>
    </div>
  </div>
);

const AdminDashboard = ({ data }) => {
  const chartData = data?.payrollTrend || [];

  const deptData = Object.entries(data?.departmentHeadcount || {}).map(([name, value]) => ({
    name,
    value
  }));

  const COLORS = ['#0ea5e9', '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e'];

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <StatCard 
          label="Total Employees" 
          value={data?.totalEmployees || 0} 
          icon={Users} 
          color="bg-blue-500" 
          trend={12}
        />
        <StatCard 
          label="Payroll Cost (Month)" 
          value={`RM ${data?.totalPayrollCost?.toLocaleString() || 0}`} 
          icon={Wallet} 
          color="bg-emerald-500" 
          trend={5.4}
        />
        <StatCard 
          label="Pending Leaves" 
          value={data?.pendingLeaves || 0} 
          icon={CalendarClock} 
          color="bg-amber-500" 
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <div className="flex items-center justify-between mb-8">
            <h3 className="font-bold text-slate-800 flex items-center gap-2">
              <TrendingUp size={20} className="text-primary-500" />
              Payroll Trend
            </h3>
          </div>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
                <Tooltip contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }} />
                <Line type="monotone" dataKey="cost" stroke="#0ea5e9" strokeWidth={4} dot={{ r: 4, fill: '#0ea5e9', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 6, strokeWidth: 0 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <h3 className="font-bold text-slate-800 mb-8">Headcount per Department</h3>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={deptData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12, fontWeight: 500}} width={100} />
                <Tooltip cursor={{fill: '#f8fafc'}} />
                <Bar dataKey="value" radius={[0, 8, 8, 0]} barSize={24}>
                  {deptData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

const EmployeeDashboard = ({ data }) => {
  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <StatCard 
          label="Last Net Salary" 
          value={`RM ${data?.lastNetSalary?.toLocaleString() || '0.00'}`} 
          icon={Wallet} 
          color="bg-emerald-500" 
        />
        <StatCard 
          label="Leaves Taken" 
          value={`${data?.totalLeavesTaken || 0} Days`} 
          icon={Calendar} 
          color="bg-blue-500" 
        />
        <StatCard 
          label="Pending Requests" 
          value={data?.pendingLeaves || 0} 
          icon={Clock} 
          color="bg-amber-500" 
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Recent Payslips */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm">
          <h3 className="font-bold text-slate-800 mb-6 flex items-center gap-2">
            <FileText size={20} className="text-primary-500" />
            Recent Payslips
          </h3>
          <div className="space-y-4">
            {data?.recentPayrolls?.length > 0 ? (
              data.recentPayrolls.map((payroll) => (
                <div key={payroll.id} className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 border border-slate-100 group hover:border-primary-100 hover:bg-white transition-all">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-white flex items-center justify-center text-slate-400 group-hover:text-primary-600 shadow-sm transition-colors">
                      <FileText size={20} />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-800">
                        {format(new Date(2024, payroll.month - 1, 1), 'MMMM')} {payroll.year}
                      </p>
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Net: RM {payroll.netSalary.toFixed(2)}</p>
                    </div>
                  </div>
                  <button className="p-2 text-slate-400 hover:text-primary-600 transition-colors">
                    <ArrowUpRight size={18} />
                  </button>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-400 text-center py-8 italic">No payroll records found.</p>
            )}
          </div>
        </div>

        {/* Recent Leaves */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm">
          <h3 className="font-bold text-slate-800 mb-6 flex items-center gap-2">
            <CheckCircle2 size={20} className="text-primary-500" />
            Leave Status
          </h3>
          <div className="space-y-4">
            {data?.recentLeaves?.length > 0 ? (
              data.recentLeaves.map((leave) => (
                <div key={leave.id} className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 border border-slate-100">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-white flex items-center justify-center text-slate-400 shadow-sm">
                      <Calendar size={20} />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-800">{leave.leaveType}</p>
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                        {format(new Date(leave.startDate), 'dd MMM')} - {format(new Date(leave.endDate), 'dd MMM')}
                      </p>
                    </div>
                  </div>
                  <span className={`px-2 py-1 rounded-lg text-[9px] font-black uppercase tracking-widest ${
                    leave.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-600' :
                    leave.status === 'REJECTED' ? 'bg-rose-50 text-rose-600' : 'bg-amber-50 text-amber-600'
                  }`}>
                    {leave.status}
                  </span>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-400 text-center py-8 italic">No leave requests found.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const Dashboard = () => {
  const { isAdmin } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true);
        const endpoint = isAdmin ? '/dashboard/summary' : '/dashboard/my';
        const response = await api.get(endpoint);
        setData(response.data);
      } catch (error) {
        console.error('Failed to fetch dashboard data', error);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, [isAdmin]);

  if (loading) {
    return (
      <div className="h-[60vh] flex items-center justify-center">
        <Loader2 className="animate-spin text-primary-500" size={40} />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          {isAdmin ? 'Dashboard Overview' : `Welcome back, ${data?.fullName?.split(' ')[0] || 'User'}`}
        </h1>
        <p className="text-slate-500 text-sm mt-1">
          {isAdmin ? "Welcome back, here's what's happening today." : "Here's a quick look at your current status."}
        </p>
      </div>

      {isAdmin ? <AdminDashboard data={data} /> : <EmployeeDashboard data={data} />}
    </div>
  );
};

export default Dashboard;