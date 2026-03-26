import api from '@/api/instance';
import type { ApiResponse, NotificationListResponse } from '@/types';

export async function fetchNotifications(params?: {
  page?: number;
  size?: number;
}): Promise<NotificationListResponse> {
  const { data } = await api.get<ApiResponse<NotificationListResponse>>(
    '/api/notifications',
    { params },
  );
  return data.data;
}

export async function markAsRead(id: number): Promise<void> {
  await api.patch(`/api/notifications/${id}/read`);
}

export async function markAllAsRead(): Promise<void> {
  await api.patch('/api/notifications/read-all');
}
