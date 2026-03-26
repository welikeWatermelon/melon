interface PaginationProps {
  currentPage: number;
  hasNext: boolean;
  totalCount: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  hasNext,
  totalCount,
  pageSize,
  onPageChange,
}: PaginationProps) {
  const totalPages = Math.ceil(totalCount / pageSize);
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages: number[] = [];
    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  };

  return (
    <nav className="flex items-center justify-center gap-1 py-4">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="rounded-lg px-3 py-2 text-sm text-gray-500 hover:bg-gray-100 disabled:text-gray-300 disabled:hover:bg-transparent"
      >
        &larr;
      </button>

      {getPageNumbers().map((page) => (
        <button
          key={page}
          onClick={() => onPageChange(page)}
          className={`rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
            page === currentPage
              ? 'bg-primary-400 text-white'
              : 'text-gray-600 hover:bg-primary-50'
          }`}
        >
          {page + 1}
        </button>
      ))}

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={!hasNext}
        className="rounded-lg px-3 py-2 text-sm text-gray-500 hover:bg-gray-100 disabled:text-gray-300 disabled:hover:bg-transparent"
      >
        &rarr;
      </button>
    </nav>
  );
}
