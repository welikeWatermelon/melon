import { useCallback, useEffect, useState } from 'react';
import { Avatar, Button, EmptyState, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchBlockedMembers, toggleBlock } from '@/api/blockApi';
import type { BlockedMember } from '@/types';

export default function BlockedMembersTab() {
  const [members, setMembers] = useState<BlockedMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [unblockingId, setUnblockingId] = useState<number | null>(null);

  const loadBlockedMembers = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchBlockedMembers();
      setMembers(data.blockedMembers);
    } catch {
      showToast('error', '차단 목록을 불러오지 못했어요');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBlockedMembers();
  }, [loadBlockedMembers]);

  const handleUnblock = async (memberId: number) => {
    setUnblockingId(memberId);
    try {
      await toggleBlock(memberId);
      setMembers((prev) => prev.filter((m) => m.id !== memberId));
      showToast('success', '차단을 해제했어요');
    } catch {
      showToast('error', '차단 해제에 실패했어요');
    } finally {
      setUnblockingId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="md" />
      </div>
    );
  }

  if (members.length === 0) {
    return <EmptyState title="차단한 회원이 없어요" />;
  }

  return (
    <div className="flex flex-col gap-2">
      {members.map((member) => (
        <div
          key={member.id}
          className="flex items-center justify-between rounded-xl border border-gray-100 bg-white px-4 py-3"
        >
          <div className="flex items-center gap-3">
            <Avatar nickname={member.nickname} size="md" />
            <span className="text-sm font-medium text-gray-700">{member.nickname}</span>
          </div>
          <Button
            variant="ghost"
            size="sm"
            loading={unblockingId === member.id}
            onClick={() => handleUnblock(member.id)}
            className="text-danger-500"
          >
            차단 해제
          </Button>
        </div>
      ))}
    </div>
  );
}
