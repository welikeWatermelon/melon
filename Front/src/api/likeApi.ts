import api from '@/api/instance';
import type { ApiResponse, LikeToggleRequest, LikeToggleResponse } from '@/types';

export async function toggleLike(body: LikeToggleRequest): Promise<LikeToggleResponse> {
  const { data } = await api.post<ApiResponse<LikeToggleResponse>>('/api/likes', body);
  return data.data;
}
