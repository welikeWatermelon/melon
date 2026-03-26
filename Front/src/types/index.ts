// === 공통 응답 ===
export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export interface ApiErrorResponse {
  success: false;
  code: string;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  hasNext: boolean;
  totalCount: number;
}

// === 도메인: Member ===
export type Role = 'PENDING' | 'MEMBER' | 'ADMIN';
export type Provider = 'KAKAO' | 'GOOGLE';
export type TherapyArea = '작업' | '언어' | '인지' | '놀이' | '기타';

export interface Member {
  id: number;
  nickname: string;
  therapyArea: string;
  role: Role;
}

export interface MemberUpdateRequest {
  nickname?: string;
  therapyArea?: string;
}

export interface LicenseStatus {
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  adminMemo?: string;
}

// === 도메인: Auth ===
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  member: Member;
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken?: string;
}

// === 도메인: Post ===
export type PostStatus = 'ACTIVE' | 'HIDDEN' | 'DELETED';

export interface PostListItem {
  id: number;
  title: string;
  author: string;
  therapyArea: string;
  likeCount: number;
  commentCount: number;
  viewCount: number;
  createdAt: string;
}

export interface PostDetail {
  id: number;
  title: string;
  content: string;
  author: string;
  isAnonymous: boolean;
  therapyArea: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  isScrapped: boolean;
  isMyPost: boolean;
  files: FileInfo[];
  createdAt: string;
  updatedAt: string;
}

export interface PostCreateRequest {
  title: string;
  content: string;
  therapyArea?: string;
  isAnonymous: boolean;
  fileIds?: number[];
}

export interface PostUpdateRequest {
  title?: string;
  content?: string;
  therapyArea?: string;
  fileIds?: number[];
}

// === 도메인: Comment ===
export interface Reply {
  id: number;
  author: string;
  content: string;
  isAnonymous: boolean;
  likeCount: number;
  isLiked: boolean;
  isMyComment: boolean;
  createdAt: string;
}

export interface Comment {
  id: number;
  author: string;
  content: string;
  isAnonymous: boolean;
  likeCount: number;
  isLiked: boolean;
  isMyComment: boolean;
  createdAt: string;
  replies: Reply[];
}

export interface CommentCreateRequest {
  content: string;
  isAnonymous: boolean;
}

export interface MyComment {
  id: number;
  content: string;
  postId: number;
  postTitle: string;
  createdAt: string;
}

// === 도메인: Like ===
export type LikeTargetType = 'POST' | 'COMMENT';

export interface LikeToggleRequest {
  targetType: LikeTargetType;
  targetId: number;
}

export interface LikeToggleResponse {
  isLiked: boolean;
  likeCount: number;
}

// === 도메인: File ===
export interface FileInfo {
  id: number;
  originalName: string;
  fileSize: number;
}

export interface FileUploadResponse {
  fileId: number;
  originalName: string;
  fileSize: number;
}

// === 도메인: Report ===
export type ReportTargetType = 'POST' | 'COMMENT';

export interface ReportCreateRequest {
  targetType: ReportTargetType;
  targetId: number;
  reason: string;
}

// === 도메인: Block ===
export interface BlockedMember {
  id: number;
  nickname: string;
}

export interface BlockToggleResponse {
  isBlocked: boolean;
}

// === 도메인: Notification ===
export type NotificationType =
  | 'COMMENT_ON_POST'
  | 'REPLY_ON_COMMENT'
  | 'LIKE_ON_POST'
  | 'LIKE_ON_COMMENT';

export type NotificationTargetType = 'POST' | 'COMMENT';

export interface Notification {
  id: number;
  type: NotificationType;
  targetType: NotificationTargetType;
  targetId: number;
  postId?: number;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationListResponse {
  content: Notification[];
  unreadCount: number;
  hasNext: boolean;
}

// === 도메인: Admin ===
export interface AdminLicense {
  id: number;
  memberId: number;
  nickname: string;
  licenseImgUrl: string;
  status: string;
  createdAt: string;
}

export interface AdminReport {
  id: number;
  reporterId: number;
  targetType: string;
  targetId: number;
  reason: string;
  status: string;
  createdAt: string;
}

export interface AdminMember {
  id: number;
  nickname: string;
  therapyArea: string;
  role: string;
  createdAt: string;
}

export interface AdminStats {
  wau: number;
  mau: number;
  weeklyPostCount: number;
  uploaderRatio: number;
  retentionRate: number;
}

export type ReportAction = 'HIDE_POST' | 'HIDE_COMMENT' | 'DISMISS';
export type MemberAction = 'FORCE_DELETE';

export interface LicenseReviewRequest {
  status: 'APPROVED' | 'REJECTED';
  adminMemo?: string;
}

export interface ScrapResponse {
  isScrapped: boolean;
}
