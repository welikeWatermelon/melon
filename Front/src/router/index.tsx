import { createBrowserRouter } from 'react-router-dom';
import { MainLayout, AdminLayout } from '@/layouts';
import PublicRoute from './PublicRoute';
import PendingRoute from './PendingRoute';
import PrivateRoute from './PrivateRoute';
import AdminRoute from './AdminRoute';

import { LandingPage } from '@/pages/landing';
import { LoginPage, OAuthCallbackPage, PendingPage } from '@/pages/auth';
import { BoardPage, PostDetailPage, PostWritePage, PostEditPage } from '@/pages/board';
import { MyPage, NotificationsPage } from '@/pages/mypage';
import { LicensesPage, ReportsPage, MembersPage, StatsPage } from '@/pages/admin';

export const router = createBrowserRouter([
  // Public: 비로그인 접근 가능
  {
    element: <PublicRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { path: '/', element: <LandingPage /> },
          { path: '/login', element: <LoginPage /> },
        ],
      },
      { path: '/auth/kakao/callback', element: <OAuthCallbackPage /> },
      { path: '/auth/google/callback', element: <OAuthCallbackPage /> },
    ],
  },

  // Pending: PENDING 상태만
  {
    element: <PendingRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [{ path: '/pending', element: <PendingPage /> }],
      },
    ],
  },

  // Private: MEMBER 이상
  {
    element: <PrivateRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { path: '/board', element: <BoardPage /> },
          { path: '/board/write', element: <PostWritePage /> },
          { path: '/board/:postId', element: <PostDetailPage /> },
          { path: '/board/:postId/edit', element: <PostEditPage /> },
          { path: '/mypage', element: <MyPage /> },
          { path: '/notifications', element: <NotificationsPage /> },
        ],
      },
    ],
  },

  // Admin: ADMIN만
  {
    element: <AdminRoute />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { path: '/admin', element: <LicensesPage /> },
          { path: '/admin/licenses', element: <LicensesPage /> },
          { path: '/admin/reports', element: <ReportsPage /> },
          { path: '/admin/members', element: <MembersPage /> },
          { path: '/admin/stats', element: <StatsPage /> },
        ],
      },
    ],
  },
]);
