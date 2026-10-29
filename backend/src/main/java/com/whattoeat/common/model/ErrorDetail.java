package com.whattoeat.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.validation.BindingResult;

/**
 * 실패 응답 공통 형식. code는 HTTP 상태 코드, errorCode는 ErrorCode의 enum name, errors는 검증 실패 시에만 내려준다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        int code,
        String errorCode,
        String message,
        List<FieldErrorDetail> errors
) {

    public static ErrorDetail of(int code, String errorCode, String message) {
        return new ErrorDetail(code, errorCode, message, null);
    }

    public static ErrorDetail of(int code, String errorCode, String message, BindingResult bindingResult) {
        return new ErrorDetail(code, errorCode, message, FieldErrorDetail.of(bindingResult));
    }

    public record FieldErrorDetail(
            String field,
            String rejectedValue,
            String reason
    ) {

        public static List<FieldErrorDetail> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldErrorDetail(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }
    }
}
