export default function Footer() {
  return (
    <footer className="mt-auto border-t border-gray-100 bg-gray-50 py-6">
      <div className="mx-auto max-w-5xl px-4 text-center text-xs text-gray-400">
        <p>&copy; {new Date().getFullYear()} 멜론미. 치료사를 위한 커뮤니티.</p>
      </div>
    </footer>
  );
}
