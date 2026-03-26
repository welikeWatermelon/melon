import { useEffect } from 'react';
import { useAuthStore } from '@/store/authStore';
import { useNotificationStore } from '@/store/notificationStore';

export function useSSE() {
  const member = useAuthStore((s) => s.member);
  const accessToken = useAuthStore((s) => s.accessToken);
  const connectSSE = useNotificationStore((s) => s.connectSSE);
  const disconnectSSE = useNotificationStore((s) => s.disconnectSSE);

  useEffect(() => {
    if (member && accessToken && member.role !== 'PENDING') {
      connectSSE();
    } else {
      disconnectSSE();
    }

    return () => {
      disconnectSSE();
    };
  }, [member, accessToken, connectSSE, disconnectSSE]);
}
