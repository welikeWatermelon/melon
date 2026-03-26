import api from '@/api/instance';
import type { ReportCreateRequest } from '@/types';

export async function createReport(body: ReportCreateRequest): Promise<void> {
  await api.post('/api/reports', body);
}
