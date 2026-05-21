import { useState, useEffect } from 'react';
import api from '../services/api';
import { useToast } from '../context/ToastContext';
import { 
  User,
  Mail,
  Phone,
  Briefcase,
  Building2,
  Calendar,
  Wallet,
  ShieldCheck,
  Loader2,
  X,
  CheckCircle2,
  AlertCircle,
  KeyRound,
  Hash
} from 'lucide-react';
import { format } from 'date-fns';

const InfoRow = ({ icon: Icon, label, value, iconColor }) => (
  <div className="flex items-center gap-4 py-3.5 border-b border-slate-50 last:border-0">
    <div className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 ${iconColor || 'bg-slate-100 text-slate-400'}`}>
      <Icon size={15} />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{label}</p>
      <p className="text-sm font-semibold text-slate-800 truncate">{value || 'Not provided'}</p>
    </div>
  </div>
);

const SalaryItem = ({ label, value, highlight }) => (
  <div className={`flex items-center justify-between py-3 border-b border-slate-50 last:border-0 ${highlight ? 'pt-4 border-t border-slate-200 mt-2' : ''}`}>
    <span className={`text-sm ${highlight ? 'font-bold text-slate-900' : 'text-slate-500'}`}>{label}</span>
    <span className={`text-sm font-bold ${highlight ? 'text-primary-600 text-base' : 'text-slate-700'}`}>{value}</span>
  </div>
);

const MyProfile = () => {
  const { showToast } = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordStatus, setPasswordStatus] = useState({ loading: false, message: '', type: '' });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const response = await api.get('/employees/me');
        setProfile(response.data);
      } catch (error) {
        console.error('Failed to fetch profile', error);
        showToast('Failed to load profile data.', 'error');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      showToast('Passwords do not match!', 'error');
      return;
    }
    try {
      setPasswordStatus({ loading: true, message: '', type: '' });
      await api.post('/auth/change-password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      });
      showToast('Password updated successfully! 🔐', 'success');
      setShowPasswordModal(false);
      setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
      setPasswordStatus({ loading: false, message: '', type: '' });
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to update password. Check old password.';
      showToast(msg, 'error');
      setPasswordStatus({ loading: false, message: msg, type: 'error' });
    }
  };

  if (loading) {
    return (
      <div className="h-[60vh] flex items-center justify-center">
        <Loader2 className="animate-spin text-primary-500" size={40} />
      </div>
    );
  }

  const gross = (profile.basicSalary || 0) + (profile.housingAllowance || 0) + (profile.transportAllowance || 0);

  return (
    <div className="max-w-5xl mx-auto space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      
      {/* ── Profile Card ─────────────────────────────── */}
      <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
        {/* Avatar + Name Row */}
        <div className="px-8 py-6">
          <div className="flex flex-col sm:flex-row sm:items-center gap-5">
            {/* Avatar */}
            <div className="w-24 h-24 rounded-2xl bg-white p-1.5 shadow-xl flex-shrink-0">
              <div className="w-full h-full rounded-xl bg-primary-50 flex items-center justify-center text-primary-400">
                <User size={40} strokeWidth={1.5} />
              </div>
            </div>

            {/* Name & meta */}
            <div className="flex-1 pb-1">
              <h1 className="text-2xl font-black text-slate-900 leading-tight">{profile.fullName || 'Employee'}</h1>
              <div className="flex flex-wrap items-center gap-3 mt-1.5">
                <span className="flex items-center gap-1 text-xs font-semibold text-slate-500">
                  <Hash size={12} className="text-primary-400" />
                  {profile.employeeCode || '—'}
                </span>
                <span className="flex items-center gap-1 text-xs font-semibold text-slate-500">
                  <Briefcase size={12} className="text-primary-400" />
                  {profile.position || '—'}
                </span>
                <span className="flex items-center gap-1 text-xs font-semibold text-slate-500">
                  <Building2 size={12} className="text-primary-400" />
                  {profile.department || '—'}
                </span>
                <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${
                  profile.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-rose-50 text-rose-600 border-rose-100'
                }`}>
                  {profile.status || 'ACTIVE'}
                </span>
              </div>
            </div>

            {/* Change Password Button */}
            <button
              onClick={() => setShowPasswordModal(true)}
              className="flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white text-xs font-bold rounded-xl hover:bg-primary-600 transition-all shadow-sm flex-shrink-0"
            >
              <KeyRound size={14} />
              Change Password
            </button>
          </div>
        </div>
      </div>

      {/* ── Content Grid ─────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">

        {/* Personal Info — wider col */}
        <div className="lg:col-span-3 bg-white rounded-3xl border border-slate-100 shadow-sm p-6">
          <div className="flex items-center gap-2 mb-4">
            <div className="w-7 h-7 rounded-lg bg-primary-50 flex items-center justify-center">
              <User size={14} className="text-primary-600" />
            </div>
            <h3 className="text-sm font-bold text-slate-800">Personal Information</h3>
          </div>
          <InfoRow icon={Mail}      label="Email Address"  value={profile.email}       iconColor="bg-blue-50 text-blue-500" />
          <InfoRow icon={Phone}     label="Phone Number"   value={profile.phone}       iconColor="bg-indigo-50 text-indigo-500" />
          <InfoRow icon={Building2} label="Department"     value={profile.department}  iconColor="bg-amber-50 text-amber-500" />
          <InfoRow icon={Briefcase} label="Position"       value={profile.position}    iconColor="bg-emerald-50 text-emerald-500" />
          <InfoRow icon={Calendar}  label="Join Date"      value={profile.joinDate ? format(new Date(profile.joinDate), 'dd MMMM yyyy') : null} iconColor="bg-rose-50 text-rose-500" />
          <InfoRow icon={ShieldCheck} label="Role"         value={profile.role?.replace('ROLE_', '') || 'EMPLOYEE'} iconColor="bg-violet-50 text-violet-500" />
        </div>

        {/* Salary Panel — narrower col */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          {/* Base Salary Hero */}
          <div className="bg-gradient-to-br from-primary-600 to-indigo-600 rounded-3xl p-6 text-white shadow-lg shadow-primary-600/20 relative overflow-hidden">
            <div className="absolute -top-6 -right-6 w-32 h-32 bg-white/10 rounded-full" />
            <div className="absolute -bottom-8 -left-4 w-24 h-24 bg-white/10 rounded-full" />
            <p className="text-[10px] font-bold text-white/70 uppercase tracking-[0.2em] mb-1 relative">Base Salary</p>
            <p className="text-3xl font-black relative">
              RM {(profile.basicSalary || 0).toLocaleString()}
            </p>
            <p className="text-white/60 text-xs mt-1 relative">per month</p>
          </div>

          {/* Allowances breakdown */}
          <div className="bg-white rounded-3xl border border-slate-100 shadow-sm p-6 flex-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-7 h-7 rounded-lg bg-emerald-50 flex items-center justify-center">
                <Wallet size={14} className="text-emerald-600" />
              </div>
              <h3 className="text-sm font-bold text-slate-800">Allowances & Total</h3>
            </div>
            <SalaryItem label="Housing Allowance"   value={`RM ${(profile.housingAllowance || 0).toFixed(2)}`} />
            <SalaryItem label="Transport Allowance" value={`RM ${(profile.transportAllowance || 0).toFixed(2)}`} />
            <SalaryItem label="Total Gross Monthly"  value={`RM ${gross.toLocaleString(undefined, { minimumFractionDigits: 2 })}`} highlight />
          </div>
        </div>
      </div>

      {/* ── Change Password Modal ─────────────────────── */}
      {showPasswordModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 animate-in fade-in duration-300">
          <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => !passwordStatus.loading && setShowPasswordModal(false)}></div>
          <div className="bg-white rounded-[32px] w-full max-w-md relative z-10 shadow-2xl border border-slate-100 overflow-hidden animate-in zoom-in duration-300">
            <div className="p-8">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="text-xl font-bold text-slate-900">Change Password</h3>
                  <p className="text-slate-400 text-xs mt-0.5">Keep your account secure</p>
                </div>
                <button onClick={() => setShowPasswordModal(false)} className="p-2 hover:bg-slate-50 rounded-xl transition-colors">
                  <X size={20} className="text-slate-400" />
                </button>
              </div>

              {passwordStatus.message && (
                <div className={`mb-6 p-4 rounded-xl border flex items-center gap-3 animate-in slide-in-from-top-2 ${
                  passwordStatus.type === 'success' ? 'bg-emerald-50 border-emerald-100 text-emerald-600' : 'bg-rose-50 border-rose-100 text-rose-600'
                }`}>
                  {passwordStatus.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
                  <p className="text-xs font-bold">{passwordStatus.message}</p>
                </div>
              )}

              <form onSubmit={handlePasswordChange} className="space-y-4">
                {[
                  { key: 'oldPassword', label: 'Current Password' },
                  { key: 'newPassword', label: 'New Password' },
                  { key: 'confirmPassword', label: 'Confirm New Password' }
                ].map(({ key, label }) => (
                  <div key={key}>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">{label}</label>
                    <input
                      type="password"
                      required
                      className="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-primary-500 outline-none transition-all"
                      value={passwordForm[key]}
                      onChange={(e) => setPasswordForm({ ...passwordForm, [key]: e.target.value })}
                    />
                  </div>
                ))}
                <button
                  type="submit"
                  disabled={passwordStatus.loading}
                  className="w-full py-4 bg-primary-600 text-white rounded-2xl font-bold text-sm shadow-lg shadow-primary-600/20 hover:bg-primary-700 transition-all flex items-center justify-center gap-2 disabled:opacity-50 mt-4"
                >
                  {passwordStatus.loading ? <Loader2 className="animate-spin" size={20} /> : 'Update Password'}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyProfile;