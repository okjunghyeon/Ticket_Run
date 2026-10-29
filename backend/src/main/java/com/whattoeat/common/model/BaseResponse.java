package com.whattoeat.common.model;

import org.springframework.http.HttpStatus;

/**
 * 단건 응답 공통 형식. 실패 응답은 {@link ErrorDetail}을 사용한다.
 * code에는 실제 HTTP 상태 코드가 들어가며, 응답 직전에 ResponseCodeAdvice가 채운다.
 */
public record BaseResponse<T>(
        int code,
        T data,
        String message
) {

    public BaseResponse(T data, String message) {
        this(HttpStatus.OK.value(), data, message);
    }

    // 데이터 없이 메시지만 내려주는 경우
    public BaseResponse(String message) {
        this(HttpStatus.OK.value(), null, message);
    }

    public BaseResponse<T> withCode(int code) {
        return new BaseResponse<>(code, data, message);
    }
}
