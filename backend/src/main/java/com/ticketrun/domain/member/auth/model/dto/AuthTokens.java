package com.ticketrun.domain.member.auth.model.dto;

/**
 * 재발급 결과. Access 토큰은 응답 본문으로, Refresh 토큰은 쿠키로 나눠 내려준다.
 */
public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
