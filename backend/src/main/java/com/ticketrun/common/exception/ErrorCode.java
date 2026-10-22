package com.ticketrun.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인별 {Domain}ExceptionCode enum이 구현하는 에러 코드 규약.
 * common은 이 인터페이스만 알고, 구체적인 도메인 코드는 알지 못한다.
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
