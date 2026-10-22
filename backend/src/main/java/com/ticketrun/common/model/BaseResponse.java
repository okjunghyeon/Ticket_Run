package com.ticketrun.common.model;

/**
 * 단건 응답 공통 형식. 실패 응답은 {@link ErrorDetail}을 사용한다.
 */
public record BaseResponse<T>(
        T data,
        String message
) {

    // 데이터 없이 메시지만 내려주는 경우
    public BaseResponse(String message) {
        this(null, message);
    }
}
