import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export default function AdminRoute() {
  const member = useAuthStore((s) => s.member);

  if (!member) return <Navigate to="/login" replace />;
  if (member.role !== 'ADMIN') return <Navigate to="/board" replace />;

  return <Outlet />;
}
