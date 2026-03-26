import { create } from 'zustand';
import type { Member } from '@/types';

interface AuthStore {
  accessToken: string | null;
  member: Member | null;
  setAuth: (token: string, member: Member) => void;
  updateMember: (member: Partial<Member>) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  accessToken: localStorage.getItem('accessToken'),
  member: (() => {
    const raw = localStorage.getItem('member');
    return raw ? (JSON.parse(raw) as Member) : null;
  })(),

  setAuth: (token, member) => {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('member', JSON.stringify(member));
    set({ accessToken: token, member });
  },

  updateMember: (partial) =>
    set((state) => {
      if (!state.member) return state;
      const updated = { ...state.member, ...partial };
      localStorage.setItem('member', JSON.stringify(updated));
      return { member: updated };
    }),

  clearAuth: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('member');
    localStorage.removeItem('refreshToken');
    set({ accessToken: null, member: null });
  },
}));
