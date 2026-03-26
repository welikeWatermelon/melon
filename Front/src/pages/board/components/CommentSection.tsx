import { useCallback, useEffect, useState } from 'react';
import { fetchComments, createComment, createReply, updateComment, deleteComment } from '@/api/commentApi';
import { toggleLike } from '@/api/likeApi';
import { Avatar, Button, Textarea, showToast } from '@/components/common';
import type { Comment, Reply } from '@/types';

interface CommentSectionProps {
  postId: number;
}

export default function CommentSection({ postId }: CommentSectionProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);

  // New comment form
  const [newContent, setNewContent] = useState('');
  const [newAnonymous, setNewAnonymous] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const loadComments = useCallback(async () => {
    try {
      const res = await fetchComments(postId);
      setComments(res.comments);
    } catch {
      // error handled by interceptor
    } finally {
      setLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  const handleCreateComment = async () => {
    if (!newContent.trim()) return;
    setSubmitting(true);
    try {
      await createComment(postId, { content: newContent.trim(), isAnonymous: newAnonymous });
      setNewContent('');
      setNewAnonymous(false);
      showToast('success', '댓글이 등록되었습니다.');
      await loadComments();
    } catch {
      showToast('error', '댓글 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <h3 className="text-base font-semibold text-gray-800">댓글 {comments.length}개</h3>

      {/* Comment input */}
      <div className="space-y-3 rounded-xl bg-warm-50 p-4">
        <Textarea
          value={newContent}
          onChange={(e) => setNewContent(e.target.value)}
          placeholder="댓글을 남겨보세요..."
          rows={3}
        />
        <div className="flex items-center justify-between">
          <label className="flex cursor-pointer items-center gap-2 text-sm text-gray-500">
            <input
              type="checkbox"
              checked={newAnonymous}
              onChange={(e) => setNewAnonymous(e.target.checked)}
              className="h-4 w-4 rounded border-gray-300 text-primary-400 focus:ring-primary-400"
            />
            익명으로 작성
          </label>
          <Button size="sm" onClick={handleCreateComment} loading={submitting} disabled={!newContent.trim()}>
            등록
          </Button>
        </div>
      </div>

      {/* Comments list */}
      {loading ? (
        <p className="py-4 text-center text-sm text-gray-400">댓글을 불러오는 중...</p>
      ) : comments.length === 0 ? (
        <p className="py-8 text-center text-sm text-gray-400">아직 댓글이 없어요. 첫 댓글을 남겨보세요!</p>
      ) : (
        <div className="space-y-4">
          {comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              postId={postId}
              onRefresh={loadComments}
            />
          ))}
        </div>
      )}
    </div>
  );
}

// ====== CommentItem ======

interface CommentItemProps {
  comment: Comment;
  postId: number;
  onRefresh: () => Promise<void>;
}

function CommentItem({ comment, postId, onRefresh }: CommentItemProps) {
  const [replyOpen, setReplyOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editContent, setEditContent] = useState(comment.content);
  const [likeCount, setLikeCount] = useState(comment.likeCount);
  const [isLiked, setIsLiked] = useState(comment.isLiked ?? false);

  // Reply form
  const [replyContent, setReplyContent] = useState('');
  const [replyAnonymous, setReplyAnonymous] = useState(false);
  const [replySubmitting, setReplySubmitting] = useState(false);

  const isDeleted = comment.content === '삭제된 댓글입니다';
  const isBlocked = comment.content.includes('차단한 사용자');

  const handleLike = async () => {
    try {
      const res = await toggleLike({ targetType: 'COMMENT', targetId: comment.id });
      setLikeCount(res.likeCount);
      setIsLiked(res.isLiked);
    } catch {
      showToast('error', '좋아요 처리에 실패했습니다.');
    }
  };

  const handleEdit = async () => {
    if (!editContent.trim()) return;
    try {
      await updateComment(postId, comment.id, { content: editContent.trim() });
      showToast('success', '댓글이 수정되었습니다.');
      setEditMode(false);
      await onRefresh();
    } catch {
      showToast('error', '댓글 수정에 실패했습니다.');
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;
    try {
      await deleteComment(postId, comment.id);
      showToast('success', '댓글이 삭제되었습니다.');
      await onRefresh();
    } catch {
      showToast('error', '댓글 삭제에 실패했습니다.');
    }
  };

  const handleReply = async () => {
    if (!replyContent.trim()) return;
    setReplySubmitting(true);
    try {
      await createReply(postId, comment.id, { content: replyContent.trim(), isAnonymous: replyAnonymous });
      setReplyContent('');
      setReplyAnonymous(false);
      setReplyOpen(false);
      showToast('success', '답글이 등록되었습니다.');
      await onRefresh();
    } catch {
      showToast('error', '답글 등록에 실패했습니다.');
    } finally {
      setReplySubmitting(false);
    }
  };

  if (isDeleted) {
    return (
      <div className="rounded-xl bg-gray-50 px-4 py-3">
        <p className="text-sm text-gray-400">삭제된 댓글입니다.</p>
        {/* Replies still visible */}
        {comment.replies.length > 0 && (
          <div className="mt-3 space-y-3 border-l-2 border-gray-200 pl-4">
            {comment.replies.map((reply) => (
              <ReplyItem key={reply.id} reply={reply} postId={postId} commentId={comment.id} onRefresh={onRefresh} />
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="rounded-xl bg-white px-4 py-3">
        <div className="flex items-start gap-3">
          <Avatar nickname={comment.isAnonymous ? '익명' : comment.author} size="sm" />
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-gray-700">
                {comment.isAnonymous ? '익명' : comment.author}
              </span>
              <span className="text-xs text-gray-400">{formatDate(comment.createdAt)}</span>
            </div>

            {editMode ? (
              <div className="mt-2 space-y-2">
                <Textarea value={editContent} onChange={(e) => setEditContent(e.target.value)} rows={2} />
                <div className="flex gap-2">
                  <Button size="sm" onClick={handleEdit}>저장</Button>
                  <Button size="sm" variant="ghost" onClick={() => setEditMode(false)}>취소</Button>
                </div>
              </div>
            ) : (
              <p className={`mt-1 text-sm ${isBlocked ? 'text-gray-400' : 'text-gray-600'}`}>
                {comment.content}
              </p>
            )}

            {!editMode && (
              <div className="mt-2 flex items-center gap-3">
                <button
                  onClick={handleLike}
                  className={`flex items-center gap-1 text-xs ${isLiked ? 'text-primary-500' : 'text-gray-400'} hover:text-primary-500`}
                >
                  <svg className="h-3.5 w-3.5" fill={isLiked ? 'currentColor' : 'none'} viewBox="0 0 24 24" stroke="currentColor">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                    />
                  </svg>
                  {likeCount}
                </button>
                <button
                  onClick={() => setReplyOpen(!replyOpen)}
                  className="text-xs text-gray-400 hover:text-primary-500"
                >
                  답글
                </button>
                {comment.isMyComment && !isBlocked && (
                  <>
                    <button
                      onClick={() => {
                        setEditContent(comment.content);
                        setEditMode(true);
                      }}
                      className="text-xs text-gray-400 hover:text-gray-600"
                    >
                      수정
                    </button>
                    <button onClick={handleDelete} className="text-xs text-gray-400 hover:text-danger-500">
                      삭제
                    </button>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Reply form */}
      {replyOpen && (
        <div className="ml-8 space-y-2 rounded-xl bg-warm-50 p-3">
          <Textarea
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="답글을 남겨보세요..."
            rows={2}
          />
          <div className="flex items-center justify-between">
            <label className="flex cursor-pointer items-center gap-2 text-xs text-gray-500">
              <input
                type="checkbox"
                checked={replyAnonymous}
                onChange={(e) => setReplyAnonymous(e.target.checked)}
                className="h-3.5 w-3.5 rounded border-gray-300 text-primary-400 focus:ring-primary-400"
              />
              익명
            </label>
            <div className="flex gap-2">
              <Button size="sm" variant="ghost" onClick={() => setReplyOpen(false)}>취소</Button>
              <Button size="sm" onClick={handleReply} loading={replySubmitting} disabled={!replyContent.trim()}>
                등록
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Replies */}
      {comment.replies.length > 0 && (
        <div className="ml-8 space-y-3 border-l-2 border-primary-100 pl-4">
          {comment.replies.map((reply) => (
            <ReplyItem key={reply.id} reply={reply} postId={postId} commentId={comment.id} onRefresh={onRefresh} />
          ))}
        </div>
      )}
    </div>
  );
}

// ====== ReplyItem ======

interface ReplyItemProps {
  reply: Reply;
  postId: number;
  commentId: number;
  onRefresh: () => Promise<void>;
}

function ReplyItem({ reply, postId, commentId, onRefresh }: ReplyItemProps) {
  const [likeCount, setLikeCount] = useState(reply.likeCount);
  const [isLiked, setIsLiked] = useState(reply.isLiked ?? false);
  const [editMode, setEditMode] = useState(false);
  const [editContent, setEditContent] = useState(reply.content);

  const handleLike = async () => {
    try {
      const res = await toggleLike({ targetType: 'COMMENT', targetId: reply.id });
      setLikeCount(res.likeCount);
      setIsLiked(res.isLiked);
    } catch {
      showToast('error', '좋아요 처리에 실패했습니다.');
    }
  };

  const handleEdit = async () => {
    if (!editContent.trim()) return;
    try {
      await updateComment(postId, reply.id, { content: editContent.trim() });
      showToast('success', '답글이 수정되었습니다.');
      setEditMode(false);
      await onRefresh();
    } catch {
      showToast('error', '답글 수정에 실패했습니다.');
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('답글을 삭제하시겠습니까?')) return;
    try {
      await deleteComment(postId, reply.id);
      showToast('success', '답글이 삭제되었습니다.');
      await onRefresh();
    } catch {
      showToast('error', '답글 삭제에 실패했습니다.');
    }
  };

  return (
    <div className="flex items-start gap-2">
      <Avatar nickname={reply.isAnonymous ? '익명' : reply.author} size="sm" />
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-gray-700">
            {reply.isAnonymous ? '익명' : reply.author}
          </span>
          <span className="text-xs text-gray-400">{formatDate(reply.createdAt)}</span>
        </div>

        {editMode ? (
          <div className="mt-1 space-y-2">
            <Textarea value={editContent} onChange={(e) => setEditContent(e.target.value)} rows={2} />
            <div className="flex gap-2">
              <Button size="sm" onClick={handleEdit}>저장</Button>
              <Button size="sm" variant="ghost" onClick={() => setEditMode(false)}>취소</Button>
            </div>
          </div>
        ) : (
          <p className="mt-0.5 text-sm text-gray-600">{reply.content}</p>
        )}

        {!editMode && (
          <div className="mt-1 flex items-center gap-3">
            <button
              onClick={handleLike}
              className={`flex items-center gap-1 text-xs ${isLiked ? 'text-primary-500' : 'text-gray-400'} hover:text-primary-500`}
            >
              <svg className="h-3 w-3" fill={isLiked ? 'currentColor' : 'none'} viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                />
              </svg>
              {likeCount}
            </button>
            {reply.isMyComment && (
              <>
                <button
                  onClick={() => {
                    setEditContent(reply.content);
                    setEditMode(true);
                  }}
                  className="text-xs text-gray-400 hover:text-gray-600"
                >
                  수정
                </button>
                <button onClick={handleDelete} className="text-xs text-gray-400 hover:text-danger-500">
                  삭제
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </div>
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
