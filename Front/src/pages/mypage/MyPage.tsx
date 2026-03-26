import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Avatar, Badge, Button, Input, Modal } from '@/components/common';
import { showToast } from '@/components/common';
import { useAuthStore } from '@/store/authStore';
import { updateMyInfo, deleteMyAccount } from '@/api/memberApi';
import type { TherapyArea } from '@/types';
import MyPostsTab from './components/MyPostsTab';
import MyCommentsTab from './components/MyCommentsTab';
import ScrappedPostsTab from './components/ScrappedPostsTab';
import BlockedMembersTab from './components/BlockedMembersTab';

type TabType = 'posts' | 'comments' | 'scrapped' | 'blocked';

const TABS: { key: TabType; label: string }[] = [
  { key: 'posts', label: '내가 쓴 글' },
  { key: 'comments', label: '내가 쓴 댓글' },
  { key: 'scrapped', label: '스크랩한 글' },
  { key: 'blocked', label: '차단 목록' },
];

const THERAPY_AREAS: TherapyArea[] = ['작업', '언어', '인지', '놀이', '기타'];

export default function MyPage() {
  const navigate = useNavigate();
  const member = useAuthStore((s) => s.member);
  const updateMember = useAuthStore((s) => s.updateMember);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const [activeTab, setActiveTab] = useState<TabType>('posts');
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  // Edit form state
  const [editNickname, setEditNickname] = useState(member?.nickname ?? '');
  const [editTherapyArea, setEditTherapyArea] = useState(member?.therapyArea ?? '기타');
  const [editLoading, setEditLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleOpenEditModal = () => {
    setEditNickname(member?.nickname ?? '');
    setEditTherapyArea(member?.therapyArea ?? '기타');
    setEditModalOpen(true);
  };

  const handleUpdateProfile = async () => {
    if (!editNickname.trim()) {
      showToast('error', '닉네임을 입력해주세요');
      return;
    }
    setEditLoading(true);
    try {
      const updated = await updateMyInfo({
        nickname: editNickname.trim(),
        therapyArea: editTherapyArea,
      });
      updateMember(updated);
      setEditModalOpen(false);
      showToast('success', '프로필이 수정되었어요');
    } catch {
      showToast('error', '프로필 수정에 실패했어요');
    } finally {
      setEditLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    setDeleteLoading(true);
    try {
      await deleteMyAccount();
      clearAuth();
      navigate('/login');
      showToast('info', '회원 탈퇴가 완료되었어요');
    } catch {
      showToast('error', '회원 탈퇴에 실패했어요');
    } finally {
      setDeleteLoading(false);
    }
  };

  if (!member) return null;

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      {/* Profile Card */}
      <div className="mb-6 rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
        <div className="flex items-center gap-4">
          <Avatar nickname={member.nickname} size="lg" />
          <div className="flex-1">
            <h2 className="text-lg font-bold text-gray-800">{member.nickname}</h2>
            <Badge variant="therapy">{member.therapyArea}</Badge>
          </div>
        </div>
        <div className="mt-4 flex gap-2">
          <Button variant="secondary" size="sm" onClick={handleOpenEditModal}>
            정보 수정
          </Button>
          <Button variant="ghost" size="sm" onClick={() => setDeleteModalOpen(true)} className="text-danger-500">
            회원 탈퇴
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-4 flex gap-1 rounded-xl bg-gray-50 p-1">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
              activeTab === tab.key
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div>
        {activeTab === 'posts' && <MyPostsTab />}
        {activeTab === 'comments' && <MyCommentsTab />}
        {activeTab === 'scrapped' && <ScrappedPostsTab />}
        {activeTab === 'blocked' && <BlockedMembersTab />}
      </div>

      {/* Edit Modal */}
      <Modal
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        title="정보 수정"
        footer={
          <>
            <Button variant="ghost" size="sm" onClick={() => setEditModalOpen(false)}>
              취소
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={editLoading}
              onClick={handleUpdateProfile}
            >
              저장
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          <Input
            label="닉네임"
            value={editNickname}
            onChange={(e) => setEditNickname(e.target.value)}
            placeholder="닉네임을 입력해주세요"
          />
          <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">치료영역</label>
            <select
              value={editTherapyArea}
              onChange={(e) => setEditTherapyArea(e.target.value)}
              className="w-full rounded-xl border border-gray-200 px-4 py-2.5 text-sm outline-none transition-colors focus:border-primary-400 focus:ring-2 focus:ring-primary-50"
            >
              {THERAPY_AREAS.map((area) => (
                <option key={area} value={area}>
                  {area}치료
                </option>
              ))}
            </select>
          </div>
        </div>
      </Modal>

      {/* Delete Account Modal */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="회원 탈퇴"
        footer={
          <>
            <Button variant="ghost" size="sm" onClick={() => setDeleteModalOpen(false)}>
              취소
            </Button>
            <Button
              variant="danger"
              size="sm"
              loading={deleteLoading}
              onClick={handleDeleteAccount}
            >
              탈퇴하기
            </Button>
          </>
        }
      >
        <div className="text-center">
          <p className="mb-2 text-base font-semibold text-gray-800">
            정말 탈퇴하시겠습니까?
          </p>
          <p className="text-sm text-danger-500">
            되돌릴 수 없습니다.
          </p>
        </div>
      </Modal>
    </div>
  );
}
