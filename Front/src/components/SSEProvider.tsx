import type { ReactNode } from 'react';
import { useSSE } from '@/hooks/useSSE';

interface SSEProviderProps {
  children: ReactNode;
}

export default function SSEProvider({ children }: SSEProviderProps) {
  useSSE();
  return <>{children}</>;
}
