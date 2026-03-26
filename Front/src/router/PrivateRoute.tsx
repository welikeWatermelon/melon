import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export default function PrivateRoute() {
  const member = useAuthStore((s) => s.member);

  if (!member) return <Navigate to="/login" replace />;
  if (member.role === 'PENDING') return <Navigate to="/pending" replace />;

  return <Outlet />;
}
