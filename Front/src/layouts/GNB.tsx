import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Avatar, Button, Dropdown } from '@/components/common';
import NotificationDropdown from '@/pages/mypage/components/NotificationDropdown';

export default function GNB() {
  const { member, clearAuth } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 border-b border-gray-100 bg-white/80 backdrop-blur-md">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        {/* 로고 */}
        <Link to="/" className="flex items-center gap-2 text-lg font-bold text-primary-500">
          <span className="text-xl">🍈</span>
          <span>멜론미</span>
        </Link>

        {/* 우측 메뉴 */}
        <nav className="flex items-center gap-3">
          {!member && (
            <Button size="sm" onClick={() => navigate('/login')}>
              로그인
            </Button>
          )}

          {member?.role === 'PENDING' && (
            <span className="text-xs text-secondary-500 font-medium">
              면허 인증 대기중
            </span>
          )}

          {member && member.role !== 'PENDING' && (
            <>
              {/* 알림 드롭다운 */}
              <NotificationDropdown />

              {/* 관리자 메뉴 */}
              {member.role === 'ADMIN' && (
                <Link
                  to="/admin"
                  className="rounded-lg px-3 py-1.5 text-xs font-medium text-secondary-600 hover:bg-secondary-50"
                >
                  관리자
                </Link>
              )}

              {/* 프로필 드롭다운 */}
              <Dropdown
                align="right"
                trigger={<Avatar nickname={member.nickname} size="sm" />}
                items={[
                  { label: '마이페이지', value: 'mypage' },
                  { label: '로그아웃', value: 'logout' },
                ]}
                onSelect={(value) => {
                  if (value === 'mypage') navigate('/mypage');
                  if (value === 'logout') handleLogout();
                }}
              />
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
