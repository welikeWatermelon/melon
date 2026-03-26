import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchPost, deletePost, toggleScrap } from '@/api/postApi';
import { toggleLike } from '@/api/likeApi';
import { createReport } from '@/api/reportApi';
import { getDownloadUrl } from '@/api/fileApi';
import { Avatar, Badge, Button, Modal, Spinner, Textarea, showToast } from '@/components/common';
import TipTapViewer from './components/TipTapViewer';
import CommentSection from './components/CommentSection';
import type { PostDetail } from '@/types';

export default function PostDetailPage() {
  const { postId } = useParams<{ postId: string }>();
  const navigate = useNavigate();
  const id = Number(postId);

  const [post, setPost] = useState<PostDetail | null>(null);
  const [loading, setLoading] = useState(true);

  // Like / Scrap state
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [isScrapped, setIsScrapped] = useState(false);

  // Modals
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportReason, setReportReason] = useState('');
  const [reportSubmitting, setReportSubmitting] = useState(false);

  const loadPost = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchPost(id);
      setPost(data);
      setIsLiked(data.isLiked);
      setLikeCount(data.likeCount);
      setIsScrapped(data.isScrapped);
    } catch {
      showToast('error', '게시글을 불러올 수 없습니다.');
      navigate('/board');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => {
    loadPost();
  }, [loadPost]);

  const handleLike = async () => {
    try {
      const res = await toggleLike({ targetType: 'POST', targetId: id });
      setIsLiked(res.isLiked);
      setLikeCount(res.likeCount);
    } catch {
      showToast('error', '좋아요 처리에 실패했습니다.');
    }
  };

  const handleScrap = async () => {
    try {
      const res = await toggleScrap(id);
      setIsScrapped(res.isScrapped);
      showToast('success', res.isScrapped ? '스크랩했습니다.' : '스크랩을 해제했습니다.');
    } catch {
      showToast('error', '스크랩 처리에 실패했습니다.');
    }
  };

  const handleDelete = async () => {
    try {
      await deletePost(id);
      showToast('success', '게시글이 삭제되었습니다.');
      navigate('/board');
    } catch {
      showToast('error', '게시글 삭제에 실패했습니다.');
    } finally {
      setDeleteModalOpen(false);
    }
  };

  const handleReport = async () => {
    if (!reportReason.trim()) {
      showToast('error', '신고 사유를 입력해주세요.');
      return;
    }
    setReportSubmitting(true);
    try {
      await createReport({ targetType: 'POST', targetId: id, reason: reportReason.trim() });
      showToast('success', '신고가 접수되었습니다.');
      setReportModalOpen(false);
      setReportReason('');
    } catch {
      showToast('error', '신고 접수에 실패했습니다.');
    } finally {
      setReportSubmitting(false);
    }
  };

  if (loading || !post) {
    return <Spinner className="py-20" />;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      {/* Back button */}
      <button
        onClick={() => navigate('/board')}
        className="mb-4 flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        목록으로
      </button>

      {/* Post header */}
      <div className="mb-6">
        {post.therapyArea && (
          <Badge variant="therapy" className="mb-2">
            {post.therapyArea}
          </Badge>
        )}
        <h1 className="text-lg font-bold text-gray-800">{post.title}</h1>
        <div className="mt-3 flex items-center gap-3">
          <Avatar nickname={post.isAnonymous ? '익명' : post.author} size="sm" />
          <div>
            <span className="text-sm font-medium text-gray-700">
              {post.isAnonymous ? '익명' : post.author}
            </span>
            <div className="flex items-center gap-2 text-xs text-gray-400">
              <span>{formatDate(post.createdAt)}</span>
              <span>조회 {post.viewCount}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Post content */}
      <div className="mb-6">
        <TipTapViewer content={post.content} />
      </div>

      {/* Attachments */}
      {post.files.length > 0 && (
        <div className="mb-6">
          <h4 className="mb-2 text-sm font-medium text-gray-600">첨부 파일</h4>
          <div className="space-y-1">
            {post.files.map((file) => (
              <a
                key={file.id}
                href={getDownloadUrl(file.id)}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2 text-sm text-primary-500 hover:bg-gray-100"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                  />
                </svg>
                {file.originalName}
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Action bar */}
      <div className="mb-8 flex items-center justify-between border-y border-gray-100 py-3">
        <div className="flex items-center gap-4">
          {/* Like */}
          <button
            onClick={handleLike}
            className={`flex items-center gap-1.5 text-sm transition-colors ${
              isLiked ? 'text-primary-500' : 'text-gray-400 hover:text-primary-500'
            }`}
          >
            <svg
              className="h-5 w-5"
              fill={isLiked ? 'currentColor' : 'none'}
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
              />
            </svg>
            {likeCount}
          </button>

          {/* Scrap */}
          <button
            onClick={handleScrap}
            className={`flex items-center gap-1.5 text-sm transition-colors ${
              isScrapped ? 'text-secondary-500' : 'text-gray-400 hover:text-secondary-500'
            }`}
          >
            <svg
              className="h-5 w-5"
              fill={isScrapped ? 'currentColor' : 'none'}
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"
              />
            </svg>
            스크랩
          </button>

          {/* Report */}
          {!post.isMyPost && (
            <button
              onClick={() => setReportModalOpen(true)}
              className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-danger-500"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"
                />
              </svg>
              신고
            </button>
          )}
        </div>

        {/* Edit / Delete */}
        {post.isMyPost && (
          <div className="flex gap-2">
            <Button size="sm" variant="ghost" onClick={() => navigate(`/board/${id}/edit`)}>
              수정
            </Button>
            <Button size="sm" variant="danger" onClick={() => setDeleteModalOpen(true)}>
              삭제
            </Button>
          </div>
        )}
      </div>

      {/* Comments */}
      <CommentSection postId={id} />

      {/* Delete modal */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="게시글 삭제"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleteModalOpen(false)}>
              취소
            </Button>
            <Button variant="danger" onClick={handleDelete}>
              삭제
            </Button>
          </>
        }
      >
        <p>정말 이 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.</p>
      </Modal>

      {/* Report modal */}
      <Modal
        isOpen={reportModalOpen}
        onClose={() => {
          setReportModalOpen(false);
          setReportReason('');
        }}
        title="게시글 신고"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => {
                setReportModalOpen(false);
                setReportReason('');
              }}
            >
              취소
            </Button>
            <Button variant="danger" onClick={handleReport} loading={reportSubmitting}>
              신고하기
            </Button>
          </>
        }
      >
        <Textarea
          label="신고 사유"
          placeholder="신고 사유를 입력해주세요..."
          value={reportReason}
          onChange={(e) => setReportReason(e.target.value)}
          rows={3}
        />
      </Modal>
    </div>
  );
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}
