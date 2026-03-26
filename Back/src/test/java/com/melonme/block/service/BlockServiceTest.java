package com.melonme.block.service;

import com.melonme.block.domain.Block;
import com.melonme.block.dto.response.BlockListResponse;
import com.melonme.block.dto.response.BlockToggleResponse;
import com.melonme.block.exception.BlockSelfException;
import com.melonme.block.repository.BlockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @InjectMocks
    private BlockService blockService;

    @Mock
    private BlockRepository blockRepository;

    @Test
    @DisplayName("본인을 차단하면 BlockSelfException 발생")
    void toggleBlock_self_throwsException() {
        // given
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> blockService.toggleBlock(memberId, memberId))
                .isInstanceOf(BlockSelfException.class);

        verify(blockRepository, never()).save(any());
    }

    @Test
    @DisplayName("차단이 없으면 새로 차단한다")
    void toggleBlock_notBlocked_createsBlock() {
        // given
        Long blockerId = 1L;
        Long blockedId = 2L;
        given(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId))
                .willReturn(Optional.empty());
        given(blockRepository.save(any(Block.class)))
                .willReturn(Block.builder().blockerId(blockerId).blockedId(blockedId).build());

        // when
        BlockToggleResponse response = blockService.toggleBlock(blockerId, blockedId);

        // then
        assertThat(response.isBlocked()).isTrue();
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    @DisplayName("이미 차단이면 해제한다")
    void toggleBlock_alreadyBlocked_deletesBlock() {
        // given
        Long blockerId = 1L;
        Long blockedId = 2L;
        Block existingBlock = Block.builder().blockerId(blockerId).blockedId(blockedId).build();
        given(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId))
                .willReturn(Optional.of(existingBlock));

        // when
        BlockToggleResponse response = blockService.toggleBlock(blockerId, blockedId);

        // then
        assertThat(response.isBlocked()).isFalse();
        verify(blockRepository).delete(existingBlock);
        verify(blockRepository, never()).save(any());
    }

    @Test
    @DisplayName("차단 목록을 조회한다")
    void getBlockedMembers_returnsList() {
        // given
        Long blockerId = 1L;
        List<Block> blocks = List.of(
                Block.builder().blockerId(blockerId).blockedId(2L).build(),
                Block.builder().blockerId(blockerId).blockedId(3L).build()
        );
        given(blockRepository.findAllByBlockerId(blockerId)).willReturn(blocks);

        // when
        BlockListResponse response = blockService.getBlockedMembers(blockerId);

        // then
        assertThat(response.getBlockedMembers()).hasSize(2);
        assertThat(response.getBlockedMembers().get(0).getId()).isEqualTo(2L);
        assertThat(response.getBlockedMembers().get(1).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("차단 목록이 비어있으면 빈 리스트를 반환한다")
    void getBlockedMembers_empty_returnsEmptyList() {
        // given
        Long blockerId = 1L;
        given(blockRepository.findAllByBlockerId(blockerId)).willReturn(List.of());

        // when
        BlockListResponse response = blockService.getBlockedMembers(blockerId);

        // then
        assertThat(response.getBlockedMembers()).isEmpty();
    }
}
