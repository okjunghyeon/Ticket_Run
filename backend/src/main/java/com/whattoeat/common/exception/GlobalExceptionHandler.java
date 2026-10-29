package com.whattoeat.common.exception;

import com.whattoeat.common.model.ErrorDetail;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorDetail> handleCustomException(CustomException e) {
        return toResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handleValidationException(MethodArgumentNotValidException e) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorDetail.of(errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage(),
                        e.getBindingResult()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetail> handleNotReadableException(HttpMessageNotReadableException e) {
        return toResponse(CommonErrorCode.INVALID_REQUEST_BODY);
    }

    // 시큐리티 필터의 EntryPoint/AccessDeniedHandler가 위임한 예외와, 컨트롤러 안(@PreAuthorize 등)에서 던져진 예외를 함께 받는다.
    // 아래 catch-all에 걸리면 500이 되므로 반드시 별도로 매핑한다.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetail> handleAuthenticationException(AuthenticationException e) {
        return toResponse(CommonErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetail> handleAccessDeniedException(AccessDeniedException e) {
        return toResponse(CommonErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleException(Exception e) {
        // 404, 405 등 Spring MVC 표준 예외는 ErrorResponse를 구현하므로 500으로 덮지 않고 원래 상태를 유지한다.
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(status.value());
            String errorCode = resolved != null ? resolved.name() : String.valueOf(status.value());
            return ResponseEntity.status(status)
                    .body(ErrorDetail.of(status.value(), errorCode, errorResponse.getBody().getDetail()));
        }

        log.error("처리되지 않은 예외가 발생했습니다.", e);
        return toResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /* ===== private Method ===== */

    private ResponseEntity<ErrorDetail> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorDetail.of(errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage()));
    }

    // 특정 도메인에 속하지 않는 공통 에러 코드. common이 도메인 코드를 알지 않도록 핸들러 내부에만 둔다.
    @Getter
    @RequiredArgsConstructor
    private enum CommonErrorCode implements ErrorCode {

        // ===== 400 Bad Request =====
        INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
        INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),

        // ===== 401 Unauthorized =====
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

        // ===== 403 Forbidden =====
        FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

        // ===== 500 Internal Server Error =====
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

        private final HttpStatus status;
        private final String message;
    }
}
