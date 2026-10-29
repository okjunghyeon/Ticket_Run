package com.whattoeat.domain.member.auth.api;

import com.whattoeat.common.security.JwtProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh 토큰 쿠키 생성 규칙. 로그인 성공 핸들러와 재발급·로그아웃 API가 같은 속성을 쓰도록 한 곳에 둔다.
 * JS 접근을 막고(HttpOnly), 재발급·로그아웃 요청에만 실리도록 경로를 /api/auth 로 제한한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refresh_token";

    private static final String PATH = "/api/auth";
    private static final String SAME_SITE = "Lax";

    private final JwtProperties jwtProperties;

    // 쿠키 만료를 토큰 만료와 맞춰, 만료된 토큰이 브라우저에 남지 않게 한다.
    public ResponseCookie create(String refreshToken) {
        return baseCookie(refreshToken).maxAge(jwtProperties.refreshTokenExpiration()).build();
    }

    public ResponseCookie expire() {
        return baseCookie("").maxAge(Duration.ZERO).build();
    }

    /* ===== private Method ===== */

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(SAME_SITE)
                .path(PATH);
    }
}
