import api from '@/api/instance';
import type { ApiResponse, BlockedMember, BlockToggleResponse } from '@/types';

export async function fetchBlockedMembers(): Promise<{ blockedMembers: BlockedMember[] }> {
  const { data } = await api.get<ApiResponse<{ blockedMembers: BlockedMember[] }>>(
    '/api/blocks',
  );
  return data.data;
}

export async function toggleBlock(memberId: number): Promise<BlockToggleResponse> {
  const { data } = await api.post<ApiResponse<BlockToggleResponse>>(
    `/api/blocks/${memberId}`,
  );
  return data.data;
}
