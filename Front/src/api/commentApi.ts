import api from '@/api/instance';
import type { ApiResponse, Comment, CommentCreateRequest } from '@/types';

export async function fetchComments(postId: number): Promise<{ comments: Comment[] }> {
  const { data } = await api.get<ApiResponse<{ comments: Comment[] }>>(`/api/posts/${postId}/comments`);
  return data.data;
}

export async function createComment(postId: number, body: CommentCreateRequest): Promise<{ id: number }> {
  const { data } = await api.post<ApiResponse<{ id: number }>>(`/api/posts/${postId}/comments`, body);
  return data.data;
}

export async function createReply(
  postId: number,
  commentId: number,
  body: CommentCreateRequest,
): Promise<{ id: number }> {
  const { data } = await api.post<ApiResponse<{ id: number }>>(
    `/api/posts/${postId}/comments/${commentId}/replies`,
    body,
  );
  return data.data;
}

export async function updateComment(
  postId: number,
  commentId: number,
  body: { content: string },
): Promise<{ id: number }> {
  const { data } = await api.patch<ApiResponse<{ id: number }>>(
    `/api/posts/${postId}/comments/${commentId}`,
    body,
  );
  return data.data;
}

export async function deleteComment(postId: number, commentId: number): Promise<void> {
  await api.patch(`/api/posts/${postId}/comments/${commentId}/delete`);
}
