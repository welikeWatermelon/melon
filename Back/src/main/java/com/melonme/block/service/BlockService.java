package com.melonme.block.service;

import com.melonme.block.domain.Block;
import com.melonme.block.dto.response.BlockListResponse;
import com.melonme.block.dto.response.BlockToggleResponse;
import com.melonme.block.dto.response.BlockedMemberResponse;
import com.melonme.block.exception.BlockSelfException;
import com.melonme.block.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;

    @Transactional
    public BlockToggleResponse toggleBlock(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BlockSelfException();
        }

        Optional<Block> existing = blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId);

        if (existing.isPresent()) {
            blockRepository.delete(existing.get());
            return new BlockToggleResponse(false);
        }

        Block block = Block.builder()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .build();
        blockRepository.save(block);
        return new BlockToggleResponse(true);
    }

    @Transactional(readOnly = true)
    public BlockListResponse getBlockedMembers(Long blockerId) {
        List<Block> blocks = blockRepository.findAllByBlockerId(blockerId);

        List<BlockedMemberResponse> blockedMembers = blocks.stream()
                .map(block -> new BlockedMemberResponse(block.getBlockedId(), null))
                .toList();

        return new BlockListResponse(blockedMembers);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }
}
