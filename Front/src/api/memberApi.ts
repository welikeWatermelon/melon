import api from '@/api/instance';
import type { ApiResponse, Member, MemberUpdateRequest, MyComment, PageResponse } from '@/types';

export async function fetchMyInfo(): Promise<Member> {
  const { data } = await api.get<ApiResponse<Member>>('/api/members/me');
  return data.data;
}

export async function updateMyInfo(request: MemberUpdateRequest): Promise<Member> {
  const { data } = await api.patch<ApiResponse<Member>>('/api/members/me', request);
  return data.data;
}

export async function deleteMyAccount(): Promise<void> {
  await api.patch('/api/members/me/delete');
}

export async function fetchMyComments(params?: {
  page?: number;
  size?: number;
}): Promise<PageResponse<MyComment>> {
  const { data } = await api.get<ApiResponse<PageResponse<MyComment>>>(
    '/api/members/me/comments',
    { params },
  );
  return data.data;
}
