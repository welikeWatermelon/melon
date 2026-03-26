package com.melonme.comment.domain;

import com.melonme.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    @Builder
    public Comment(Long postId, Long memberId, Comment parent, String content, boolean isAnonymous) {
        this.postId = postId;
        this.memberId = memberId;
        this.parent = parent;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.likeCount = 0;
        this.status = CommentStatus.ACTIVE;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
        this.softDelete();
    }

    public void hideByAdmin() {
        this.status = CommentStatus.DELETED;
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isReply() {
        return this.parent != null;
    }

    public boolean isActive() {
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean hasActiveReplies() {
        return this.replies.stream().anyMatch(Comment::isActive);
    }
}
