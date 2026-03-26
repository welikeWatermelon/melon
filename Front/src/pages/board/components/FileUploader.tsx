import { useCallback, useRef, useState } from 'react';
import { uploadFile } from '@/api/fileApi';
import { showToast } from '@/components/common';
import { Spinner } from '@/components/common';
import type { FileInfo } from '@/types';

interface FileUploaderProps {
  files: FileInfo[];
  onChange: (files: FileInfo[]) => void;
}

const ALLOWED_TYPES = [
  'application/pdf',
  'application/x-hwp',
  'application/haansofthwp',
  'image/jpeg',
  'image/png',
  'image/gif',
];

const ALLOWED_EXTENSIONS = ['pdf', 'hwp', 'jpg', 'jpeg', 'png', 'gif'];
const MAX_SIZE = 10 * 1024 * 1024; // 10MB

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes}B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
}

function getExtension(name: string): string {
  return name.split('.').pop()?.toLowerCase() ?? '';
}

export default function FileUploader({ files, onChange }: FileUploaderProps) {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const validateFile = (file: File): boolean => {
    const ext = getExtension(file.name);
    if (!ALLOWED_EXTENSIONS.includes(ext) && !ALLOWED_TYPES.includes(file.type)) {
      showToast('error', `허용되지 않는 파일 형식입니다: ${file.name}`);
      return false;
    }
    if (file.size > MAX_SIZE) {
      showToast('error', `파일 크기가 10MB를 초과합니다: ${file.name}`);
      return false;
    }
    return true;
  };

  const handleUpload = useCallback(
    async (fileList: FileList) => {
      const validFiles = Array.from(fileList).filter(validateFile);
      if (validFiles.length === 0) return;

      setUploading(true);
      try {
        const uploaded: FileInfo[] = [];
        for (const file of validFiles) {
          const res = await uploadFile(file);
          uploaded.push({
            id: res.fileId,
            originalName: res.originalName,
            fileSize: res.fileSize,
          });
        }
        onChange([...files, ...uploaded]);
        showToast('success', '파일이 업로드되었습니다.');
      } catch {
        showToast('error', '파일 업로드에 실패했습니다.');
      } finally {
        setUploading(false);
      }
    },
    [files, onChange],
  );

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragOver(false);
      if (e.dataTransfer.files.length > 0) {
        handleUpload(e.dataTransfer.files);
      }
    },
    [handleUpload],
  );

  const handleRemove = (fileId: number) => {
    onChange(files.filter((f) => f.id !== fileId));
  };

  return (
    <div className="space-y-3">
      {/* Drop zone */}
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragOver(true);
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        className={`flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-4 py-8 transition-colors ${
          dragOver
            ? 'border-primary-400 bg-primary-50'
            : 'border-gray-200 bg-gray-50 hover:border-primary-300 hover:bg-primary-50/50'
        }`}
      >
        {uploading ? (
          <Spinner size="sm" />
        ) : (
          <>
            <svg className="mb-2 h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            <p className="text-sm text-gray-500">
              파일을 드래그하거나 <span className="font-medium text-primary-500">클릭</span>하여 업로드
            </p>
            <p className="mt-1 text-xs text-gray-400">PDF, HWP, JPG, PNG, GIF (최대 10MB)</p>
          </>
        )}
        <input
          ref={inputRef}
          type="file"
          multiple
          accept=".pdf,.hwp,.jpg,.jpeg,.png,.gif"
          className="hidden"
          onChange={(e) => {
            if (e.target.files && e.target.files.length > 0) {
              handleUpload(e.target.files);
              e.target.value = '';
            }
          }}
        />
      </div>

      {/* File list */}
      {files.length > 0 && (
        <ul className="space-y-2">
          {files.map((file) => (
            <li
              key={file.id}
              className="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2"
            >
              <div className="flex items-center gap-2 overflow-hidden">
                <svg className="h-4 w-4 flex-shrink-0 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                  />
                </svg>
                <span className="truncate text-sm text-gray-700">{file.originalName}</span>
                <span className="flex-shrink-0 text-xs text-gray-400">{formatFileSize(file.fileSize)}</span>
              </div>
              <button
                type="button"
                onClick={() => handleRemove(file.id)}
                className="ml-2 flex-shrink-0 rounded-lg p-1 text-gray-400 hover:bg-gray-200 hover:text-gray-600"
              >
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
