package com.melonme.block.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlockToggleResponse {

    @JsonProperty("isBlocked")
    private final boolean isBlocked;
}
