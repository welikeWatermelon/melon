import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, EmptyState, Pagination, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import api from '@/api/instance';
import type { ApiResponse, PageResponse, PostListItem, ScrapResponse } from '@/types';
import { timeAgo } from '../utils/timeAgo';

const PAGE_SIZE = 10;

export default function ScrappedPostsTab() {
  const navigate = useNavigate();
  const [posts, setPosts] = useState<PostListItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadPosts = useCallback(async (pageNum: number) => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<PageResponse<PostListItem>>>(
        '/api/posts/scrapped',
        { params: { page: pageNum, size: PAGE_SIZE } },
      );
      setPosts(data.data.content);
      setTotalCount(data.data.totalCount);
      setHasNext(data.data.hasNext);
    } catch {
      showToast('error', '스크랩한 글을 불러오지 못했어요');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPosts(page);
  }, [page, loadPosts]);

  const handleUnscrap = async (postId: number) => {
    try {
      await api.post<ApiResponse<ScrapResponse>>(`/api/posts/${postId}/scraps`);
      setPosts((prev) => prev.filter((p) => p.id !== postId));
      setTotalCount((prev) => prev - 1);
      showToast('success', '스크랩을 취소했어요');
    } catch {
      showToast('error', '스크랩 취소에 실패했어요');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="md" />
      </div>
    );
  }

  if (posts.length === 0) {
    return <EmptyState title="스크랩한 글이 없어요" description="마음에 드는 글을 스크랩해보세요" />;
  }

  return (
    <div className="flex flex-col gap-3">
      {posts.map((post) => (
        <div
          key={post.id}
          className="flex items-start justify-between rounded-xl border border-gray-100 bg-white p-4 transition-colors hover:border-primary-200 hover:bg-primary-50/30"
        >
          <button
            onClick={() => navigate(`/board/${post.id}`)}
            className="flex-1 text-left"
          >
            <h4 className="mb-2 text-sm font-semibold text-gray-800 line-clamp-1">
              {post.title}
            </h4>
            <div className="flex items-center gap-3 text-xs text-gray-400">
              <span>{post.author}</span>
              <span>{timeAgo(post.createdAt)}</span>
              <span className="flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                {post.likeCount}
              </span>
              <span className="flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                {post.commentCount}
              </span>
            </div>
          </button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => handleUnscrap(post.id)}
            className="ml-2 shrink-0 text-gray-400 hover:text-danger-500"
          >
            <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
            </svg>
          </Button>
        </div>
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
