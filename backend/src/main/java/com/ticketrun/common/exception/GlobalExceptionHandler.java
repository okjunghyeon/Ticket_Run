package com.ticketrun.common.exception;

import com.ticketrun.common.model.BaseResponse;
import com.ticketrun.common.model.ErrorDetail;
import com.ticketrun.common.model.ErrorDetail.FieldErrorDetail;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<ErrorDetail>> handleCustomException(CustomException e) {
        return toResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorDetail>> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldErrorDetail> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus())
                .body(new BaseResponse<>(new ErrorDetail(errorCode.getCode(), errors), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<ErrorDetail>> handleNotReadableException(HttpMessageNotReadableException e) {
        return toResponse(CommonErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorDetail>> handleException(Exception e) {
        // 404, 405 등 Spring MVC 표준 예외는 ErrorResponse를 구현하므로 500으로 덮지 않고 원래 상태를 유지한다.
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(status.value());
            String code = resolved != null ? resolved.name() : String.valueOf(status.value());
            return ResponseEntity.status(status)
                    .body(new BaseResponse<>(ErrorDetail.of(code), errorResponse.getBody().getDetail()));
        }

        log.error("처리되지 않은 예외가 발생했습니다.", e);
        return toResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /* ===== private Method ===== */

    private ResponseEntity<BaseResponse<ErrorDetail>> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new BaseResponse<>(ErrorDetail.of(errorCode.getCode()), errorCode.getMessage()));
    }

    // 특정 도메인에 속하지 않는 공통 에러 코드. common이 도메인 코드를 알지 않도록 핸들러 내부에만 둔다.
    @Getter
    @RequiredArgsConstructor
    private enum CommonErrorCode implements ErrorCode {

        // ===== 400 Bad Request =====
        INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
        INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),

        // ===== 500 Internal Server Error =====
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

        private final HttpStatus status;
        private final String message;
    }
}
