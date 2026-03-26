import { NavLink, Outlet } from 'react-router-dom';
import GNB from './GNB';
import { ErrorBoundary, Toast } from '@/components/common';

const adminNavItems = [
  { to: '/admin/licenses', label: '면허 관리' },
  { to: '/admin/reports', label: '신고 관리' },
  { to: '/admin/members', label: '회원 관리' },
  { to: '/admin/stats', label: '통계' },
];

export default function AdminLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <GNB />
      <div className="mx-auto flex w-full max-w-5xl flex-1 gap-6 px-4 py-6">
        {/* 사이드 네비게이션 */}
        <aside className="hidden w-48 flex-shrink-0 md:block">
          <nav className="sticky top-20 flex flex-col gap-1">
            <h2 className="mb-2 px-3 text-xs font-semibold uppercase tracking-wider text-gray-400">
              관리자
            </h2>
            {adminNavItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `rounded-xl px-3 py-2 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary-50 text-primary-600'
                      : 'text-gray-500 hover:bg-gray-100 hover:text-gray-700'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>

        {/* 모바일 탭 네비게이션 */}
        <nav className="flex gap-1 overflow-x-auto border-b border-gray-100 pb-3 md:hidden">
          {adminNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-medium ${
                  isActive
                    ? 'bg-primary-400 text-white'
                    : 'bg-gray-100 text-gray-500'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* 메인 콘텐츠 */}
        <main className="min-w-0 flex-1">
          <ErrorBoundary>
            <Outlet />
          </ErrorBoundary>
        </main>
      </div>
      <Toast />
    </div>
  );
}
