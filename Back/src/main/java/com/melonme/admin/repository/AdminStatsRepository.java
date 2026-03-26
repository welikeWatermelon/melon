package com.melonme.admin.repository;

import com.melonme.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminStatsRepository extends JpaRepository<Member, Long> {

    // WAU: 최근 7일 로그인한 unique member 수 (updatedAt 기준)
    @Query("SELECT COUNT(DISTINCT m.id) FROM Member m WHERE m.updatedAt >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    // 전체 회원 수
    @Query("SELECT COUNT(m) FROM Member m")
    long countAllMembers();

    // MEMBER role 회원 수
    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = 'MEMBER'")
    long countMemberRoleMembers();

    // 최근 N일 게시글 수
    @Query(value = "SELECT COUNT(*) FROM post WHERE created_at >= :since AND deleted_at IS NULL", nativeQuery = true)
    long countPostsSince(@Param("since") LocalDateTime since);

    // 최근 N일 게시글 작성자 unique 수
    @Query(value = "SELECT COUNT(DISTINCT member_id) FROM post WHERE created_at >= :since AND deleted_at IS NULL", nativeQuery = true)
    long countUniquePostAuthorsSince(@Param("since") LocalDateTime since);

    // 30일 잔존율: 30일 전 가입한 회원 중 최근 7일 활동한 회원 비율
    @Query(value = """
            SELECT COUNT(DISTINCT m.id)
            FROM member m
            WHERE m.created_at <= :joinedBefore
            AND m.updated_at >= :activeSince
            AND m.deleted_at IS NULL
            """, nativeQuery = true)
    long countRetainedMembers(@Param("joinedBefore") LocalDateTime joinedBefore,
                              @Param("activeSince") LocalDateTime activeSince);

    @Query(value = """
            SELECT COUNT(*)
            FROM member m
            WHERE m.created_at <= :joinedBefore
            AND m.deleted_at IS NULL
            """, nativeQuery = true)
    long countMembersJoinedBefore(@Param("joinedBefore") LocalDateTime joinedBefore);
}
