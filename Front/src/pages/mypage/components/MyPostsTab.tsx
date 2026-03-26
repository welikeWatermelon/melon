import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { EmptyState, Pagination, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import api from '@/api/instance';
import type { ApiResponse, PageResponse, PostListItem } from '@/types';
import { timeAgo } from '../utils/timeAgo';

const PAGE_SIZE = 10;

export default function MyPostsTab() {
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
        '/api/posts/my',
        { params: { page: pageNum, size: PAGE_SIZE } },
      );
      setPosts(data.data.content);
      setTotalCount(data.data.totalCount);
      setHasNext(data.data.hasNext);
    } catch {
      showToast('error', '내가 쓴 글을 불러오지 못했어요');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPosts(page);
  }, [page, loadPosts]);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="md" />
      </div>
    );
  }

  if (posts.length === 0) {
    return <EmptyState title="작성한 글이 없어요" description="첫 글을 작성해보세요" />;
  }

  return (
    <div className="flex flex-col gap-3">
      {posts.map((post) => (
        <button
          key={post.id}
          onClick={() => navigate(`/board/${post.id}`)}
          className="w-full rounded-xl border border-gray-100 bg-white p-4 text-left transition-colors hover:border-primary-200 hover:bg-primary-50/30"
        >
          <h4 className="mb-2 text-sm font-semibold text-gray-800 line-clamp-1">
            {post.title}
          </h4>
          <div className="flex items-center gap-3 text-xs text-gray-400">
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
            <span className="flex items-center gap-1">
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              {post.viewCount}
            </span>
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
