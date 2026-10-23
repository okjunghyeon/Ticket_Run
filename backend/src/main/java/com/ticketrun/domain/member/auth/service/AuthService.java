package com.ticketrun.domain.member.auth.service;

import static com.ticketrun.domain.member.exception.MemberExceptionCode.INVALID_REFRESH_TOKEN;
import static com.ticketrun.domain.member.exception.MemberExceptionCode.REFRESH_TOKEN_NOT_FOUND;

import com.ticketrun.common.security.JwtProvider;
import com.ticketrun.domain.member.auth.model.dto.AuthTokens;
import com.ticketrun.domain.member.auth.repository.RefreshTokenRepository;
import com.ticketrun.domain.member.exception.MemberException;
import com.ticketrun.domain.member.model.dto.response.MemberResponse;
import com.ticketrun.domain.member.service.MemberService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;

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

    /**
     * Refresh 토큰을 검증해 Access 토큰과 새 Refresh 토큰을 발급한다 (Refresh Token Rotation).
     *
     * @param refreshToken 쿠키로 받은 Refresh 토큰
     * @return 새 Access/Refresh 토큰
     * @throws MemberException 토큰이 없으면 REFRESH_TOKEN_NOT_FOUND,
     *                         서명·만료가 잘못됐거나 서버에 저장된 토큰과 다르면 INVALID_REFRESH_TOKEN
     */
    public AuthTokens reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new MemberException(REFRESH_TOKEN_NOT_FOUND);
        }

        Long memberId = parseMemberId(refreshToken)
                .orElseThrow(() -> new MemberException(INVALID_REFRESH_TOKEN));
        String savedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(INVALID_REFRESH_TOKEN));

        // 서명은 맞지만 저장값과 다르면 이미 회전된 토큰이 다시 쓰인 것이다.
        // 탈취 가능성이 있으므로 현재 토큰까지 지워 정상 사용자도 다시 로그인하게 한다.
        if (!savedToken.equals(refreshToken)) {
            refreshTokenRepository.deleteByMemberId(memberId);
            throw new MemberException(INVALID_REFRESH_TOKEN);
        }

        MemberResponse member = memberService.getMember(memberId);
        String accessToken = jwtProvider.createAccessToken(memberId, member.role().getAuthority());
        return new AuthTokens(accessToken, issueRefreshToken(memberId));
    }

    /**
     * 서버에 저장된 Refresh 토큰을 지워 더 이상 재발급할 수 없게 한다.
     * 토큰이 없거나 유효하지 않아도 쿠키는 지워야 하므로 예외를 던지지 않는다.
     *
     * @param refreshToken 쿠키로 받은 Refresh 토큰 (없을 수 있음)
     */
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        parseMemberId(refreshToken).ifPresent(memberId -> refreshTokenRepository.findByMemberId(memberId)
                .filter(refreshToken::equals)
                .ifPresent(savedToken -> refreshTokenRepository.deleteByMemberId(memberId)));
    }

    /* ===== private Method ===== */

    private String issueRefreshToken(Long memberId) {
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        refreshTokenRepository.save(memberId, refreshToken, jwtProvider.getRefreshTokenExpiration());
        return refreshToken;
    }

    private Optional<Long> parseMemberId(String refreshToken) {
        try {
            return Optional.of(jwtProvider.getMemberIdFromRefreshToken(refreshToken));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
