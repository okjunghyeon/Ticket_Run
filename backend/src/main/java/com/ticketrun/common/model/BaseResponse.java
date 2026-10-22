package com.ticketrun.common.model;

/**
 * 단건 응답 공통 형식. 실패 응답은 {@link ErrorDetail}을 사용한다.
 */
public record BaseResponse<T>(
        String code,
        T data,
        String message
) {

    // 실패 응답(ErrorDetail)과 같이 code를 먼저 내려 클라이언트가 결과를 같은 방식으로 판별할 수 있게 한다.
    public static final String SUCCESS_CODE = "SUCCESS";

    public BaseResponse(T data, String message) {
        this(SUCCESS_CODE, data, message);
    }

    // 데이터 없이 메시지만 내려주는 경우
    public BaseResponse(String message) {
        this(SUCCESS_CODE, null, message);
    }
}
