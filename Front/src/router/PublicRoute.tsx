import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

export default function PublicRoute() {
  const member = useAuthStore((s) => s.member);

  // 로그인 상태면 role에 따라 리다이렉트
  if (member) {
    switch (member.role) {
      case 'PENDING':
        return <Navigate to="/pending" replace />;
      case 'ADMIN':
        return <Navigate to="/admin" replace />;
      default:
        return <Navigate to="/board" replace />;
    }
  }

  return <Outlet />;
}
