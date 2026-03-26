import { useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { loginWithKakao, loginWithGoogle } from '@/api/authApi';
import { useAuthStore } from '@/store/authStore';

export default function OAuthCallbackPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const calledRef = useRef(false);

  useEffect(() => {
    if (calledRef.current) return;
    calledRef.current = true;

    const params = new URLSearchParams(location.search);
    const code = params.get('code');

    if (!code) {
      showToast('error', '로그인에 실패했습니다');
      navigate('/login', { replace: true });
      return;
    }

    const isKakao = location.pathname.includes('/kakao/');
    const loginFn = isKakao ? loginWithKakao : loginWithGoogle;

    loginFn(code)
      .then(({ data }) => {
        const { accessToken, refreshToken, member } = data.data;
        setAuth(accessToken, member);
        localStorage.setItem('refreshToken', refreshToken);

        switch (member.role) {
          case 'PENDING':
            navigate('/pending', { replace: true });
            break;
          case 'ADMIN':
            navigate('/admin', { replace: true });
            break;
          default:
            navigate('/board', { replace: true });
            break;
        }
      })
      .catch(() => {
        showToast('error', '로그인에 실패했습니다');
        navigate('/login', { replace: true });
      });
  }, [location, navigate, setAuth]);

  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-4 bg-warm-50">
      <Spinner size="lg" />
      <p className="text-sm text-gray-500">로그인 중이에요...</p>
    </div>
  );
}
