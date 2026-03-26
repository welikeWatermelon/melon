import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost } from '@/api/postApi';
import { Button, Input, showToast } from '@/components/common';
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

export default function PostWritePage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [therapyArea, setTherapyArea] = useState<string>('');
  const [isAnonymous, setIsAnonymous] = useState(false);
  const [files, setFiles] = useState<FileInfo[]>([]);
  const [submitting, setSubmitting] = useState(false);

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
      const res = await createPost({
        title: title.trim(),
        content,
        therapyArea: therapyArea || undefined,
        isAnonymous,
        fileIds: files.length > 0 ? files.map((f) => f.id) : undefined,
      });
      showToast('success', '게시글이 작성되었습니다.');
      navigate(`/board/${res.id}`);
    } catch {
      showToast('error', '게시글 작성에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-3xl px-4 py-6">
      <h1 className="mb-6 text-xl font-bold text-gray-800">글쓰기</h1>

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

        {/* Anonymous */}
        <label className="flex cursor-pointer items-center gap-2">
          <input
            type="checkbox"
            checked={isAnonymous}
            onChange={(e) => setIsAnonymous(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300 text-primary-400 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-600">익명으로 작성</span>
        </label>

        {/* Editor */}
        <div>
          <span className="mb-1.5 block text-sm font-medium text-gray-700">내용</span>
          <TipTapEditor content={content} onChange={setContent} />
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
            작성 완료
          </Button>
        </div>
      </div>
    </div>
  );
}
