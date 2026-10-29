package com.whattoeat.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인별 {Domain}ExceptionCode enum이 구현하는 에러 코드 규약.
 */
public interface ErrorCode {

    HttpStatus getStatus();

    String getMessage();

    String name();

    // 응답에 내려가는 문자열 코드. 같은 HTTP 상태 안에서도 클라이언트가 원인을 구분할 수 있게 enum name을 쓴다.
    default String getCode() {
        return name();
    }
}
