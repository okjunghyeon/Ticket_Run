package com.ticketrun.common.model;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

/**
 * 목록 응답 공통 형식. Spring {@link Page}를 그대로 노출하지 않고 필요한 페이지 정보만 담는다.
 * code에는 실제 HTTP 상태 코드가 들어가며, 응답 직전에 ResponseCodeAdvice가 채운다.
 */
public record PageResponse<T>(
        int code,
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
                HttpStatus.OK.value(),
                page.getContent(),
                message,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    public PageResponse<T> withCode(int code) {
        return new PageResponse<>(code, data, message, page, size, totalElements, totalPages, hasNext);
    }
}
