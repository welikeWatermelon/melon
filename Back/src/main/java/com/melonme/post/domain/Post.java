package com.melonme.post.domain;

import com.melonme.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "therapy_area")
    private String therapyArea;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Builder
    public Post(Long memberId, String title, String content, String therapyArea, boolean isAnonymous) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.therapyArea = therapyArea;
        this.isAnonymous = isAnonymous;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.status = PostStatus.ACTIVE;
    }

    public void update(String title, String content, String therapyArea) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (therapyArea != null) this.therapyArea = therapyArea;
    }

    public void hide() {
        this.status = PostStatus.HIDDEN;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
        this.softDelete();
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void addViewCount(int count) {
        this.viewCount += count;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
