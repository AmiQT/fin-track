import React, { createContext, useContext, useState, useCallback } from 'react';
import { 
  CheckCircle2, 
  AlertCircle, 
  Info, 
  X,
  Loader2
} from 'lucide-react';

const ToastContext = createContext(null);

const Toast = ({ message, type, onClose }) => {
  const icons = {
    success: <CheckCircle2 className="text-emerald-500" size={20} />,
    error: <AlertCircle className="text-rose-500" size={20} />,
    info: <Info className="text-blue-500" size={20} />,
    loading: <Loader2 className="text-primary-500 animate-spin" size={20} />
  };

  const bgStyles = {
    success: 'bg-white border-emerald-100',
    error: 'bg-white border-rose-100',
    info: 'bg-white border-blue-100',
    loading: 'bg-white border-slate-100'
  };

  return (
    <div className={`
      flex items-center gap-3 p-4 pr-6 rounded-2xl border shadow-xl shadow-slate-200/50 
      animate-in slide-in-from-right-8 fade-in duration-300 pointer-events-auto
      ${bgStyles[type] || bgStyles.info}
    `}>
      <div className="shrink-0">{icons[type] || icons.info}</div>
      <p className="text-sm font-bold text-slate-700 whitespace-nowrap">{message}</p>
      <button 
        onClick={onClose}
        className="ml-4 p-1 rounded-lg hover:bg-slate-50 text-slate-400 transition-colors"
      >
        <X size={14} />
      </button>
    </div>
  );
};

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, type = 'info', duration = 3000) => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);

    if (duration > 0) {
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-8 right-8 z-[100] flex flex-col gap-3 pointer-events-none">
        {toasts.map((toast) => (
          <Toast 
            key={toast.id} 
            {...toast} 
            onClose={() => removeToast(toast.id)} 
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used within a ToastProvider');
  return context;
};