package com.ticketrun.domain.member.auth.api;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

/**
 * Refresh 토큰 쿠키 생성 규칙. 로그인 성공 핸들러와 재발급·로그아웃 API가 같은 속성을 쓰도록 한 곳에 둔다.
 * JS 접근을 막고(HttpOnly), 재발급·로그아웃 요청에만 실리도록 경로를 /api/auth 로 제한한다.
 */
public final class RefreshTokenCookie {

    public static final String NAME = "refresh_token";

    private static final String PATH = "/api/auth";
    private static final String SAME_SITE = "Lax";

    private RefreshTokenCookie() {
    }

    public static ResponseCookie create(String refreshToken, Duration maxAge) {
        return baseCookie(refreshToken).maxAge(maxAge).build();
    }

    public static ResponseCookie expire() {
        return baseCookie("").maxAge(Duration.ZERO).build();
    }

    /* ===== private Method ===== */

    private static ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE)
                .path(PATH);
    }
}
