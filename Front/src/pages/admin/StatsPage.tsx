import { useEffect, useState } from 'react';
import { Spinner } from '@/components/common';
import { showToast } from '@/components/common';
import { fetchStats } from '@/api/adminApi';
import type { AdminStats } from '@/types';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

interface StatCardProps {
  label: string;
  value: string | number;
  description: string;
}

function StatCard({ label, value, description }: StatCardProps) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
      <p className="text-xs font-medium text-gray-400">{label}</p>
      <p className="mt-1 text-2xl font-bold text-gray-800">{value}</p>
      <p className="mt-1 text-xs text-gray-400">{description}</p>
    </div>
  );
}

const PIE_COLORS = ['#F87171', '#E5E7EB'];

export default function StatsPage() {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const result = await fetchStats();
        setStats(result);
      } catch {
        showToast('error', '통계를 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading || !stats) {
    return <Spinner className="py-20" />;
  }

  const barData = [
    { name: 'WAU', value: stats.wau },
    { name: 'MAU', value: stats.mau },
  ];

  const uploaderPercent = Math.round(stats.uploaderRatio * 100);
  const pieData = [
    { name: '작성자', value: uploaderPercent },
    { name: '비작성자', value: 100 - uploaderPercent },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-gray-800">통계 대시보드</h1>

      {/* Stat cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          label="WAU"
          value={stats.wau.toLocaleString()}
          description="주간 활성 유저"
        />
        <StatCard
          label="MAU"
          value={stats.mau.toLocaleString()}
          description="월간 활성 유저"
        />
        <StatCard
          label="주간 게시글"
          value={stats.weeklyPostCount.toLocaleString()}
          description="이번 주 작성된 글"
        />
        <StatCard
          label="잔존율"
          value={`${Math.round(stats.retentionRate * 100)}%`}
          description="사용자 잔존율"
        />
      </div>

      {/* Charts */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Bar chart: WAU vs MAU */}
        <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
          <h2 className="mb-4 text-sm font-semibold text-gray-700">활성 유저 비교</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#9CA3AF' }} />
                <YAxis tick={{ fontSize: 12, fill: '#9CA3AF' }} />
                <Tooltip
                  contentStyle={{
                    borderRadius: '12px',
                    border: '1px solid #f0f0f0',
                    fontSize: '13px',
                  }}
                />
                <Bar dataKey="value" fill="#F87171" radius={[8, 8, 0, 0]} barSize={48} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Pie chart: Uploader ratio */}
        <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
          <h2 className="mb-4 text-sm font-semibold text-gray-700">게시글 작성 비율</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={4}
                  dataKey="value"
                  label={({ name, value }) => `${name} ${value}%`}
                >
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[index]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    borderRadius: '12px',
                    border: '1px solid #f0f0f0',
                    fontSize: '13px',
                  }}
                  formatter={(value) => [`${value}%`, '']}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
