import api from '@/api/instance';
import type {
  ApiResponse,
  PageResponse,
  AdminLicense,
  AdminReport,
  AdminMember,
  AdminStats,
  LicenseReviewRequest,
  ReportAction,
  MemberAction,
} from '@/types';

interface LicenseParams {
  status?: string;
  page?: number;
  size?: number;
}

interface ReportParams {
  status?: string;
  page?: number;
  size?: number;
}

interface MemberParams {
  role?: string;
  page?: number;
  size?: number;
}

export async function fetchLicenses(params: LicenseParams = {}) {
  const { data } = await api.get<ApiResponse<PageResponse<AdminLicense>>>(
    '/api/admin/licenses',
    { params },
  );
  return data.data;
}

export async function reviewLicense(id: number, body: LicenseReviewRequest) {
  const { data } = await api.patch<ApiResponse<void>>(
    `/api/admin/licenses/${id}`,
    body,
  );
  return data;
}

export async function fetchReports(params: ReportParams = {}) {
  const { data } = await api.get<ApiResponse<PageResponse<AdminReport>>>(
    '/api/admin/reports',
    { params },
  );
  return data.data;
}

export async function processReport(id: number, body: { action: ReportAction }) {
  const { data } = await api.patch<ApiResponse<void>>(
    `/api/admin/reports/${id}`,
    body,
  );
  return data;
}

export async function fetchMembers(params: MemberParams = {}) {
  const { data } = await api.get<ApiResponse<PageResponse<AdminMember>>>(
    '/api/admin/members',
    { params },
  );
  return data.data;
}

export async function actionMember(id: number, body: { action: MemberAction }) {
  const { data } = await api.patch<ApiResponse<void>>(
    `/api/admin/members/${id}`,
    body,
  );
  return data;
}

export async function fetchStats() {
  const { data } = await api.get<ApiResponse<AdminStats>>(
    '/api/admin/stats',
  );
  return data.data;
}
