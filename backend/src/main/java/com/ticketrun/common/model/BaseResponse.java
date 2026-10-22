package com.ticketrun.common.model;

/**
 * 단건 응답 공통 형식. 실패 응답도 data에 {@link ErrorDetail}을 담아 같은 형식으로 내려준다.
 */
public record BaseResponse<T>(
        T data,
        String message
) {
}
