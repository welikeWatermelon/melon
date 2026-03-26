package com.melonme.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private boolean hasNext;
    private long totalCount;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.hasNext(),
                page.getTotalElements()
        );
    }

    public static <T> PageResponse<T> of(List<T> content, boolean hasNext, long totalCount) {
        return new PageResponse<>(content, hasNext, totalCount);
    }
}
