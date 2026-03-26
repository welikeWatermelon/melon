package com.melonme.comment.service;

import com.melonme.comment.domain.Comment;
import com.melonme.comment.domain.CommentCreatedEvent;
import com.melonme.comment.domain.CommentStatus;
import com.melonme.comment.dto.request.CommentCreateRequest;
import com.melonme.comment.dto.request.CommentUpdateRequest;
import com.melonme.comment.dto.response.*;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import com.melonme.block.repository.BlockRepository;
import com.melonme.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public CommentListResponse getComments(Long postId, Long memberId) {
        validatePostExists(postId);

        Set<Long> blockedMemberIds = blockRepository.findBlockedIdsByBlockerId(memberId);
        List<Comment> parentComments = commentRepository.findAllByPostIdWithReplies(postId);

        List<CommentResponse> commentResponses = new ArrayList<>();

        for (Comment comment : parentComments) {
            List<ReplyResponse> replyResponses = buildReplyResponses(comment, memberId, blockedMemberIds);

            if (!comment.isActive()) {
                if (!replyResponses.isEmpty()) {
                    commentResponses.add(CommentResponse.deleted(comment, replyResponses));
                }
                continue;
            }

            if (blockedMemberIds.contains(comment.getMemberId())) {
                commentResponses.add(CommentResponse.blocked(
                        comment,
                        comment.isOwner(memberId),
                        replyResponses
                ));
                continue;
            }

            String author = comment.isAnonymous() ? "익명" : getMemberNickname(comment.getMemberId());
            commentResponses.add(CommentResponse.of(
                    comment,
                    author,
                    comment.isOwner(memberId),
                    replyResponses
            ));
        }

        return CommentListResponse.of(commentResponses);
    }

    @Transactional
    public CommentIdResponse createComment(Long postId, Long memberId, String memberRole, CommentCreateRequest request) {
        validateMemberRole(memberRole);
        Post post = findPostById(postId);

        Comment comment = Comment.builder()
                .postId(postId)
                .memberId(memberId)
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous())
                .build();

        Comment saved = commentRepository.save(comment);
        post.incrementCommentCount();

        eventPublisher.publishEvent(new CommentCreatedEvent(
                saved.getId(), postId, memberId, null,
                post.getMemberId(), null
        ));

        return CommentIdResponse.of(saved.getId());
    }

    @Transactional
    public CommentIdResponse createReply(Long postId, Long parentId, Long memberId, String memberRole, CommentCreateRequest request) {
        validateMemberRole(memberRole);
        Post post = findPostById(postId);

        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parentComment.getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (parentComment.isReply()) {
            throw new CustomException(ErrorCode.COMMENT_DEPTH_EXCEEDED);
        }

        Comment reply = Comment.builder()
                .postId(postId)
                .memberId(memberId)
                .parent(parentComment)
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous())
                .build();

        Comment saved = commentRepository.save(reply);
        post.incrementCommentCount();

        eventPublisher.publishEvent(new CommentCreatedEvent(
                saved.getId(), postId, memberId, parentId,
                post.getMemberId(), parentComment.getMemberId()
        ));

        return CommentIdResponse.of(saved.getId());
    }

    @Transactional
    public CommentIdResponse updateComment(Long postId, Long commentId, Long memberId, CommentUpdateRequest request) {
        Comment comment = findCommentById(commentId);

        if (!comment.getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.isOwner(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_NO_PERMISSION);
        }

        comment.updateContent(request.getContent());
        return CommentIdResponse.of(comment.getId());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long memberId) {
        Comment comment = findCommentById(commentId);

        if (!comment.getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.isOwner(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_NO_PERMISSION);
        }

        comment.delete();

        Post post = findPostById(postId);
        post.decrementCommentCount();
    }

    @Transactional(readOnly = true)
    public Page<MyCommentResponse> getMyComments(Long memberId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByMemberId(memberId, pageable);

        return comments.map(comment -> {
            String postTitle = postRepository.findById(comment.getPostId())
                    .map(Post::getTitle)
                    .orElse("삭제된 게시글");
            return MyCommentResponse.of(comment, postTitle);
        });
    }

    private List<ReplyResponse> buildReplyResponses(Comment parentComment, Long memberId, Set<Long> blockedMemberIds) {
        List<ReplyResponse> replies = new ArrayList<>();

        for (Comment reply : parentComment.getReplies()) {
            if (!reply.isActive()) {
                replies.add(ReplyResponse.deleted(reply));
                continue;
            }

            if (blockedMemberIds.contains(reply.getMemberId())) {
                replies.add(ReplyResponse.blocked(reply, reply.isOwner(memberId)));
                continue;
            }

            String author = reply.isAnonymous() ? "익명" : getMemberNickname(reply.getMemberId());
            replies.add(ReplyResponse.of(reply, author, reply.isOwner(memberId)));
        }

        return replies;
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateMemberRole(String memberRole) {
        if ("PENDING".equals(memberRole)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    private String getMemberNickname(Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> member.getNickname())
                .orElse("회원" + memberId);
    }
}
