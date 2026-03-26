package com.melonme.block.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockedMemberResponse {

    private final Long id;
    private final String nickname;
}
