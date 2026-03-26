package com.melonme.block.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BlockListResponse {

    private final List<BlockedMemberResponse> blockedMembers;
}
