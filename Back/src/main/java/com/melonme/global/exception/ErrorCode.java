package com.melonme.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "리소스를 찾을 수 없습니다."),

    // Auth
    AUTH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증 토큰이 없습니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_003", "접근 권한이 없습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_005", "리프레시 토큰을 찾을 수 없습니다."),
    AUTH_OAUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH_006", "소셜 로그인에 실패했습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_000", "회원을 찾을 수 없습니다."),
    MEMBER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "MEMBER_001", "이미 사용 중인 닉네임입니다."),
    MEMBER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "MEMBER_002", "이미 탈퇴한 회원입니다."),

    // License
    LICENSE_ALREADY_PENDING(HttpStatus.CONFLICT, "LICENSE_001", "이미 심사 중인 인증 신청이 있습니다."),
    LICENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "LICENSE_002", "인증 신청 내역이 없습니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),
    POST_NO_PERMISSION(HttpStatus.FORBIDDEN, "POST_002", "게시글 수정/삭제 권한이 없습니다."),

    // File
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_001", "파일 크기가 초과되었습니다."),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FILE_002", "허용되지 않는 파일 형식입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_003", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_004", "파일 업로드에 실패했습니다."),

    // Report
    REPORT_DUPLICATE(HttpStatus.CONFLICT, "REPORT_001", "이미 신고한 대상입니다."),
    REPORT_SELF(HttpStatus.BAD_REQUEST, "REPORT_002", "본인의 게시글/댓글은 신고할 수 없습니다."),
    REPORT_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_003", "신고 대상을 찾을 수 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_NO_PERMISSION(HttpStatus.FORBIDDEN, "COMMENT_002", "댓글 수정/삭제 권한이 없습니다."),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT_003", "대댓글에는 답글을 달 수 없습니다."),

    // Block
    BLOCK_SELF(HttpStatus.BAD_REQUEST, "BLOCK_001", "본인을 차단할 수 없습니다."),

    // Admin
    ADMIN_LICENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_001", "인증 신청을 찾을 수 없습니다."),
    ADMIN_LICENSE_ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "ADMIN_002", "이미 처리된 인증 신청입니다."),
    ADMIN_REJECT_MEMO_REQUIRED(HttpStatus.BAD_REQUEST, "ADMIN_003", "거절 사유를 입력해야 합니다."),
    ADMIN_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_004", "신고를 찾을 수 없습니다."),
    ADMIN_REPORT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "ADMIN_005", "이미 처리된 신고입니다."),
    ADMIN_INVALID_ACTION(HttpStatus.BAD_REQUEST, "ADMIN_006", "유효하지 않은 처리 액션입니다."),
    ADMIN_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_007", "신고 대상을 찾을 수 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
