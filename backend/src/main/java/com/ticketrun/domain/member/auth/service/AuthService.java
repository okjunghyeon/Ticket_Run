package com.ticketrun.domain.member.auth.service;

import com.ticketrun.common.security.JwtProvider;
import com.ticketrun.domain.member.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 소셜 로그인 성공 시 Refresh 토큰을 발급하고 회원의 유효 토큰으로 저장한다.
     * 이전에 발급된 토큰은 덮어써져 더 이상 재발급에 쓸 수 없다.
     *
     * @param memberId 로그인한 회원 ID
     * @return 쿠키로 내려줄 Refresh 토큰
     */
    public String login(Long memberId) {
        return issueRefreshToken(memberId);
    }

    /* ===== private Method ===== */

    private String issueRefreshToken(Long memberId) {
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        refreshTokenRepository.save(memberId, refreshToken, jwtProvider.getRefreshTokenExpiration());
        return refreshToken;
    }
}
