import { useCallback, useEffect, useState } from 'react';
import { Button, Modal, Pagination, EmptyState, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchMembers, actionMember } from '@/api/adminApi';
import type { AdminMember, PageResponse } from '@/types';

type RoleFilter = 'ALL' | 'PENDING' | 'MEMBER' | 'ADMIN';

const ROLE_FILTERS: { label: string; value: RoleFilter }[] = [
  { label: '전체', value: 'ALL' },
  { label: '대기', value: 'PENDING' },
  { label: '회원', value: 'MEMBER' },
  { label: '관리자', value: 'ADMIN' },
];

const PAGE_SIZE = 10;

function roleBadgeClass(role: string): string {
  switch (role) {
    case 'PENDING':
      return 'bg-secondary-100 text-secondary-600';
    case 'MEMBER':
      return 'bg-sage-100 text-sage-600';
    case 'ADMIN':
      return 'bg-primary-100 text-primary-600';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}

function roleLabel(role: string): string {
  switch (role) {
    case 'PENDING':
      return '대기';
    case 'MEMBER':
      return '회원';
    case 'ADMIN':
      return '관리자';
    default:
      return role;
  }
}

export default function MembersPage() {
  const [filter, setFilter] = useState<RoleFilter>('ALL');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<AdminMember> | null>(null);
  const [loading, setLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<AdminMember | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { page, size: PAGE_SIZE };
      if (filter !== 'ALL') params.role = filter;
      const result = await fetchMembers(params as { role?: string; page?: number; size?: number });
      setData(result);
    } catch {
      showToast('error', '회원 목록을 불러오지 못했습니다.');
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

  const handleForceDelete = async () => {
    if (!deleteTarget) return;
    setActionLoading(true);
    try {
      await actionMember(deleteTarget.id, { action: 'FORCE_DELETE' });
      showToast('success', '강제 탈퇴 처리되었습니다.');
      setDeleteTarget(null);
      load();
    } catch {
      showToast('error', '처리에 실패했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-gray-800">회원 관리</h1>

      {/* Filter tabs */}
      <div className="flex gap-2">
        {ROLE_FILTERS.map((f) => (
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
        <EmptyState title="회원이 없습니다" description="조건에 맞는 회원이 없어요." />
      ) : (
        <>
          <div className="overflow-x-auto rounded-xl border border-gray-100">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 bg-warm-50">
                  <th className="px-4 py-3 text-left font-medium text-gray-500">닉네임</th>
                  <th className="hidden px-4 py-3 text-left font-medium text-gray-500 sm:table-cell">치료영역</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-500">역할</th>
                  <th className="hidden px-4 py-3 text-left font-medium text-gray-500 sm:table-cell">가입일</th>
                  <th className="px-4 py-3 text-right font-medium text-gray-500">관리</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((member) => (
                  <tr key={member.id} className="border-b border-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-800">{member.nickname}</td>
                    <td className="hidden px-4 py-3 text-gray-600 sm:table-cell">
                      {member.therapyArea || '-'}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${roleBadgeClass(member.role)}`}>
                        {roleLabel(member.role)}
                      </span>
                    </td>
                    <td className="hidden px-4 py-3 text-gray-500 sm:table-cell">
                      {new Date(member.createdAt).toLocaleDateString('ko-KR')}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          setDeleteTarget(member);
                        }}
                      >
                        강제 탈퇴
                      </Button>
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

      {/* Confirm delete modal */}
      <Modal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="강제 탈퇴 확인"
        footer={
          <>
            <Button variant="ghost" size="sm" onClick={() => setDeleteTarget(null)}>
              취소
            </Button>
            <Button
              variant="danger"
              size="sm"
              loading={actionLoading}
              onClick={handleForceDelete}
            >
              강제 탈퇴
            </Button>
          </>
        }
      >
        <p className="text-sm text-gray-700">
          정말 <span className="font-semibold">{deleteTarget?.nickname}</span>님을 강제 탈퇴시키겠습니까?
        </p>
      </Modal>
    </div>
  );
}
