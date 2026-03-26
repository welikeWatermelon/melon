import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { getLicenseStatus, submitLicense } from '@/api/authApi';
import { useAuthStore } from '@/store/authStore';
import type { LicenseStatus } from '@/types';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'application/pdf'];
const MAX_SIZE = 10 * 1024 * 1024; // 10MB

type PageStatus = 'loading' | 'not_submitted' | 'pending' | 'rejected' | 'approved';

export default function PendingPage() {
  const navigate = useNavigate();
  const updateMember = useAuthStore((s) => s.updateMember);
  const [status, setStatus] = useState<PageStatus>('loading');
  const [adminMemo, setAdminMemo] = useState<string>('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchStatus = useCallback(async () => {
    try {
      const { data } = await getLicenseStatus();
      const license: LicenseStatus = data.data;

      if (license.status === 'APPROVED') {
        updateMember({ role: 'MEMBER' });
        navigate('/board', { replace: true });
        return;
      }

      if (license.status === 'REJECTED') {
        setStatus('rejected');
        setAdminMemo(license.adminMemo ?? '');
      } else {
        setStatus('pending');
      }
    } catch {
      // 404 or no license = not submitted
      setStatus('not_submitted');
    }
  }, [navigate, updateMember]);

  useEffect(() => {
    fetchStatus();
  }, [fetchStatus]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      showToast('error', 'JPG, PNG, PDF 형식만 업로드할 수 있어요.');
      return;
    }

    if (file.size > MAX_SIZE) {
      showToast('error', '파일 크기는 10MB 이하만 가능해요.');
      return;
    }

    setSelectedFile(file);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setUploading(true);
    try {
      await submitLicense(selectedFile);
      showToast('success', '면허증이 제출되었어요. 검토까지 잠시 기다려주세요.');
      setStatus('pending');
      setSelectedFile(null);
    } catch {
      showToast('error', '업로드에 실패했어요. 다시 시도해주세요.');
    } finally {
      setUploading(false);
    }
  };

  if (status === 'loading') {
    return (
      <div className="flex min-h-dvh items-center justify-center bg-warm-50">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="flex min-h-dvh items-center justify-center bg-warm-50 px-4">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="mb-6 text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary-100">
            <svg className="h-8 w-8 text-primary-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 12l2 2 4-4" />
              <rect x="3" y="4" width="18" height="16" rx="2" />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-gray-800">면허증 인증이 필요해요</h1>
          <p className="mt-2 text-sm text-gray-500">
            치료사 커뮤니티 이용을 위해 면허증을 인증해주세요.
          </p>
        </div>

        {/* Content Card */}
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          {/* Not submitted / Rejected: Upload UI */}
          {(status === 'not_submitted' || status === 'rejected') && (
            <>
              {status === 'rejected' && (
                <div className="mb-4 rounded-xl bg-danger-50 p-4">
                  <p className="mb-1 text-sm font-medium text-danger-600">인증이 거절되었어요</p>
                  {adminMemo && (
                    <p className="text-sm text-danger-500">{adminMemo}</p>
                  )}
                  <p className="mt-2 text-xs text-gray-500">아래에서 다시 제출해주세요.</p>
                </div>
              )}

              <div className="mb-4">
                <p className="mb-3 text-sm font-medium text-gray-700">면허증 업로드</p>
                <div
                  className="cursor-pointer rounded-xl border-2 border-dashed border-gray-200 p-6 text-center transition-colors hover:border-primary-300 hover:bg-primary-50/30"
                  onClick={() => fileInputRef.current?.click()}
                >
                  {selectedFile ? (
                    <div className="flex flex-col items-center gap-2">
                      <svg className="h-8 w-8 text-sage-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
                        <polyline points="14 2 14 8 20 8" />
                        <line x1="16" y1="13" x2="8" y2="13" />
                        <line x1="16" y1="17" x2="8" y2="17" />
                        <polyline points="10 9 9 9 8 9" />
                      </svg>
                      <p className="text-sm font-medium text-gray-700">{selectedFile.name}</p>
                      <p className="text-xs text-gray-400">
                        {(selectedFile.size / 1024 / 1024).toFixed(1)}MB
                      </p>
                    </div>
                  ) : (
                    <div className="flex flex-col items-center gap-2">
                      <svg className="h-8 w-8 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
                        <polyline points="17 8 12 3 7 8" />
                        <line x1="12" y1="3" x2="12" y2="15" />
                      </svg>
                      <p className="text-sm text-gray-500">클릭하여 파일을 선택하세요</p>
                      <p className="text-xs text-gray-400">JPG, PNG, PDF (최대 10MB)</p>
                    </div>
                  )}
                </div>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".jpg,.jpeg,.png,.pdf"
                  className="hidden"
                  onChange={handleFileChange}
                />
              </div>

              <Button
                className="w-full"
                size="lg"
                disabled={!selectedFile}
                loading={uploading}
                onClick={handleUpload}
              >
                면허증 제출하기
              </Button>
            </>
          )}

          {/* Pending: Review in progress */}
          {status === 'pending' && (
            <div className="flex flex-col items-center py-4 text-center">
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-secondary-100">
                <svg className="h-7 w-7 text-secondary-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </div>
              <h2 className="mb-2 text-lg font-semibold text-gray-800">검토 중입니다</h2>
              <p className="text-sm leading-relaxed text-gray-500">
                면허증 검토에는 보통 1~2일이 소요돼요.
                <br />
                승인되면 바로 커뮤니티를 이용할 수 있어요.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
