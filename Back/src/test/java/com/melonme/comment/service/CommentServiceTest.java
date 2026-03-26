package com.melonme.comment.service;

import com.melonme.block.repository.BlockRepository;
import com.melonme.member.repository.MemberRepository;
import com.melonme.comment.domain.Comment;
import com.melonme.comment.domain.CommentCreatedEvent;
import com.melonme.comment.domain.CommentStatus;
import com.melonme.comment.dto.request.CommentCreateRequest;
import com.melonme.comment.dto.request.CommentUpdateRequest;
import com.melonme.comment.dto.response.CommentIdResponse;
import com.melonme.comment.dto.response.CommentListResponse;
import com.melonme.comment.dto.response.CommentResponse;
import com.melonme.comment.dto.response.MyCommentResponse;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Post post;
    private Comment parentComment;
    private Comment replyComment;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .memberId(100L)
                .title("테스트 게시글")
                .content("내용")
                .therapyArea("언어")
                .isAnonymous(false)
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        parentComment = Comment.builder()
                .postId(1L)
                .memberId(10L)
                .content("부모 댓글")
                .isAnonymous(false)
                .build();
        ReflectionTestUtils.setField(parentComment, "id", 1L);
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());

        replyComment = Comment.builder()
                .postId(1L)
                .memberId(20L)
                .parent(parentComment)
                .content("대댓글")
                .isAnonymous(true)
                .build();
        ReflectionTestUtils.setField(replyComment, "id", 2L);
        ReflectionTestUtils.setField(replyComment, "createdAt", LocalDateTime.now());
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("익명 댓글은 작성자를 '익명'으로 마스킹한다")
        void anonymousCommentAuthorMasking() {
            // given
            Comment anonymousComment = Comment.builder()
                    .postId(1L)
                    .memberId(10L)
                    .content("익명 댓글")
                    .isAnonymous(true)
                    .build();
            ReflectionTestUtils.setField(anonymousComment, "id", 3L);
            ReflectionTestUtils.setField(anonymousComment, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(anonymousComment, "replies", new ArrayList<>());

            given(postRepository.existsById(1L)).willReturn(true);
            given(blockRepository.findBlockedIdsByBlockerId(10L)).willReturn(Collections.emptySet());
            given(commentRepository.findAllByPostIdWithReplies(1L)).willReturn(List.of(anonymousComment));

            // when
            CommentListResponse response = commentService.getComments(1L, 10L);

            // then
            assertThat(response.getComments()).hasSize(1);
            assertThat(response.getComments().get(0).getAuthor()).isEqualTo("익명");
        }

        @Test
        @DisplayName("삭제된 댓글에 대댓글이 있으면 '삭제된 댓글입니다'로 표시한다")
        void deletedCommentWithRepliesShowsDeletedText() {
            // given
            Comment deletedComment = Comment.builder()
                    .postId(1L)
                    .memberId(10L)
                    .content("삭제될 댓글")
                    .isAnonymous(false)
                    .build();
            ReflectionTestUtils.setField(deletedComment, "id", 4L);
            ReflectionTestUtils.setField(deletedComment, "status", CommentStatus.DELETED);
            ReflectionTestUtils.setField(deletedComment, "createdAt", LocalDateTime.now());

            Comment activeReply = Comment.builder()
                    .postId(1L)
                    .memberId(20L)
                    .parent(deletedComment)
                    .content("대댓글 내용")
                    .isAnonymous(false)
                    .build();
            ReflectionTestUtils.setField(activeReply, "id", 5L);
            ReflectionTestUtils.setField(activeReply, "createdAt", LocalDateTime.now());

            ReflectionTestUtils.setField(deletedComment, "replies", List.of(activeReply));

            given(postRepository.existsById(1L)).willReturn(true);
            given(blockRepository.findBlockedIdsByBlockerId(10L)).willReturn(Collections.emptySet());
            given(commentRepository.findAllByPostIdWithReplies(1L)).willReturn(List.of(deletedComment));

            // when
            CommentListResponse response = commentService.getComments(1L, 10L);

            // then
            assertThat(response.getComments()).hasSize(1);
            assertThat(response.getComments().get(0).getContent()).isEqualTo("삭제된 댓글입니다");
            assertThat(response.getComments().get(0).getReplies()).hasSize(1);
        }

        @Test
        @DisplayName("삭제된 댓글에 대댓글이 없으면 숨긴다")
        void deletedCommentWithoutRepliesIsHidden() {
            // given
            Comment deletedComment = Comment.builder()
                    .postId(1L)
                    .memberId(10L)
                    .content("삭제될 댓글")
                    .isAnonymous(false)
                    .build();
            ReflectionTestUtils.setField(deletedComment, "id", 4L);
            ReflectionTestUtils.setField(deletedComment, "status", CommentStatus.DELETED);
            ReflectionTestUtils.setField(deletedComment, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(deletedComment, "replies", new ArrayList<>());

            given(postRepository.existsById(1L)).willReturn(true);
            given(blockRepository.findBlockedIdsByBlockerId(10L)).willReturn(Collections.emptySet());
            given(commentRepository.findAllByPostIdWithReplies(1L)).willReturn(List.of(deletedComment));

            // when
            CommentListResponse response = commentService.getComments(1L, 10L);

            // then
            assertThat(response.getComments()).isEmpty();
        }

        @Test
        @DisplayName("차단 회원의 댓글은 '차단한 사용자의 댓글입니다'로 마스킹한다")
        void blockedMemberCommentMasking() {
            // given
            Long blockedMemberId = 99L;
            Comment blockedComment = Comment.builder()
                    .postId(1L)
                    .memberId(blockedMemberId)
                    .content("차단된 사용자 댓글")
                    .isAnonymous(false)
                    .build();
            ReflectionTestUtils.setField(blockedComment, "id", 6L);
            ReflectionTestUtils.setField(blockedComment, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(blockedComment, "replies", new ArrayList<>());

            given(postRepository.existsById(1L)).willReturn(true);
            given(blockRepository.findBlockedIdsByBlockerId(10L)).willReturn(Set.of(blockedMemberId));
            given(commentRepository.findAllByPostIdWithReplies(1L)).willReturn(List.of(blockedComment));

            // when
            CommentListResponse response = commentService.getComments(1L, 10L);

            // then
            assertThat(response.getComments()).hasSize(1);
            assertThat(response.getComments().get(0).getContent()).isEqualTo("차단한 사용자의 댓글입니다");
        }
    }

    @Nested
    @DisplayName("댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("댓글 작성 시 Post의 comment_count가 증가한다")
        void commentCountIncrement() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 10L);
                return saved;
            });

            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "새 댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when
            CommentIdResponse response = commentService.createComment(1L, 10L, "MEMBER", request);

            // then
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(post.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("댓글 작성 시 CommentCreatedEvent를 발행한다")
        void commentCreatedEventPublished() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 10L);
                return saved;
            });

            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "새 댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when
            commentService.createComment(1L, 10L, "MEMBER", request);

            // then
            ArgumentCaptor<CommentCreatedEvent> captor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            CommentCreatedEvent event = captor.getValue();
            assertThat(event.getCommentId()).isEqualTo(10L);
            assertThat(event.getPostId()).isEqualTo(1L);
            assertThat(event.getMemberId()).isEqualTo(10L);
            assertThat(event.getParentCommentId()).isNull();
            assertThat(event.getPostAuthorId()).isEqualTo(100L);
            assertThat(event.getParentCommentAuthorId()).isNull();
        }

        @Test
        @DisplayName("PENDING 상태 회원은 댓글을 작성할 수 없다")
        void pendingMemberCannotCreateComment() {
            // given
            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "새 댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when & then
            assertThatThrownBy(() -> commentService.createComment(1L, 10L, "PENDING", request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("대댓글 작성")
    class CreateReply {

        @Test
        @DisplayName("대댓글의 대댓글을 시도하면 예외가 발생한다")
        void replyToReplyThrowsException() {
            // given
            ReflectionTestUtils.setField(replyComment, "replies", new ArrayList<>());
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(2L)).willReturn(Optional.of(replyComment));

            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "대대댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, 2L, 30L, "MEMBER", request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_DEPTH_EXCEEDED);
        }

        @Test
        @DisplayName("대댓글 작성 시 comment_count가 증가한다")
        void replyCommentCountIncrement() {
            // given
            ReflectionTestUtils.setField(parentComment, "replies", new ArrayList<>());
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 20L);
                return saved;
            });

            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "대댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when
            CommentIdResponse response = commentService.createReply(1L, 1L, 20L, "MEMBER", request);

            // then
            assertThat(response.getId()).isEqualTo(20L);
            assertThat(post.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("대댓글 작성 시 CommentCreatedEvent에 parentCommentId가 포함된다")
        void replyEventContainsParentId() {
            // given
            ReflectionTestUtils.setField(parentComment, "replies", new ArrayList<>());
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 20L);
                return saved;
            });

            CommentCreateRequest request = new CommentCreateRequest();
            ReflectionTestUtils.setField(request, "content", "대댓글");
            ReflectionTestUtils.setField(request, "isAnonymous", false);

            // when
            commentService.createReply(1L, 1L, 20L, "MEMBER", request);

            // then
            ArgumentCaptor<CommentCreatedEvent> captor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().getParentCommentId()).isEqualTo(1L);
            assertThat(captor.getValue().isReply()).isTrue();
            assertThat(captor.getValue().getPostAuthorId()).isEqualTo(100L);
            assertThat(captor.getValue().getParentCommentAuthorId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("본인 댓글만 수정할 수 있다")
        void onlyOwnerCanUpdate() {
            // given
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));

            CommentUpdateRequest request = new CommentUpdateRequest();
            ReflectionTestUtils.setField(request, "content", "수정된 내용");

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(1L, 1L, 999L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_NO_PERMISSION);
        }

        @Test
        @DisplayName("본인 댓글을 수정하면 내용이 변경된다")
        void ownerCanUpdate() {
            // given
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));

            CommentUpdateRequest request = new CommentUpdateRequest();
            ReflectionTestUtils.setField(request, "content", "수정된 내용");

            // when
            CommentIdResponse response = commentService.updateComment(1L, 1L, 10L, request);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(parentComment.getContent()).isEqualTo("수정된 내용");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("댓글 삭제 시 comment_count가 감소한다")
        void commentCountDecrement() {
            // given
            post.incrementCommentCount();
            assertThat(post.getCommentCount()).isEqualTo(1);

            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            commentService.deleteComment(1L, 1L, 10L);

            // then
            assertThat(post.getCommentCount()).isEqualTo(0);
            assertThat(parentComment.getStatus()).isEqualTo(CommentStatus.DELETED);
        }

        @Test
        @DisplayName("본인이 아닌 회원은 댓글을 삭제할 수 없다")
        void nonOwnerCannotDelete() {
            // given
            given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(1L, 1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMMENT_NO_PERMISSION);
        }
    }

    @Nested
    @DisplayName("내가 쓴 댓글 목록")
    class GetMyComments {

        @Test
        @DisplayName("내가 쓴 댓글 목록을 조회한다")
        void getMyCommentsList() {
            // given
            ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.now());
            Page<Comment> commentPage = new PageImpl<>(List.of(parentComment));
            given(commentRepository.findAllByMemberId(eq(10L), any(Pageable.class))).willReturn(commentPage);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            Page<MyCommentResponse> response = commentService.getMyComments(10L, PageRequest.of(0, 20));

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getPostTitle()).isEqualTo("테스트 게시글");
            assertThat(response.getContent().get(0).getContent()).isEqualTo("부모 댓글");
        }
    }
}
