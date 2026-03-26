interface AvatarProps {
  nickname?: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeStyles = {
  sm: 'h-7 w-7 text-xs',
  md: 'h-9 w-9 text-sm',
  lg: 'h-12 w-12 text-base',
};

const bgColors = [
  'bg-primary-200 text-primary-700',
  'bg-secondary-200 text-secondary-700',
  'bg-sage-200 text-sage-700',
  'bg-warm-300 text-primary-800',
];

function getColorIndex(name: string): number {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return Math.abs(hash) % bgColors.length;
}

export default function Avatar({ nickname = '?', size = 'md', className = '' }: AvatarProps) {
  const initial = nickname === '익명' ? '?' : nickname.charAt(0);
  const colorClass = nickname === '익명' ? 'bg-gray-200 text-gray-500' : bgColors[getColorIndex(nickname)];

  return (
    <div
      className={`inline-flex flex-shrink-0 items-center justify-center rounded-full font-semibold ${sizeStyles[size]} ${colorClass} ${className}`}
    >
      {initial}
    </div>
  );
}
