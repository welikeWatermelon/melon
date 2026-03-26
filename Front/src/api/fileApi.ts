import api from '@/api/instance';
import type { ApiResponse, FileUploadResponse } from '@/types';

export async function uploadFile(file: File): Promise<FileUploadResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const { data } = await api.post<ApiResponse<FileUploadResponse>>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data;
}

export function getDownloadUrl(fileId: number): string {
  const baseURL = api.defaults.baseURL || '';
  return `${baseURL}/api/files/${fileId}/download`;
}
