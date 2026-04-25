import React, { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext(null);

let id = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'success') => {
    const tid = ++id;
    setToasts(prev => [...prev, { id: tid, message, type, removing: false }]);
    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === tid ? { ...t, removing: true } : t));
      setTimeout(() => setToasts(prev => prev.filter(t => t.id !== tid)), 320);
    }, 2800);
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, addToast }}>
      {children}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) return { toasts: [], addToast: () => {} };
  return ctx;
}
