import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchPosts } from '@/api/postApi';
import { useBoardStore } from '@/store/boardStore';
import { useAuthStore } from '@/store/authStore';
import { Button, Input, Badge, Spinner, Pagination, EmptyState, Avatar } from '@/components/common';
import type { PostListItem, PageResponse } from '@/types';

const THERAPY_TABS: Array<{ label: string; value: string | null }> = [
  { label: '전체', value: null },
  { label: '작업', value: '작업' },
  { label: '언어', value: '언어' },
  { label: '인지', value: '인지' },
  { label: '놀이', value: '놀이' },
  { label: '기타', value: '기타' },
];

const PAGE_SIZE = 10;

export default function BoardPage() {
  const navigate = useNavigate();
  const member = useAuthStore((s) => s.member);
  const { therapyFilter, keyword, setTherapyFilter, setKeyword } = useBoardStore();

  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<PostListItem> | null>(null);
  const [loading, setLoading] = useState(true);
  const [searchInput, setSearchInput] = useState(keyword);

  const loadPosts = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, string | number> = { page, size: PAGE_SIZE };
      if (therapyFilter) params.therapyArea = therapyFilter;
      if (keyword) params.keyword = keyword;
      const res = await fetchPosts(params);
      setData(res);
    } catch {
      // handled by interceptor
    } finally {
      setLoading(false);
    }
  }, [page, therapyFilter, keyword]);

  useEffect(() => {
    loadPosts();
  }, [loadPosts]);

  // Debounce search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchInput !== keyword) {
        setKeyword(searchInput);
        setPage(0);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchInput, keyword, setKeyword]);

  const handleFilterChange = (value: string | null) => {
    setTherapyFilter(value);
    setPage(0);
  };

  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-800">게시판</h1>
        {member && member.role !== 'PENDING' && (
          <Button size="sm" onClick={() => navigate('/board/write')}>
            글쓰기
          </Button>
        )}
      </div>

      {/* Filter tabs */}
      <div className="mb-4 flex flex-wrap gap-2">
        {THERAPY_TABS.map((tab) => (
          <button
            key={tab.label}
            onClick={() => handleFilterChange(tab.value)}
            className={`rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${
              therapyFilter === tab.value
                ? 'bg-primary-400 text-white'
                : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Search */}
      <div className="mb-6">
        <Input
          placeholder="검색어를 입력하세요"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
        />
      </div>

      {/* Content */}
      {loading ? (
        <Spinner className="py-16" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          title="게시글이 없습니다"
          description={keyword ? '다른 검색어로 시도해보세요.' : '첫 게시글을 작성해보세요!'}
          action={
            member && member.role !== 'PENDING' ? (
              <Button size="sm" onClick={() => navigate('/board/write')}>
                글쓰기
              </Button>
            ) : undefined
          }
        />
      ) : (
        <>
          <div className="space-y-3">
            {data.content.map((post) => (
              <PostCard key={post.id} post={post} onClick={() => navigate(`/board/${post.id}`)} />
            ))}
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
    </div>
  );
}

// ====== PostCard ======

interface PostCardProps {
  post: PostListItem;
  onClick: () => void;
}

function PostCard({ post, onClick }: PostCardProps) {
  return (
    <article
      onClick={onClick}
      className="cursor-pointer rounded-xl border border-gray-100 bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="mb-1.5 flex items-center gap-2">
            {post.therapyArea && <Badge variant="therapy">{post.therapyArea}</Badge>}
          </div>
          <h3 className="truncate text-sm font-semibold text-gray-800">{post.title}</h3>
          <div className="mt-2 flex items-center gap-3">
            <div className="flex items-center gap-1.5">
              <Avatar nickname={post.author} size="sm" />
              <span className="text-xs text-gray-500">{post.author}</span>
            </div>
            <span className="text-xs text-gray-400">{formatDate(post.createdAt)}</span>
          </div>
        </div>
      </div>

      <div className="mt-3 flex items-center gap-4 text-xs text-gray-400">
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
    </article>
  );
}

// ====== Utils ======

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 7) return `${diffDay}일 전`;
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
}
