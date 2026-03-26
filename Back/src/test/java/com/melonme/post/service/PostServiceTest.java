package com.melonme.post.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.post.domain.Post;
import com.melonme.post.domain.Scrap;
import com.melonme.post.dto.request.PostCreateRequest;
import com.melonme.post.dto.request.PostUpdateRequest;
import com.melonme.post.dto.response.*;
import com.melonme.file.repository.FileRepository;
import com.melonme.like.repository.LikeRepository;
import com.melonme.member.repository.MemberRepository;
import com.melonme.post.repository.PostRepository;
import com.melonme.post.repository.ScrapRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private Post createTestPost(Long memberId, boolean isAnonymous) {
        return Post.builder()
                .memberId(memberId)
                .title("테스트 제목")
                .content("{\"type\":\"doc\"}")
                .therapyArea("언어")
                .isAnonymous(isAnonymous)
                .build();
    }

    // ===== 익명 게시글 작성자 마스킹 =====

    @Test
    @DisplayName("익명 게시글은 다른 회원에게 '익명'으로 표시된다")
    void anonymousPost_shouldMaskAuthor_forOtherMembers() {
        // given
        Post post = createTestPost(1L, true);
        Long otherMemberId = 2L;

        // when
        String author = postService.resolveAuthor(post, otherMemberId);

        // then
        assertThat(author).isEqualTo("익명");
    }

    @Test
    @DisplayName("익명 게시글이라도 본인에게는 작성자 정보가 표시된다")
    void anonymousPost_shouldShowAuthor_forOwner() {
        // given
        Post post = createTestPost(1L, true);
        Long ownerId = 1L;

        // when
        String author = postService.resolveAuthor(post, ownerId);

        // then
        assertThat(author).isNotEqualTo("익명");
    }

    @Test
    @DisplayName("비익명 게시글은 항상 작성자 정보가 표시된다")
    void nonAnonymousPost_shouldShowAuthor() {
        // given
        Post post = createTestPost(1L, false);
        Long otherMemberId = 2L;

        // when
        String author = postService.resolveAuthor(post, otherMemberId);

        // then
        assertThat(author).isNotEqualTo("익명");
    }

    // ===== 조회수 Redis 증가 =====

    @Test
    @DisplayName("게시글 상세 조회 시 Redis 조회수가 증가한다")
    void getPost_shouldIncrementRedisViewCount() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(scrapRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);
        given(fileRepository.findAllByPostId(1L)).willReturn(Collections.emptyList());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment("view:post:1")).willReturn(1L);
        given(valueOperations.get("view:post:1")).willReturn(1);
        given(valueOperations.get("like:post:1")).willReturn(null);

        // when
        postService.getPost(1L, 1L);

        // then
        then(valueOperations).should().increment("view:post:1");
    }

    // ===== 스크랩 토글 (추가/취소) =====

    @Test
    @DisplayName("스크랩하지 않은 게시글을 스크랩하면 추가된다")
    void toggleScrap_shouldAdd_whenNotScrapped() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(scrapRepository.existsByMemberIdAndPostId(2L, 1L)).willReturn(false);
        given(scrapRepository.save(any(Scrap.class))).willReturn(new Scrap(2L, 1L));

        // when
        ScrapResponse response = postService.toggleScrap(1L, 2L);

        // then
        assertThat(response.isScrapped()).isTrue();
        then(scrapRepository).should().save(any(Scrap.class));
    }

    @Test
    @DisplayName("이미 스크랩한 게시글을 스크랩하면 취소된다")
    void toggleScrap_shouldRemove_whenAlreadyScrapped() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(scrapRepository.existsByMemberIdAndPostId(2L, 1L)).willReturn(true);

        // when
        ScrapResponse response = postService.toggleScrap(1L, 2L);

        // then
        assertThat(response.isScrapped()).isFalse();
        then(scrapRepository).should().deleteByMemberIdAndPostId(2L, 1L);
    }

    // ===== 권한 없는 수정/삭제 예외 =====

    @Test
    @DisplayName("본인이 아닌 회원이 게시글을 수정하면 예외가 발생한다")
    void updatePost_shouldThrow_whenNotOwner() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest("수정 제목", null, null, null);

        // when & then
        assertThatThrownBy(() -> postService.updatePost(1L, 2L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NO_PERMISSION);
    }

    @Test
    @DisplayName("본인이 아닌 회원이 게시글을 삭제하면 예외가 발생한다")
    void deletePost_shouldThrow_whenNotOwner() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NO_PERMISSION);
    }

    @Test
    @DisplayName("본인은 게시글을 수정할 수 있다")
    void updatePost_shouldSucceed_whenOwner() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest("수정 제목", "수정 내용", null, null);

        // when
        PostCreateResponse response = postService.updatePost(1L, 1L, request);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("본인은 게시글을 삭제할 수 있다")
    void deletePost_shouldSucceed_whenOwner() {
        // given
        Post post = createTestPost(1L, false);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(fileRepository.findAllByPostId(any())).willReturn(Collections.emptyList());

        // when & then
        assertThatCode(() -> postService.deletePost(1L, 1L))
                .doesNotThrowAnyException();
    }

    // ===== PENDING 상태 작성 불가 =====

    @Test
    @DisplayName("PENDING 상태의 회원은 게시글을 작성할 수 없다")
    void createPost_shouldThrow_whenPendingRole() {
        // given
        PostCreateRequest request = new PostCreateRequest(
                "제목", "{\"type\":\"doc\"}", "언어", false, null);

        // when & then
        assertThatThrownBy(() -> postService.createPost(1L, "PENDING", request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("MEMBER 상태의 회원은 게시글을 작성할 수 있다")
    void createPost_shouldSucceed_whenMemberRole() {
        // given
        PostCreateRequest request = new PostCreateRequest(
                "제목", "{\"type\":\"doc\"}", "언어", false, null);
        Post post = createTestPost(1L, false);
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        PostCreateResponse response = postService.createPost(1L, "MEMBER", request);

        // then
        assertThat(response).isNotNull();
        then(postRepository).should().save(any(Post.class));
    }

    // ===== 게시글 없음 예외 =====

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getPost_shouldThrow_whenNotFound() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(999L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    // ===== 게시글 목록 조회 =====

    @Test
    @DisplayName("게시글 목록을 페이지네이션으로 조회한다")
    void getPosts_shouldReturnPagedResult() {
        // given
        Post post = createTestPost(1L, false);
        Page<Post> postPage = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        given(postRepository.findAllWithFilters(1L, null, null, PageRequest.of(0, 20)))
                .willReturn(postPage);

        // when
        PageResponse<PostListResponse> response = postService.getPosts(1L, null, null, 0, 20);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();
    }

    // ===== 내가 쓴 게시글 목록 =====

    @Test
    @DisplayName("내가 쓴 게시글 목록을 조회한다")
    void getMyPosts_shouldReturnOwnPosts() {
        // given
        Post post = createTestPost(1L, false);
        Page<Post> postPage = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        given(postRepository.findByMemberId(1L, PageRequest.of(0, 20))).willReturn(postPage);

        // when
        PageResponse<PostListResponse> response = postService.getMyPosts(1L, 0, 20);

        // then
        assertThat(response.getContent()).hasSize(1);
    }
}
