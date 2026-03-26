package com.melonme.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScrapResponse {
    @JsonProperty("isScrapped")
    private boolean isScrapped;
}
