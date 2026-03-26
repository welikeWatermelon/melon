package com.melonme.like.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeCreatedEvent {

    private final Long senderId;
    private final TargetType targetType;
    private final Long targetId;
    private final Long targetAuthorId;
}
