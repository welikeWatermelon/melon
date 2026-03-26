import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchPost, updatePost } from '@/api/postApi';
import { Button, Input, Spinner, showToast } from '@/components/common';
import TipTapEditor from './components/TipTapEditor';
import FileUploader from './components/FileUploader';
import type { FileInfo, TherapyArea } from '@/types';

const THERAPY_OPTIONS: Array<{ label: string; value: TherapyArea | '' }> = [
  { label: '선택안함', value: '' },
  { label: '작업', value: '작업' },
  { label: '언어', value: '언어' },
  { label: '인지', value: '인지' },
  { label: '놀이', value: '놀이' },
  { label: '기타', value: '기타' },
];

export default function PostEditPage() {
  const { postId } = useParams<{ postId: string }>();
  const navigate = useNavigate();
  const id = Number(postId);

  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [therapyArea, setTherapyArea] = useState<string>('');
  const [files, setFiles] = useState<FileInfo[]>([]);
  const [submitting, setSubmitting] = useState(false);

  const loadPost = useCallback(async () => {
    try {
      const post = await fetchPost(id);
      if (!post.isMyPost) {
        showToast('error', '수정 권한이 없습니다.');
        navigate(`/board/${id}`);
        return;
      }
      setTitle(post.title);
      setContent(post.content);
      setTherapyArea(post.therapyArea || '');
      setFiles(post.files);
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

  const handleSubmit = async () => {
    if (!title.trim()) {
      showToast('error', '제목을 입력해주세요.');
      return;
    }
    if (!content || content === '{}') {
      showToast('error', '내용을 입력해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      await updatePost(id, {
        title: title.trim(),
        content,
        therapyArea: therapyArea || undefined,
        fileIds: files.map((f) => f.id),
      });
      showToast('success', '게시글이 수정되었습니다.');
      navigate(`/board/${id}`);
    } catch {
      showToast('error', '게시글 수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <Spinner className="py-20" />;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      <h1 className="mb-6 text-xl font-bold text-gray-800">글 수정</h1>

      <div className="space-y-5">
        {/* Title */}
        <Input
          label="제목"
          placeholder="제목을 입력해주세요"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        {/* Therapy area */}
        <div className="flex flex-col gap-1.5">
          <span className="text-sm font-medium text-gray-700">치료 영역</span>
          <div className="flex flex-wrap gap-2">
            {THERAPY_OPTIONS.map((opt) => (
              <button
                key={opt.label}
                type="button"
                onClick={() => setTherapyArea(opt.value)}
                className={`rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${
                  therapyArea === opt.value
                    ? 'bg-primary-400 text-white'
                    : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                }`}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>

        {/* Editor */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-gray-700">내용</span>
          {content && <TipTapEditor content={content} onChange={setContent} />}
        </div>

        {/* File upload */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-gray-700">파일 첨부</span>
          <FileUploader files={files} onChange={setFiles} />
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-3 pt-2">
          <Button variant="ghost" onClick={() => navigate(-1)}>
            취소
          </Button>
          <Button onClick={handleSubmit} loading={submitting}>
            수정 완료
          </Button>
        </div>
      </div>
    </div>
  );
}
