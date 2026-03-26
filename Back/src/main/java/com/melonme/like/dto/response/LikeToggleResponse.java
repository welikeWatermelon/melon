package com.melonme.like.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeToggleResponse {

    @JsonProperty("isLiked")
    private final boolean isLiked;
    private final long likeCount;
}
