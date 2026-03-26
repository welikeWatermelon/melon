package com.melonme.post.repository;

import com.melonme.post.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    Optional<Scrap> findByMemberIdAndPostId(Long memberId, Long postId);

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    void deleteByMemberIdAndPostId(Long memberId, Long postId);
}
