import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, EmptyState, Pagination, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import {
  fetchNotifications,
  markAllAsRead,
  markAsRead,
} from '@/api/notificationApi';
import { useNotificationStore } from '@/store/notificationStore';
import type { Notification } from '@/types';
import { timeAgo } from './utils/timeAgo';

const PAGE_SIZE = 20;

export default function NotificationsPage() {
  const navigate = useNavigate();
  const storeMarkAsRead = useNotificationStore((s) => s.markAsRead);
  const storeMarkAllAsRead = useNotificationStore((s) => s.markAllAsRead);
  const storeSetNotifications = useNotificationStore((s) => s.setNotifications);

  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [page, setPage] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadNotifications = useCallback(async (pageNum: number) => {
    setLoading(true);
    try {
      const data = await fetchNotifications({ page: pageNum, size: PAGE_SIZE });
      setNotifications(data.content);
      setTotalCount(
        data.content.length + (data.hasNext ? 1 : 0) + pageNum * PAGE_SIZE,
      );
      setHasNext(data.hasNext);
      // Sync first page to store
      if (pageNum === 0) {
        storeSetNotifications(data.content, data.unreadCount);
      }
    } catch {
      showToast('error', '알림을 불러오지 못했어요');
    } finally {
      setLoading(false);
    }
  }, [storeSetNotifications]);

  useEffect(() => {
    loadNotifications(page);
  }, [page, loadNotifications]);

  const getNotificationLink = (n: Notification): string | null => {
    if (n.postId) {
      return `/board/${n.postId}`;
    }
    if (n.targetType === 'POST') {
      return `/board/${n.targetId}`;
    }
    return null;
  };

  const handleClickNotification = async (notification: Notification) => {
    if (!notification.isRead) {
      try {
        await markAsRead(notification.id);
        storeMarkAsRead(notification.id);
        setNotifications((prev) =>
          prev.map((n) =>
            n.id === notification.id ? { ...n, isRead: true } : n,
          ),
        );
      } catch {
        // silent fail
      }
    }
    const link = getNotificationLink(notification);
    if (link) {
      navigate(link);
    }
    // COMMENT 타입이고 postId가 없는 경우 이동하지 않음 (이미 알림 페이지에 있음)
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      storeMarkAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      showToast('success', '모든 알림을 읽음 처리했어요');
    } catch {
      showToast('error', '알림 읽음 처리에 실패했어요');
    }
  };

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-800">알림</h1>
        <Button variant="ghost" size="sm" onClick={handleMarkAllAsRead}>
          전체 읽음
        </Button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <Spinner size="md" />
        </div>
      ) : notifications.length === 0 ? (
        <EmptyState title="알림이 없어요" description="새로운 알림이 오면 여기에 표시돼요" />
      ) : (
        <>
          <div className="flex flex-col gap-2">
            {notifications.map((notification) => (
              <button
                key={notification.id}
                onClick={() => handleClickNotification(notification)}
                className={`flex w-full items-start gap-3 rounded-xl border p-4 text-left transition-colors hover:border-primary-200 ${
                  notification.isRead
                    ? 'border-gray-100 bg-white'
                    : 'border-primary-100 bg-primary-50/50'
                }`}
              >
                {!notification.isRead && (
                  <span className="mt-1.5 h-2.5 w-2.5 shrink-0 rounded-full bg-primary-400" />
                )}
                <div className={`flex-1 ${notification.isRead ? 'pl-[22px]' : ''}`}>
                  <p className="mb-1 text-sm text-gray-700">{notification.message}</p>
                  <span className="text-xs text-gray-400">
                    {timeAgo(notification.createdAt)}
                  </span>
                </div>
              </button>
            ))}
          </div>

          <Pagination
            currentPage={page}
            hasNext={hasNext}
            totalCount={totalCount}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
