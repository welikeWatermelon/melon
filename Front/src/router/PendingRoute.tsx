import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export default function PendingRoute() {
  const member = useAuthStore((s) => s.member);

  if (!member) return <Navigate to="/login" replace />;
  if (member.role !== 'PENDING') return <Navigate to="/board" replace />;

  return <Outlet />;
}
