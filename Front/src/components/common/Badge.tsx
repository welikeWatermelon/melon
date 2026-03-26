import type { ReactNode } from 'react';

type BadgeVariant = 'therapy' | 'status' | 'count';

interface BadgeProps {
  children: ReactNode;
  variant?: BadgeVariant;
  className?: string;
}

const variantStyles: Record<BadgeVariant, string> = {
  therapy: 'bg-secondary-100 text-secondary-600',
  status: 'bg-sage-100 text-sage-600',
  count: 'bg-primary-100 text-primary-600',
};

export default function Badge({ children, variant = 'therapy', className = '' }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${variantStyles[variant]} ${className}`}
    >
      {children}
    </span>
  );
}
