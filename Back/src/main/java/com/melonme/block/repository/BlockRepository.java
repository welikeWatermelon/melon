package com.melonme.block.repository;

import com.melonme.block.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<Block> findAllByBlockerId(Long blockerId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("SELECT b.blockedId FROM Block b WHERE b.blockerId = :blockerId")
    Set<Long> findBlockedIdsByBlockerId(@Param("blockerId") Long blockerId);
}
