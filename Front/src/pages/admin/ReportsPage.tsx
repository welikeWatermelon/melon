import { useCallback, useEffect, useState } from 'react';
import { Button, Modal, Pagination, EmptyState, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchReports, processReport } from '@/api/adminApi';
import type { AdminReport, PageResponse, ReportAction } from '@/types';

type StatusFilter = 'ALL' | 'PENDING' | 'PROCESSED';

const STATUS_FILTERS: { label: string; value: StatusFilter }[] = [
  { label: '전체', value: 'ALL' },
  { label: '대기', value: 'PENDING' },
  { label: '처리됨', value: 'PROCESSED' },
];

const PAGE_SIZE = 10;

function statusBadgeClass(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'bg-secondary-100 text-secondary-600';
    case 'PROCESSED':
      return 'bg-sage-100 text-sage-600';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'PENDING':
      return '대기';
    case 'PROCESSED':
      return '처리됨';
    default:
      return status;
  }
}

function targetTypeLabel(type: string): string {
  return type === 'POST' ? '게시글' : '댓글';
}

export default function ReportsPage() {
  const [filter, setFilter] = useState<StatusFilter>('ALL');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AdminReport> | null>(null);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState<AdminReport | null>(null);
  const [confirmAction, setConfirmAction] = useState<ReportAction | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { page, size: PAGE_SIZE };
      if (filter !== 'ALL') params.status = filter;
      const result = await fetchReports(params as { status?: string; page?: number; size?: number });
      setData(result);
    } catch {
      showToast('error', '신고 목록을 불러오지 못했습니다.');
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

  const handleProcess = async () => {
    if (!selected || !confirmAction) return;
    setActionLoading(true);
    try {
      await processReport(selected.id, { action: confirmAction });
      showToast('success', '처리되었습니다.');
      setSelected(null);
      setConfirmAction(null);
      load();
    } catch {
      showToast('error', '처리에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const closeModal = () => {
    setSelected(null);
    setConfirmAction(null);
  };

  const closeConfirm = () => {
    setConfirmAction(null);
  };

  const actionLabel = (action: ReportAction): string => {
    switch (action) {
      case 'HIDE_POST':
        return '게시글 숨김';
      case 'HIDE_COMMENT':
        return '댓글 숨김';
      case 'DISMISS':
        return '기각';
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-gray-800">신고 관리</h1>

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
        <EmptyState title="신고 내역이 없습니다" description="접수된 신고가 없어요." />
      ) : (
        <>
          <div className="overflow-x-auto rounded-xl border border-gray-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 bg-warm-50">
                  <th className="px-4 py-3 text-left font-medium text-gray-500">신고자</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">대상</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">대상 ID</th>
                  <th className="hidden px-4 py-3 text-left font-medium text-gray-500 sm:table-cell">사유</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">상태</th>
                  <th className="hidden px-4 py-3 text-left font-medium text-gray-500 sm:table-cell">신고일</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((report) => (
                  <tr
                    key={report.id}
                    onClick={() => setSelected(report)}
                    className="cursor-pointer border-b border-gray-50 transition-colors hover:bg-primary-50/40"
                  >
                    <td className="px-4 py-3 text-gray-800">{report.reporterId}</td>
                    <td className="px-4 py-3 text-gray-600">{targetTypeLabel(report.targetType)}</td>
                    <td className="px-4 py-3 text-gray-600">{report.targetId}</td>
                    <td className="hidden max-w-[200px] truncate px-4 py-3 text-gray-500 sm:table-cell">
                      {report.reason}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${statusBadgeClass(report.status)}`}>
                        {statusLabel(report.status)}
                      </span>
                    </td>
                    <td className="hidden px-4 py-3 text-gray-500 sm:table-cell">
                      {new Date(report.createdAt).toLocaleDateString('ko-KR')}
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

      {/* Report detail modal */}
      <Modal isOpen={!!selected && !confirmAction} onClose={closeModal} title="신고 상세">
        {selected && (
          <div className="space-y-4">
            <div className="space-y-2 text-sm">
              <p>
                <span className="font-medium text-gray-500">신고자 ID: </span>
                <span className="text-gray-800">{selected.reporterId}</span>
              </p>
              <p>
                <span className="font-medium text-gray-500">대상: </span>
                <span className="text-gray-800">
                  {targetTypeLabel(selected.targetType)} #{selected.targetId}
                </span>
              </p>
              <p>
                <span className="font-medium text-gray-500">신고일: </span>
                <span className="text-gray-800">
                  {new Date(selected.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </p>
            </div>

            <div className="rounded-xl bg-warm-50 p-4">
              <p className="mb-1 text-xs font-medium text-gray-500">신고 사유</p>
              <p className="text-sm text-gray-800 whitespace-pre-wrap">{selected.reason}</p>
            </div>

            {selected.status === 'PENDING' ? (
              <div className="flex justify-end gap-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setConfirmAction('DISMISS')}
                >
                  기각
                </Button>
                {selected.targetType === 'POST' ? (
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setConfirmAction('HIDE_POST')}
                  >
                    게시글 숨김
                  </Button>
                ) : (
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setConfirmAction('HIDE_COMMENT')}
                  >
                    댓글 숨김
                  </Button>
                )}
              </div>
            ) : (
              <p className="text-center text-sm text-gray-400">이미 처리된 건입니다.</p>
            )}
          </div>
        )}
      </Modal>

      {/* Confirm action modal */}
      <Modal
        isOpen={!!confirmAction}
        onClose={closeConfirm}
        title="처리 확인"
        footer={
          <>
            <Button variant="ghost" size="sm" onClick={closeConfirm}>
              취소
            </Button>
            <Button
              variant="danger"
              size="sm"
              loading={actionLoading}
              onClick={handleProcess}
            >
              확인
            </Button>
          </>
        }
      >
        <p className="text-sm text-gray-700">
          정말 <span className="font-semibold">{confirmAction ? actionLabel(confirmAction) : ''}</span> 처리하시겠습니까?
        </p>
      </Modal>
    </div>
  );
}
