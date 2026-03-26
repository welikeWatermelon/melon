import { Outlet } from 'react-router-dom';
import GNB from './GNB';
import Footer from './Footer';
import { ErrorBoundary, Toast } from '@/components/common';
import SSEProvider from '@/components/SSEProvider';

export default function MainLayout() {
  return (
    <SSEProvider>
      <div className="flex min-h-dvh flex-col">
        <GNB />
        <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6">
          <ErrorBoundary>
            <Outlet />
          </ErrorBoundary>
        </main>
        <Footer />
        <Toast />
      </div>
    </SSEProvider>
  );
}
