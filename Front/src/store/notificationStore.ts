import { create } from 'zustand';
import type { Notification } from '@/types';
import { useAuthStore } from '@/store/authStore';
import { fetchNotifications } from '@/api/notificationApi';

interface NotificationStore {
  notifications: Notification[];
  unreadCount: number;
  eventSource: EventSource | null;
  reconnectTimer: ReturnType<typeof setTimeout> | null;
  setNotifications: (list: Notification[], unread: number) => void;
  addNotification: (notification: Notification) => void;
  markAsRead: (id: number) => void;
  markAllAsRead: () => void;
  connectSSE: () => void;
  disconnectSSE: () => void;
}

export const useNotificationStore = create<NotificationStore>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  eventSource: null,
  reconnectTimer: null,

  setNotifications: (list, unread) =>
    set({ notifications: list, unreadCount: unread }),

  addNotification: (notification) =>
    set((state) => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    })),

  markAsRead: (id) =>
    set((state) => {
      const target = state.notifications.find((n) => n.id === id);
      if (!target || target.isRead) return state;
      return {
        notifications: state.notifications.map((n) =>
          n.id === id ? { ...n, isRead: true } : n,
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      };
    }),

  markAllAsRead: () =>
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, isRead: true })),
      unreadCount: 0,
    })),

  connectSSE: () => {
    const { eventSource, reconnectTimer } = get();
    if (eventSource) return;
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      set({ reconnectTimer: null });
    }

    const token = useAuthStore.getState().accessToken;
    if (!token) return;

    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const es = new EventSource(
      `${baseURL}/api/notifications/subscribe?token=${encodeURIComponent(token)}`,
    );

    es.onopen = () => {
      // SSE 연결(재연결) 성공 시 누락 알림 동기화
      fetchNotifications({ page: 0, size: 20 })
        .then((data) => {
          get().setNotifications(data.content, data.unreadCount);
        })
        .catch(() => {
          // silent fail
        });
    };

    es.addEventListener('notification', (event) => {
      try {
        const data = JSON.parse(event.data) as Notification;
        get().addNotification(data);
      } catch {
        // ignore non-JSON messages (e.g. "connect" dummy event)
      }
    });

    es.onerror = () => {
      es.close();
      set({ eventSource: null });
      const timer = setTimeout(() => {
        set({ reconnectTimer: null });
        get().connectSSE();
      }, 3000);
      set({ reconnectTimer: timer });
    };

    set({ eventSource: es });
  },

  disconnectSSE: () => {
    const { eventSource, reconnectTimer } = get();
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      set({ reconnectTimer: null });
    }
    if (eventSource) {
      eventSource.close();
      set({ eventSource: null });
    }
  },
}));
