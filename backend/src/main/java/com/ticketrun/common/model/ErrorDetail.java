package com.ticketrun.common.model;

import java.util.List;

/**
 * 실패 응답의 data. code는 ErrorCode의 enum name, errors는 검증 실패 시 필드별 사유다.
 */
public record ErrorDetail(
        String code,
        List<FieldErrorDetail> errors
) {

    public static ErrorDetail of(String code) {
        return new ErrorDetail(code, List.of());
    }

    public record FieldErrorDetail(
            String field,
            String reason
    ) {
    }
}
