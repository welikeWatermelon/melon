import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/common';

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-dvh bg-warm-50">
      {/* Hero Section */}
      <section className="flex flex-col items-center justify-center px-4 pt-20 pb-16 text-center md:pt-32 md:pb-24">
        <div className="mb-6 flex items-center gap-2 text-primary-400">
          <svg className="h-8 w-8" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
          </svg>
          <span className="text-lg font-semibold">멜론미</span>
        </div>

        <h1 className="mb-4 text-3xl font-bold leading-tight text-gray-900 md:text-5xl">
          치료사를 위한
          <br />
          <span className="text-primary-400">따뜻한 커뮤니티</span>
        </h1>

        <p className="mb-10 max-w-md text-base leading-relaxed text-gray-500 md:text-lg">
          같은 고민을 나누고, 서로의 성장을 응원하는
          <br className="hidden sm:block" />
          치료사 전용 안전한 공간이에요.
        </p>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Button
            size="lg"
            className="min-w-[160px]"
            onClick={() => navigate('/login')}
          >
            로그인하기
          </Button>
          <Button
            variant="secondary"
            size="lg"
            className="min-w-[160px]"
            onClick={() => {
              document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' });
            }}
          >
            더 알아보기
          </Button>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="bg-white px-4 py-16 md:py-24">
        <div className="mx-auto max-w-4xl">
          <h2 className="mb-12 text-center text-2xl font-bold text-gray-800 md:text-3xl">
            멜론미만의 특별함
          </h2>

          <div className="grid gap-8 md:grid-cols-3">
            {/* Card 1 */}
            <div className="flex flex-col items-center rounded-2xl bg-warm-50 p-8 text-center">
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-primary-100 text-primary-500">
                <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
              </div>
              <h3 className="mb-2 text-lg font-semibold text-gray-800">폐쇄형 커뮤니티</h3>
              <p className="text-sm leading-relaxed text-gray-500">
                면허 인증을 완료한 치료사만 참여할 수 있어요. 안전한 공간에서 자유롭게 이야기해요.
              </p>
            </div>

            {/* Card 2 */}
            <div className="flex flex-col items-center rounded-2xl bg-secondary-50 p-8 text-center">
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-secondary-100 text-secondary-500">
                <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9 12l2 2 4-4" />
                  <rect x="3" y="4" width="18" height="16" rx="2" />
                </svg>
              </div>
              <h3 className="mb-2 text-lg font-semibold text-gray-800">면허 인증</h3>
              <p className="text-sm leading-relaxed text-gray-500">
                간편한 면허증 인증으로 빠르게 커뮤니티에 합류할 수 있어요.
              </p>
            </div>

            {/* Card 3 */}
            <div className="flex flex-col items-center rounded-2xl bg-sage-50 p-8 text-center">
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-sage-100 text-sage-500">
                <svg className="h-7 w-7" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
                </svg>
              </div>
              <h3 className="mb-2 text-lg font-semibold text-gray-800">익명 소통</h3>
              <p className="text-sm leading-relaxed text-gray-500">
                민감한 주제도 익명으로 편하게 나눌 수 있어요. 부담 없이 소통해요.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="px-4 py-16 text-center md:py-24">
        <h2 className="mb-4 text-2xl font-bold text-gray-800 md:text-3xl">
          지금 바로 시작하세요
        </h2>
        <p className="mb-8 text-gray-500">
          따뜻한 치료사 커뮤니티가 기다리고 있어요.
        </p>
        <Button
          size="lg"
          className="min-w-[200px]"
          onClick={() => navigate('/login')}
        >
          멜론미 시작하기
        </Button>
      </section>
    </div>
  );
}
