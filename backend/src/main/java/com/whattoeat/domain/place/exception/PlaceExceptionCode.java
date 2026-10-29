package com.whattoeat.domain.place.exception;

import com.whattoeat.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceExceptionCode implements ErrorCode {

    // ===== 400 Bad Request =====
    SEARCH_PAGE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "검색 결과는 최대 45개까지 조회할 수 있습니다."),

    // ===== 404 Not Found =====
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 맛집입니다."),

    // ===== 503 Service Unavailable =====
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "장소 검색 서비스와의 통신 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
