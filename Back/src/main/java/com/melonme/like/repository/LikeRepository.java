package com.melonme.like.repository;

import com.melonme.like.domain.Like;
import com.melonme.like.domain.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType targetType, Long targetId);

    boolean existsByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType targetType, Long targetId);

    @Modifying
    @Query(value = "UPDATE post SET like_count = :count WHERE id = :postId", nativeQuery = true)
    void updatePostLikeCount(@Param("postId") Long postId, @Param("count") int count);

    @Modifying
    @Query(value = "UPDATE comment SET like_count = :count WHERE id = :commentId", nativeQuery = true)
    void updateCommentLikeCount(@Param("commentId") Long commentId, @Param("count") int count);
}
