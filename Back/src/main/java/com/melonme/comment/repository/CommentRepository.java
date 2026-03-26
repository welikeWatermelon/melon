package com.melonme.comment.repository;

import com.melonme.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT DISTINCT c.* FROM comment c " +
            "WHERE c.post_id = :postId AND c.parent_id IS NULL " +
            "AND (c.deleted_at IS NULL OR EXISTS (" +
            "  SELECT 1 FROM comment r WHERE r.parent_id = c.id AND r.deleted_at IS NULL" +
            ")) " +
            "ORDER BY c.created_at ASC",
            nativeQuery = true)
    List<Comment> findAllByPostIdWithReplies(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.memberId = :memberId AND c.status = 'ACTIVE' ORDER BY c.createdAt DESC")
    Page<Comment> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
