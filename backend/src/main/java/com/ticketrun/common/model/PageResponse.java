package com.ticketrun.common.model;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 목록 응답 공통 형식. Spring {@link Page}를 그대로 노출하지 않고 필요한 페이지 정보만 담는다.
 */
public record PageResponse<T>(
        T data,
        String message,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <E> PageResponse<List<E>> of(Page<E> page, String message) {
        return new PageResponse<>(
                page.getContent(),
                message,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
