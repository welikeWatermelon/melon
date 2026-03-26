package com.melonme.file.domain;

import com.melonme.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class FileEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "s3_url", nullable = false)
    private String s3Url;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Builder
    public FileEntity(Long memberId, String originalName, String s3Url, String s3Key,
                      Long fileSize, String mimeType) {
        this.memberId = memberId;
        this.originalName = originalName;
        this.s3Url = s3Url;
        this.s3Key = s3Key;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public void assignToPost(Long postId) {
        this.postId = postId;
    }

    public void unassignFromPost() {
        this.postId = null;
    }
}
