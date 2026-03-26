package com.melonme.post.service;

import com.melonme.file.domain.FileEntity;
import com.melonme.file.repository.FileRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.like.domain.TargetType;
import com.melonme.like.repository.LikeRepository;
import com.melonme.member.repository.MemberRepository;
import com.melonme.post.domain.Post;
import com.melonme.post.dto.request.PostCreateRequest;
import com.melonme.post.dto.request.PostUpdateRequest;
import com.melonme.post.dto.response.*;
import com.melonme.post.repository.PostRepository;
import com.melonme.post.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String VIEW_COUNT_KEY_PREFIX = "view:post:";
    private static final String LIKE_COUNT_KEY_PREFIX = "like:post:";

    private final PostRepository postRepository;
    private final ScrapRepository scrapRepository;
    private final FileRepository fileRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 게시글 목록 조회 (페이지네이션, 치료영역 필터, 키워드 검색)
     * 차단 회원 게시글 제외
     */
    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getPosts(Long memberId, String therapyArea,
                                                    String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postPage = postRepository.findAllWithFilters(memberId, therapyArea, keyword, pageable);

        Page<PostListResponse> responsePage = postPage.map(post -> PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(resolveAuthor(post, memberId))
                .therapyArea(post.getTherapyArea())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());

        return PageResponse.from(responsePage);
    }

    /**
     * 게시글 상세 조회 (조회수 Redis +1)
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPost(Long postId, Long memberId) {
        Post post = findPostOrThrow(postId);

        // Redis 조회수 +1
        incrementViewCountInRedis(postId);

        boolean isScrapped = scrapRepository.existsByMemberIdAndPostId(memberId, postId);
        boolean isMyPost = post.isOwner(memberId);

        // 조회수는 DB값 + Redis 캐시값을 합산해서 반환
        int totalViewCount = post.getViewCount() + getRedisViewCount(postId);

        // 첨부 파일 조회
        List<PostDetailResponse.FileInfo> files = fileRepository.findAllByPostId(postId).stream()
                .map(f -> PostDetailResponse.FileInfo.builder()
                        .id(f.getId())
                        .originalName(f.getOriginalName())
                        .fileSize(f.getFileSize())
                        .build())
                .collect(Collectors.toList());

        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(resolveAuthor(post, memberId))
                .isAnonymous(post.isAnonymous())
                .therapyArea(post.getTherapyArea())
                .viewCount(totalViewCount)
                .likeCount(post.getLikeCount() + getRedisLikeCount(postId))
                .commentCount(post.getCommentCount())
                .isLiked(likeRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, TargetType.POST, postId))
                .isScrapped(isScrapped)
                .isMyPost(isMyPost)
                .files(files)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public PostCreateResponse createPost(Long memberId, String memberRole, PostCreateRequest request) {
        // PENDING 상태 작성 불가
        if ("PENDING".equals(memberRole)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        Post post = Post.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .content(request.getContent())
                .therapyArea(request.getTherapyArea())
                .isAnonymous(request.getIsAnonymous())
                .build();

        Post saved = postRepository.save(post);

        // 파일 연결
        assignFilesToPost(request.getFileIds(), saved.getId());

        return new PostCreateResponse(saved.getId());
    }

    /**
     * 게시글 수정 (본인만)
     */
    @Transactional
    public PostCreateResponse updatePost(Long postId, Long memberId, PostUpdateRequest request) {
        Post post = findPostOrThrow(postId);

        if (!post.isOwner(memberId)) {
            throw new CustomException(ErrorCode.POST_NO_PERMISSION);
        }

        post.update(request.getTitle(), request.getContent(), request.getTherapyArea());

        // 파일 재연결: 기존 파일 연결 해제 후 새 파일 연결
        if (request.getFileIds() != null) {
            // 기존 파일 연결 해제
            List<FileEntity> existingFiles = fileRepository.findAllByPostId(post.getId());
            existingFiles.forEach(FileEntity::unassignFromPost);

            // 새 파일 연결
            assignFilesToPost(request.getFileIds(), post.getId());
        }

        return new PostCreateResponse(post.getId());
    }

    /**
     * 게시글 삭제 (soft delete, 본인만)
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        Post post = findPostOrThrow(postId);

        if (!post.isOwner(memberId)) {
            throw new CustomException(ErrorCode.POST_NO_PERMISSION);
        }

        // 연관 파일 soft delete
        List<FileEntity> files = fileRepository.findAllByPostId(postId);
        files.forEach(FileEntity::softDelete);

        post.delete();
    }

    /**
     * 내가 쓴 게시글 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getMyPosts(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByMemberId(memberId, pageable);

        Page<PostListResponse> responsePage = postPage.map(post -> PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(resolveAuthor(post, memberId))
                .therapyArea(post.getTherapyArea())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());

        return PageResponse.from(responsePage);
    }

    /**
     * 스크랩한 게시글 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getScrappedPosts(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findScrappedByMemberId(memberId, pageable);

        Page<PostListResponse> responsePage = postPage.map(post -> PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(resolveAuthor(post, memberId))
                .therapyArea(post.getTherapyArea())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build());

        return PageResponse.from(responsePage);
    }

    /**
     * 스크랩 토글
     */
    @Transactional
    public ScrapResponse toggleScrap(Long postId, Long memberId) {
        findPostOrThrow(postId);

        boolean exists = scrapRepository.existsByMemberIdAndPostId(memberId, postId);

        if (exists) {
            scrapRepository.deleteByMemberIdAndPostId(memberId, postId);
            return new ScrapResponse(false);
        } else {
            scrapRepository.save(new com.melonme.post.domain.Scrap(memberId, postId));
            return new ScrapResponse(true);
        }
    }

    // ===== Internal methods =====

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * 익명 게시글이면 "익명" 반환, 본인 게시글이면 닉네임 반환
     * MemberRepository에서 실제 닉네임 조회, 실패 시 fallback
     */
    String resolveAuthor(Post post, Long currentMemberId) {
        if (post.isAnonymous() && !post.isOwner(currentMemberId)) {
            return "익명";
        }
        return memberRepository.findById(post.getMemberId())
                .map(member -> member.getNickname())
                .orElse("회원" + post.getMemberId());
    }

    private void assignFilesToPost(List<Long> fileIds, Long postId) {
        if (fileIds == null || fileIds.isEmpty()) return;
        List<FileEntity> files = fileRepository.findAllById(fileIds);
        files.forEach(file -> file.assignToPost(postId));
    }

    private void incrementViewCountInRedis(Long postId) {
        String key = VIEW_COUNT_KEY_PREFIX + postId;
        try {
            redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.warn("Redis 조회수 증가 실패: postId={}", postId, e);
        }
    }

    int getRedisViewCount(Long postId) {
        String key = VIEW_COUNT_KEY_PREFIX + postId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return 0;
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            log.warn("Redis 조회수 조회 실패: postId={}", postId, e);
            return 0;
        }
    }

    int getRedisLikeCount(Long postId) {
        String key = LIKE_COUNT_KEY_PREFIX + postId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return 0;
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            log.warn("Redis 좋아요 수 조회 실패: postId={}", postId, e);
            return 0;
        }
    }
}
