import { useCallback, useEffect, useState } from 'react';

type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
  id: number;
  type: ToastType;
  message: string;
}

const typeStyles: Record<ToastType, string> = {
  success: 'bg-success-50 border-sage-400 text-sage-700',
  error: 'bg-danger-50 border-danger-400 text-danger-600',
  info: 'bg-info-50 border-info-500 text-info-500',
};

const icons: Record<ToastType, string> = {
  success: '\u2713',
  error: '!',
  info: 'i',
};

let toastId = 0;

export default function Toast() {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const addToast = useCallback((type: ToastType, message: string) => {
    const id = ++toastId;
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 3000);
  }, []);

  useEffect(() => {
    const handler = (e: Event) => {
      const { type, message } = (e as CustomEvent).detail;
      addToast(type, message);
    };
    window.addEventListener('toast', handler);
    return () => window.removeEventListener('toast', handler);
  }, [addToast]);

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`flex items-center gap-2 rounded-xl border-l-4 px-4 py-3 text-sm shadow-md animate-in slide-in-from-right ${typeStyles[toast.type]}`}
        >
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-current/10 text-xs font-bold">
            {icons[toast.type]}
          </span>
          <span>{toast.message}</span>
        </div>
      ))}
    </div>
  );
}

// Toast를 외부에서 호출할 수 있는 유틸
export function showToast(type: ToastType, message: string) {
  window.dispatchEvent(new CustomEvent('toast', { detail: { type, message } }));
}
