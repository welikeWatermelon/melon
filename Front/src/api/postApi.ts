import api from '@/api/instance';
import type {
  ApiResponse,
  PageResponse,
  PostListItem,
  PostDetail,
  PostCreateRequest,
  PostUpdateRequest,
  ScrapResponse,
} from '@/types';

interface FetchPostsParams {
  page?: number;
  size?: number;
  therapyArea?: string;
  keyword?: string;
}

interface FetchPageParams {
  page?: number;
  size?: number;
}

export async function fetchPosts(params: FetchPostsParams = {}): Promise<PageResponse<PostListItem>> {
  const { data } = await api.get<ApiResponse<PageResponse<PostListItem>>>('/api/posts', { params });
  return data.data;
}

export async function fetchPost(postId: number): Promise<PostDetail> {
  const { data } = await api.get<ApiResponse<PostDetail>>(`/api/posts/${postId}`);
  return data.data;
}

export async function createPost(body: PostCreateRequest): Promise<{ id: number }> {
  const { data } = await api.post<ApiResponse<{ id: number }>>('/api/posts', body);
  return data.data;
}

export async function updatePost(postId: number, body: PostUpdateRequest): Promise<{ id: number }> {
  const { data } = await api.patch<ApiResponse<{ id: number }>>(`/api/posts/${postId}`, body);
  return data.data;
}

export async function deletePost(postId: number): Promise<void> {
  await api.patch(`/api/posts/${postId}/delete`);
}

export async function fetchMyPosts(params: FetchPageParams = {}): Promise<PageResponse<PostListItem>> {
  const { data } = await api.get<ApiResponse<PageResponse<PostListItem>>>('/api/posts/my', { params });
  return data.data;
}

export async function fetchScrappedPosts(params: FetchPageParams = {}): Promise<PageResponse<PostListItem>> {
  const { data } = await api.get<ApiResponse<PageResponse<PostListItem>>>('/api/posts/scrapped', { params });
  return data.data;
}

export async function toggleScrap(postId: number): Promise<ScrapResponse> {
  const { data } = await api.post<ApiResponse<ScrapResponse>>(`/api/posts/${postId}/scraps`);
  return data.data;
}
