import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { EmptyState, Pagination, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchMyComments } from '@/api/memberApi';
import type { MyComment } from '@/types';
import { timeAgo } from '../utils/timeAgo';

const PAGE_SIZE = 10;

export default function MyCommentsTab() {
  const navigate = useNavigate();
  const [comments, setComments] = useState<MyComment[]>([]);
  const [page, setPage] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadComments = useCallback(async (pageNum: number) => {
    setLoading(true);
    try {
      const data = await fetchMyComments({ page: pageNum, size: PAGE_SIZE });
      setComments(data.content);
      setTotalCount(data.totalCount);
      setHasNext(data.hasNext);
    } catch {
      showToast('error', '내가 쓴 댓글을 불러오지 못했어요');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadComments(page);
  }, [page, loadComments]);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="md" />
      </div>
    );
  }

  if (comments.length === 0) {
    return <EmptyState title="작성한 댓글이 없어요" description="게시글에 댓글을 남겨보세요" />;
  }

  return (
    <div className="flex flex-col gap-3">
      {comments.map((comment) => (
        <button
          key={comment.id}
          onClick={() => navigate(`/board/${comment.postId}`)}
          className="w-full rounded-xl border border-gray-100 bg-white p-4 text-left transition-colors hover:border-primary-200 hover:bg-primary-50/30"
        >
          <p className="mb-2 text-sm text-gray-700 line-clamp-2">{comment.content}</p>
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <span className="text-primary-400">원글: {comment.postTitle}</span>
            <span className="text-gray-300">|</span>
            <span>{timeAgo(comment.createdAt)}</span>
          </div>
        </button>
      ))}

      <Pagination
        currentPage={page}
        hasNext={hasNext}
        totalCount={totalCount}
        pageSize={PAGE_SIZE}
        onPageChange={setPage}
      />
    </div>
  );
}
