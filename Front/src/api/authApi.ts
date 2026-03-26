import api from '@/api/instance';
import type { ApiResponse, LoginResponse, LicenseStatus } from '@/types';

export function loginWithKakao(code: string) {
  return api.get<ApiResponse<LoginResponse>>(`/api/auth/kakao/callback`, {
    params: { code },
  });
}

export function loginWithGoogle(code: string) {
  return api.get<ApiResponse<LoginResponse>>(`/api/auth/google/callback`, {
    params: { code },
  });
}

export function refreshToken(refreshTokenValue: string) {
  return api.post<ApiResponse<{ accessToken: string }>>(`/api/auth/refresh`, {
    refreshToken: refreshTokenValue,
  });
}

export function logout() {
  return api.patch<ApiResponse<null>>(`/api/auth/logout`);
}

export function submitLicense(file: File) {
  const formData = new FormData();
  formData.append('licenseImg', file);
  return api.post<ApiResponse<{ id: number; status: string; createdAt: string }>>(
    `/api/members/me/license`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
}

export function getLicenseStatus() {
  return api.get<ApiResponse<LicenseStatus>>(`/api/members/me/license`);
}
