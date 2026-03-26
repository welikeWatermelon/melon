import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotificationStore } from '@/store/notificationStore';
import { fetchNotifications, markAllAsRead, markAsRead } from '@/api/notificationApi';
import { showToast } from '@/components/common';
import { timeAgo } from '../utils/timeAgo';
import type { Notification } from '@/types';

export default function NotificationDropdown() {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  const notifications = useNotificationStore((s) => s.notifications);
  const unreadCount = useNotificationStore((s) => s.unreadCount);
  const setNotifications = useNotificationStore((s) => s.setNotifications);
  const storeMarkAsRead = useNotificationStore((s) => s.markAsRead);
  const storeMarkAllAsRead = useNotificationStore((s) => s.markAllAsRead);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const loadNotifications = async () => {
    try {
      const data = await fetchNotifications({ page: 0, size: 5 });
      setNotifications(data.content, data.unreadCount);
    } catch {
      // silent fail
    }
  };

  const handleToggle = () => {
    if (!isOpen) {
      loadNotifications();
    }
    setIsOpen(!isOpen);
  };

  const getNotificationLink = (notification: Notification): string | null => {
    // postId가 있으면 우선 사용 (백엔드에서 제공 시)
    if (notification.postId) {
      return `/board/${notification.postId}`;
    }
    // targetType이 POST이면 targetId가 게시글 ID
    if (notification.targetType === 'POST') {
      return `/board/${notification.targetId}`;
    }
    // targetType이 COMMENT이면 targetId가 댓글 ID이므로 게시글로 이동 불가
    return null;
  };

  const handleClickNotification = async (notification: Notification) => {
    try {
      await markAsRead(notification.id);
      storeMarkAsRead(notification.id);
    } catch {
      // silent fail
    }
    setIsOpen(false);
    const link = getNotificationLink(notification);
    if (link) {
      navigate(link);
    } else {
      // COMMENT 타입이고 postId가 없는 경우 알림 전체 페이지로 이동
      navigate('/notifications');
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      storeMarkAllAsRead();
      showToast('success', '모든 알림을 읽음 처리했어요');
    } catch {
      showToast('error', '알림 읽음 처리에 실패했어요');
    }
  };

  const recentNotifications = notifications.slice(0, 5);

  return (
    <div ref={ref} className="relative">
      <button
        onClick={handleToggle}
        className="relative rounded-lg p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
      >
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-primary-400 text-[10px] font-bold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 z-50 mt-2 w-80 rounded-xl border border-gray-100 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h4 className="text-sm font-semibold text-gray-800">알림</h4>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllAsRead}
                className="text-xs text-primary-500 hover:text-primary-600"
              >
                모두 읽음
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto">
            {recentNotifications.length === 0 ? (
              <div className="py-8 text-center text-sm text-gray-400">
                알림이 없어요
              </div>
            ) : (
              recentNotifications.map((notification) => (
                <button
                  key={notification.id}
                  onClick={() =>
                    handleClickNotification(notification)
                  }
                  className={`flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-gray-50 ${
                    !notification.isRead ? 'bg-primary-50/50' : ''
                  }`}
                >
                  {!notification.isRead && (
                    <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary-400" />
                  )}
                  <div className={`flex-1 ${notification.isRead ? 'pl-5' : ''}`}>
                    <p className="text-sm text-gray-700 line-clamp-2">
                      {notification.message}
                    </p>
                    <span className="mt-1 text-xs text-gray-400">
                      {timeAgo(notification.createdAt)}
                    </span>
                  </div>
                </button>
              ))
            )}
          </div>

          <div className="border-t border-gray-100">
            <button
              onClick={() => {
                setIsOpen(false);
                navigate('/notifications');
              }}
              className="w-full py-3 text-center text-sm font-medium text-primary-500 hover:bg-primary-50/50"
            >
              전체 보기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
