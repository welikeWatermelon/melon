import { create } from 'zustand';

interface BoardStore {
  therapyFilter: string | null;
  keyword: string;
  setTherapyFilter: (v: string | null) => void;
  setKeyword: (v: string) => void;
}

export const useBoardStore = create<BoardStore>((set) => ({
  therapyFilter: null,
  keyword: '',
  setTherapyFilter: (v) => set({ therapyFilter: v }),
  setKeyword: (v) => set({ keyword: v }),
}));
