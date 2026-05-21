import { useState } from 'react';
import { NavLink, useNavigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Wallet,
  CalendarRange,
  LogOut,
  Bell,
  Search,
  User as UserIcon,
  ChevronRight,
  Menu,
  X,
  Inbox,
} from 'lucide-react';

const SidebarLink = ({ to, icon: Icon, label, onClick }) => (
  <NavLink
    to={to}
    onClick={onClick}
    className={({ isActive }) => `
      flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 group
      ${isActive 
        ? 'bg-primary-600 text-white shadow-lg shadow-primary-600/20' 
        : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'}
    `}
  >
    <Icon size={20} className="shrink-0" />
    <span className="font-medium text-sm">{label}</span>
    <ChevronRight size={14} className="ml-auto opacity-0 group-hover:opacity-100 transition-opacity" />
  </NavLink>
);

const NotificationItem = ({ title, time }) => (
  <div className="p-4 hover:bg-slate-50 transition-colors border-b border-slate-50 last:border-0 cursor-pointer group">
    <div className="flex justify-between items-start mb-1">
      <h4 className="text-xs font-bold text-slate-800 group-hover:text-primary-600 transition-colors">{title}</h4>
      <span className="text-[9px] font-medium text-slate-400">{time}</span>
    </div>
    <p className="text-[10px] text-slate-500 leading-relaxed">System activity recorded for your account.</p>
  </div>
);

const Layout = () => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim() && isAdmin) {
      navigate(`/employees?query=${encodeURIComponent(searchQuery)}`);
      setSearchQuery('');
      setIsSidebarOpen(false);
    }
  };

  const closeSidebar = () => setIsSidebarOpen(false);

  const adminNotifications = [
    { title: 'New Leave Request: Ali Ahmad', time: '10m ago' },
    { title: 'Payroll Processed: May 2026', time: '2h ago' },
    { title: 'System Backup Successful', time: '5h ago' }
  ];

  const employeeNotifications = [
    { title: 'Payslip Ready: May 2026', time: '1h ago' },
    { title: 'Leave Approved: Annual Leave', time: '1d ago' },
    { title: 'Welcome to FinTrack Pro', time: '2d ago' }
  ];

  const notifications = isAdmin ? adminNotifications : employeeNotifications;

  return (
    <div className="flex min-h-screen w-full bg-slate-50">
      {/* Mobile Backdrop */}
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-30 lg:hidden"
          onClick={closeSidebar}
        ></div>
      )}

      {/* Sidebar */}
      <aside className={`
        w-72 border-r border-slate-200 bg-white flex flex-col fixed h-full z-40 transition-transform duration-300 lg:translate-x-0
        ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="p-6">
          <div className="flex items-center justify-between lg:justify-start gap-3 mb-8 px-2">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-primary-600 flex items-center justify-center text-white shadow-lg shadow-primary-600/20">
                <Wallet size={20} />
              </div>
              <span className="text-xl font-bold bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent">
                FinTrack Pro
              </span>
            </div>
            <button 
              onClick={closeSidebar}
              className="lg:hidden p-2 text-slate-400 hover:bg-slate-50 rounded-xl transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          <nav className="space-y-1.5">
            <p className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">Main Menu</p>
            <SidebarLink to="/" icon={LayoutDashboard} label="Dashboard" onClick={closeSidebar} />
            
            {isAdmin && (
              <>
                <SidebarLink to="/employees" icon={Users} label="Employees" onClick={closeSidebar} />
                <SidebarLink to="/payroll" icon={Wallet} label="Payroll" onClick={closeSidebar} />
                <SidebarLink to="/leaves" icon={CalendarRange} label="Leave Requests" onClick={closeSidebar} />
              </>
            )}

            {!isAdmin && (
              <>
                <SidebarLink to="/my-payslips" icon={Wallet} label="My Payslips" onClick={closeSidebar} />
                <SidebarLink to="/my-leaves" icon={CalendarRange} label="My Leaves" onClick={closeSidebar} />
                <SidebarLink to="/my-profile" icon={UserIcon} label="My Profile" onClick={closeSidebar} />
              </>
            )}
          </nav>
        </div>

        <div className="mt-auto p-6 space-y-4">
          <NavLink 
            to={isAdmin ? "#" : "/my-profile"}
            onClick={closeSidebar}
            className="block p-4 rounded-2xl bg-slate-50 border border-slate-100 hover:bg-slate-100 transition-colors group"
          >
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center text-primary-600 border-2 border-white shadow-sm group-hover:scale-110 transition-transform">
                <UserIcon size={20} />
              </div>
              <div className="overflow-hidden">
                <p className="text-sm font-bold text-slate-800 truncate">{user?.email.split('@')[0]}</p>
                <p className="text-[10px] font-medium text-slate-400 uppercase tracking-wider">{user?.role.replace('ROLE_', '')}</p>
              </div>
            </div>
          </NavLink>
          <button 
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 py-2 px-4 rounded-lg text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors"
          >
            <LogOut size={16} />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 lg:ml-72 min-w-0">
        {/* Header */}
        <header className="h-20 bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-10 flex items-center justify-between px-4 lg:px-8">
          <div className="flex items-center gap-3">
            <button 
              onClick={() => setIsSidebarOpen(true)}
              className="lg:hidden p-2.5 bg-slate-50 text-slate-500 rounded-xl hover:bg-slate-100 transition-colors"
            >
              <Menu size={20} />
            </button>
            <form onSubmit={handleSearch} className="relative w-48 md:w-96">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
              <input 
                type="text" 
                placeholder={isAdmin ? "Search employees..." : "Search..."} 
                className="w-full pl-11 pr-4 py-2.5 bg-slate-50 border border-slate-100 rounded-xl text-sm focus:bg-white focus:ring-2 focus:ring-primary-500 transition-all outline-none"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </form>
          </div>

          <div className="flex items-center gap-4 relative">
            <button 
              onClick={() => setIsNotificationsOpen(!isNotificationsOpen)}
              className={`w-10 h-10 rounded-xl flex items-center justify-center transition-all relative ${
                isNotificationsOpen ? 'bg-primary-50 text-primary-600 ring-2 ring-primary-100' : 'bg-slate-50 text-slate-500 hover:bg-slate-100'
              }`}
            >
              <Bell size={20} />
              <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-primary-500 rounded-full border-2 border-white"></span>
            </button>

            {/* Notifications Dropdown */}
            {isNotificationsOpen && (
              <>
                <div className="fixed inset-0 z-40" onClick={() => setIsNotificationsOpen(false)}></div>
                <div className="absolute top-14 right-0 w-80 bg-white rounded-3xl shadow-2xl border border-slate-100 z-50 overflow-hidden animate-in fade-in zoom-in-95 duration-200 origin-top-right">
                  <div className="p-5 border-b border-slate-50 flex items-center justify-between">
                    <h3 className="font-bold text-slate-800 flex items-center gap-2">
                      <Inbox size={16} className="text-primary-500" />
                      Notifications
                    </h3>
                    <span className="bg-primary-50 text-primary-600 text-[9px] font-black px-2 py-0.5 rounded-full uppercase tracking-tighter">
                      {notifications.length} New
                    </span>
                  </div>
                  <div className="max-h-96 overflow-y-auto">
                    {notifications.map((n, i) => (
                      <NotificationItem key={i} {...n} />
                    ))}
                  </div>
                  <button className="w-full py-4 text-[10px] font-black text-slate-400 uppercase tracking-widest hover:text-primary-600 hover:bg-slate-50 transition-all">
                    View All Activity
                  </button>
                </div>
              </>
            )}
          </div>
        </header>

        <div className="p-4 lg:p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;