import { useCallback, useEffect, useState } from 'react';
import { Button, Modal, Textarea, Badge, Pagination, EmptyState, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchLicenses, reviewLicense } from '@/api/adminApi';
import type { AdminLicense, PageResponse } from '@/types';

type StatusFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED';

const STATUS_FILTERS: { label: string; value: StatusFilter }[] = [
  { label: '전체', value: 'ALL' },
  { label: '대기', value: 'PENDING' },
  { label: '승인', value: 'APPROVED' },
  { label: '거절', value: 'REJECTED' },
];

const PAGE_SIZE = 10;

function statusBadgeClass(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'bg-secondary-100 text-secondary-600';
    case 'APPROVED':
      return 'bg-sage-100 text-sage-600';
    case 'REJECTED':
      return 'bg-danger-50 text-danger-600';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'PENDING':
      return '대기';
    case 'APPROVED':
      return '승인';
    case 'REJECTED':
      return '거절';
    default:
      return status;
  }
}

export default function LicensesPage() {
  const [filter, setFilter] = useState<StatusFilter>('ALL');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AdminLicense> | null>(null);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState<AdminLicense | null>(null);
  const [rejectMemo, setRejectMemo] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { page, size: PAGE_SIZE };
      if (filter !== 'ALL') params.status = filter;
      const result = await fetchLicenses(params as { status?: string; page?: number; size?: number });
      setData(result);
    } catch {
      showToast('error', '인증 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [filter]);

  const handleReview = async (status: 'APPROVED' | 'REJECTED') => {
    if (!selected) return;
    if (status === 'REJECTED' && !rejectMemo.trim()) {
      showToast('error', '거절 사유를 입력해주세요.');
      return;
    }
    setActionLoading(true);
    try {
      await reviewLicense(selected.id, {
        status,
        ...(status === 'REJECTED' ? { adminMemo: rejectMemo.trim() } : {}),
      });
      showToast('success', status === 'APPROVED' ? '승인되었습니다.' : '거절되었습니다.');
      setSelected(null);
      setRejectMemo('');
      load();
    } catch {
      showToast('error', '처리에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const closeModal = () => {
    setSelected(null);
    setRejectMemo('');
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-gray-800">면허 인증 관리</h1>

      {/* Filter tabs */}
      <div className="flex gap-2">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
              filter === f.value
                ? 'bg-primary-400 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {loading ? (
        <Spinner className="py-20" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="인증 신청이 없습니다" description="아직 면허 인증 신청이 없어요." />
      ) : (
        <>
          {/* Table */}
          <div className="overflow-x-auto rounded-xl border border-gray-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 bg-warm-50">
                  <th className="px-4 py-3 text-left font-medium text-gray-500">닉네임</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">신청일</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">상태</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((license) => (
                  <tr
                    key={license.id}
                    onClick={() => setSelected(license)}
                    className="cursor-pointer border-b border-gray-50 transition-colors hover:bg-primary-50/40"
                  >
                    <td className="px-4 py-3 font-medium text-gray-800">{license.nickname}</td>
                    <td className="px-4 py-3 text-gray-500">
                      {new Date(license.createdAt).toLocaleDateString('ko-KR')}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${statusBadgeClass(license.status)}`}>
                        {statusLabel(license.status)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Pagination
            currentPage={page}
            hasNext={data.hasNext}
            totalCount={data.totalCount}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </>
      )}

      {/* License detail modal */}
      <Modal isOpen={!!selected} onClose={closeModal} title="면허증 확인">
        {selected && (
          <div className="space-y-4">
            <div className="overflow-hidden rounded-xl border border-gray-100">
              <img
                src={selected.licenseImgUrl}
                alt="면허증 이미지"
                className="h-auto w-full object-contain"
              />
            </div>

            <div className="space-y-1 text-sm">
              <p>
                <span className="font-medium text-gray-500">닉네임: </span>
                <span className="text-gray-800">{selected.nickname}</span>
              </p>
              <p>
                <span className="font-medium text-gray-500">신청일: </span>
                <span className="text-gray-800">
                  {new Date(selected.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </p>
              <p>
                <span className="font-medium text-gray-500">상태: </span>
                <Badge
                  variant={
                    selected.status === 'APPROVED'
                      ? 'status'
                      : selected.status === 'PENDING'
                        ? 'therapy'
                        : 'count'
                  }
                >
                  {statusLabel(selected.status)}
                </Badge>
              </p>
            </div>

            {selected.status === 'PENDING' && (
              <>
                <Textarea
                  label="거절 사유 (거절 시 필수)"
                  placeholder="거절 사유를 입력해주세요..."
                  value={rejectMemo}
                  onChange={(e) => setRejectMemo(e.target.value)}
                />
                <div className="flex justify-end gap-2">
                  <Button
                    variant="danger"
                    size="sm"
                    loading={actionLoading}
                    onClick={() => handleReview('REJECTED')}
                  >
                    거절
                  </Button>
                  <Button
                    variant="primary"
                    size="sm"
                    loading={actionLoading}
                    onClick={() => handleReview('APPROVED')}
                  >
                    승인
                  </Button>
                </div>
              </>
            )}

            {selected.status !== 'PENDING' && (
              <p className="text-center text-sm text-gray-400">이미 처리된 건입니다.</p>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
