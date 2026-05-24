import React, { useState, useEffect } from 'react';
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
  CheckCircle2,
  UserCheck,
  BarChart3,
  Award,
  Activity
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
  Cell,
  PieChart,
  Pie,
  Legend,
  AreaChart,
  Area
} from 'recharts';
import { format } from 'date-fns';

const COLORS = ['#0ea5e9', '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f59e0b', '#10b981'];

const StatCard = ({ label, value, icon: Icon, color, trend, subtitle }) => {
  const colorMap = {
    'emerald': { bg: 'bg-emerald-50', text: 'text-emerald-600', border: 'border-emerald-100' },
    'blue': { bg: 'bg-blue-50', text: 'text-blue-600', border: 'border-blue-100' },
    'amber': { bg: 'bg-amber-50', text: 'text-amber-600', border: 'border-amber-100' },
    'violet': { bg: 'bg-violet-50', text: 'text-violet-600', border: 'border-violet-100' },
    'rose': { bg: 'bg-rose-50', text: 'text-rose-600', border: 'border-rose-100' },
    'cyan': { bg: 'bg-cyan-50', text: 'text-cyan-600', border: 'border-cyan-100' },
  };
  const c = colorMap[color] || colorMap['blue'];

  return (
    <div className={`bg-white p-6 rounded-3xl border ${c.border} shadow-sm hover:shadow-md transition-all duration-300 group`}>
      <div className="flex items-start justify-between">
        <div className={`p-3 rounded-2xl ${c.bg} ${c.text} group-hover:scale-110 transition-transform duration-200`}>
          <Icon size={22} />
        </div>
        {trend !== undefined && (
          <span className={`flex items-center gap-1 text-xs font-bold ${trend >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
            {trend >= 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
            {Math.abs(trend)}%
          </span>
        )}
      </div>
      <div className="mt-4">
        <h3 className="text-slate-500 text-xs font-bold uppercase tracking-widest">{label}</h3>
        <p className="text-2xl font-black text-slate-900 mt-1">{value}</p>
        {subtitle && <p className="text-xs text-slate-400 mt-1">{subtitle}</p>}
      </div>
    </div>
  );
};

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white border border-slate-100 rounded-2xl p-3 shadow-xl">
        <p className="text-xs font-bold text-slate-500 mb-2">{label}</p>
        {payload.map((entry, i) => (
          <p key={i} className="text-sm font-bold" style={{ color: entry.color }}>
            {entry.name}: {typeof entry.value === 'number' && entry.value > 100 ? `RM ${entry.value.toLocaleString()}` : entry.value}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

const AdminDashboard = ({ data }) => {
  const payrollTrend = data?.payrollTrend || [];
  const salaryCostTrend = data?.salaryCostTrend || [];

  const deptData = Object.entries(data?.departmentHeadcount || {}).map(([name, value]) => ({ name, value }));

  const leaveStatusData = Object.entries(data?.leaveStatusBreakdown || {}).map(([name, value]) => ({
    name: name.charAt(0) + name.slice(1).toLowerCase(),
    value
  }));

  const leaveTypeData = Object.entries(data?.leaveTypeBreakdown || {}).map(([name, value]) => ({
    name: name.charAt(0) + name.slice(1).toLowerCase(),
    value
  }));

  const topEarners = data?.topEarners || [];
  const leaveApprovalRate = data?.leaveApprovalRate || 0;
  const activeEmployees = data?.activeEmployees || 0;
  const totalEmployees = data?.totalEmployees || 0;
  const avgSalary = data?.avgSalary || 0;

  const activePercent = totalEmployees > 0 ? Math.round((activeEmployees / totalEmployees) * 100) : 0;

  return (
    <div className="space-y-8 animate-in fade-in duration-500">

      {/* KPI Cards Row 1 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          label="Total Employees"
          value={totalEmployees}
          icon={Users}
          color="blue"
          subtitle={`${activeEmployees} active`}
        />
        <StatCard
          label="Payroll Cost (Month)"
          value={`RM ${(data?.totalPayrollCost || 0).toLocaleString()}`}
          icon={Wallet}
          color="emerald"
        />
        <StatCard
          label="Avg. Basic Salary"
          value={`RM ${Number(avgSalary).toLocaleString()}`}
          icon={TrendingUp}
          color="violet"
        />
        <StatCard
          label="Pending Leaves"
          value={data?.pendingLeaves || 0}
          icon={CalendarClock}
          color="amber"
        />
      </div>

      {/* KPI Cards Row 2 */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {/* Active Rate Card */}
        <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-emerald-50 text-emerald-600">
                <UserCheck size={18} />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Active Rate</p>
                <p className="text-xl font-black text-slate-900">{activePercent}%</p>
              </div>
            </div>
            <span className="text-xs font-bold text-slate-400">{activeEmployees}/{totalEmployees}</span>
          </div>
          <div className="w-full bg-slate-100 rounded-full h-2.5">
            <div
              className="bg-gradient-to-r from-emerald-400 to-emerald-600 h-2.5 rounded-full transition-all duration-700"
              style={{ width: `${activePercent}%` }}
            />
          </div>
        </div>

        {/* Leave Approval Rate */}
        <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-blue-50 text-blue-600">
                <CheckCircle2 size={18} />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Leave Approval Rate</p>
                <p className="text-xl font-black text-slate-900">{leaveApprovalRate}%</p>
              </div>
            </div>
            <span className={`text-xs font-bold px-2 py-1 rounded-lg ${leaveApprovalRate >= 75 ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'}`}>
              {leaveApprovalRate >= 75 ? 'Good' : 'Review'}
            </span>
          </div>
          <div className="w-full bg-slate-100 rounded-full h-2.5">
            <div
              className="bg-gradient-to-r from-blue-400 to-blue-600 h-2.5 rounded-full transition-all duration-700"
              style={{ width: `${leaveApprovalRate}%` }}
            />
          </div>
        </div>

        {/* Smart Insight */}
        <div className="bg-gradient-to-br from-indigo-600 to-violet-700 p-6 rounded-3xl text-white">
          <div className="flex items-center gap-2 mb-3">
            <Activity size={16} className="text-indigo-200" />
            <span className="text-xs font-black uppercase tracking-widest text-indigo-200">System Health</span>
          </div>
          <p className="text-sm font-medium text-indigo-100 leading-relaxed">
            {(data?.pendingLeaves || 0) > 3
              ? `${data.pendingLeaves} leave requests pending review. Action recommended.`
              : activePercent >= 90
              ? `Workforce is at ${activePercent}% capacity. Operations running smoothly.`
              : `${totalEmployees} total employees with RM ${Number(avgSalary).toLocaleString()} average salary.`
            }
          </p>
        </div>
      </div>

      {/* Charts Row 1: Area Chart + Donut */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

        {/* Gross vs Net Salary Area Chart */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-bold text-slate-800 flex items-center gap-2">
              <TrendingUp size={18} className="text-violet-500" />
              Gross vs Net Salary Trend
            </h3>
            <span className="text-xs text-slate-400 font-medium">Last 6 months</span>
          </div>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={salaryCostTrend}>
                <defs>
                  <linearGradient id="grossGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="netGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 12 }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 11 }} tickFormatter={v => `RM ${(v / 1000).toFixed(0)}k`} />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '16px' }} />
                <Area type="monotone" dataKey="gross" name="Gross" stroke="#8b5cf6" strokeWidth={2.5} fill="url(#grossGrad)" dot={{ r: 3, fill: '#8b5cf6' }} />
                <Area type="monotone" dataKey="net" name="Net" stroke="#0ea5e9" strokeWidth={2.5} fill="url(#netGrad)" dot={{ r: 3, fill: '#0ea5e9' }} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Leave Status Donut */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <h3 className="font-bold text-slate-800 flex items-center gap-2 mb-6">
            <Calendar size={18} className="text-amber-500" />
            Leave Status Breakdown
          </h3>
          {leaveStatusData.length > 0 ? (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={leaveStatusData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={90}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {leaveStatusData.map((entry, index) => (
                      <Cell key={index} fill={
                        entry.name === 'Approved' ? '#10b981' :
                        entry.name === 'Rejected' ? '#f43f5e' : '#f59e0b'
                      } />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v, n) => [v, n]} />
                  <Legend wrapperStyle={{ fontSize: '12px' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-64 flex items-center justify-center text-slate-400 text-sm">No leave data yet</div>
          )}
        </div>
      </div>

      {/* Charts Row 2: Dept Headcount + Leave Type */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">

        {/* Dept Headcount */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <h3 className="font-bold text-slate-800 flex items-center gap-2 mb-6">
            <BarChart3 size={18} className="text-blue-500" />
            Headcount per Department
          </h3>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={deptData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12, fontWeight: 500 }} width={110} />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: '#f8fafc' }} />
                <Bar dataKey="value" name="Employees" radius={[0, 8, 8, 0]} barSize={22}>
                  {deptData.map((entry, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Leave Type Bar Chart */}
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm min-w-0">
          <h3 className="font-bold text-slate-800 flex items-center gap-2 mb-6">
            <Calendar size={18} className="text-rose-500" />
            Leave Type Distribution
          </h3>
          {leaveTypeData.length > 0 ? (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={leaveTypeData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 12 }} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fill: '#94a3b8', fontSize: 12 }} />
                  <Tooltip content={<CustomTooltip />} cursor={{ fill: '#f8fafc' }} />
                  <Bar dataKey="value" name="Count" radius={[8, 8, 0, 0]} barSize={40}>
                    {leaveTypeData.map((entry, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-64 flex items-center justify-center text-slate-400 text-sm">No leave data yet</div>
          )}
        </div>
      </div>

      {/* Top 5 Earners Table */}
      {topEarners.length > 0 && (
        <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm">
          <h3 className="font-bold text-slate-800 flex items-center gap-2 mb-6">
            <Award size={18} className="text-amber-500" />
            Top 5 Highest Earners
          </h3>
          <div className="space-y-3">
            {topEarners.map((earner, index) => (
              <div key={index} className="flex items-center justify-between p-4 rounded-2xl bg-slate-50/70 hover:bg-slate-50 transition-colors group">
                <div className="flex items-center gap-4">
                  <div className={`w-8 h-8 rounded-xl flex items-center justify-center font-black text-sm ${
                    index === 0 ? 'bg-amber-100 text-amber-600' :
                    index === 1 ? 'bg-slate-200 text-slate-600' :
                    index === 2 ? 'bg-orange-100 text-orange-600' :
                    'bg-slate-100 text-slate-500'
                  }`}>
                    {index + 1}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-slate-800">{earner.name}</p>
                    <p className="text-xs text-slate-400 font-medium">{earner.department}</p>
                  </div>
                </div>
                <span className="text-sm font-black text-emerald-600 bg-emerald-50 px-3 py-1 rounded-xl">
                  RM {Number(earner.netSalary).toLocaleString()}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
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
          color="emerald"
        />
        <StatCard
          label="Leaves Taken"
          value={`${data?.totalLeavesTaken || 0} Days`}
          icon={Calendar}
          color="blue"
        />
        <StatCard
          label="Pending Requests"
          value={data?.pendingLeaves || 0}
          icon={Clock}
          color="amber"
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
  const { user, isAdmin } = useAuth();
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
          {isAdmin ? "Here's your real-time HR analytics & insights." : "Here's a quick look at your current status."}
        </p>
      </div>

      {isAdmin ? <AdminDashboard data={data} /> : <EmployeeDashboard data={data} />}
    </div>
  );
};

export default Dashboard;