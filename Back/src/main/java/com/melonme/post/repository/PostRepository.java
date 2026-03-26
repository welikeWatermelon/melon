package com.melonme.post.repository;

import com.melonme.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 게시글 목록 조회 - 차단 회원 제외, 치료영역 필터, 키워드 검색
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'ACTIVE'
            AND p.memberId NOT IN (
                SELECT b.blockedId FROM com.melonme.block.domain.Block b WHERE b.blockerId = :memberId
            )
            AND (:therapyArea IS NULL OR p.therapyArea = :therapyArea)
            AND (:keyword IS NULL OR LOWER(CAST(p.title AS String)) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))
                 OR LOWER(CAST(p.content AS String)) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findAllWithFilters(
            @Param("memberId") Long memberId,
            @Param("therapyArea") String therapyArea,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 차단 테이블이 아직 없는 경우를 위한 기본 목록 조회
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'ACTIVE'
            AND (:therapyArea IS NULL OR p.therapyArea = :therapyArea)
            AND (:keyword IS NULL OR LOWER(CAST(p.title AS String)) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))
                 OR LOWER(CAST(p.content AS String)) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findAllWithFiltersNoBlock(
            @Param("therapyArea") String therapyArea,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 내가 쓴 게시글 목록
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.memberId = :memberId
            AND p.status = 'ACTIVE'
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 스크랩한 게시글 목록 (차단 회원 게시글 제외)
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.id IN (
                SELECT s.postId FROM Scrap s WHERE s.memberId = :memberId
            )
            AND p.memberId NOT IN (
                SELECT b.blockedId FROM com.melonme.block.domain.Block b WHERE b.blockerId = :memberId
            )
            AND p.status = 'ACTIVE'
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findScrappedByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 조회수 배치 업데이트
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 활성 게시글 ID 목록 (배치용)
     */
    @Query("SELECT p.id FROM Post p WHERE p.status = 'ACTIVE'")
    List<Long> findAllActivePostIds();
}
