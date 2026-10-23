package com.ticketrun.domain.member.auth.handler;

import com.ticketrun.common.security.JwtProvider;
import com.ticketrun.domain.member.auth.api.RefreshTokenCookie;
import com.ticketrun.domain.member.auth.model.dto.OAuth2LoginMember;
import com.ticketrun.domain.member.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 성공 시 Refresh 토큰만 쿠키로 심고 프론트엔드로 보낸다.
 * Access 토큰은 URL 에 노출하지 않고, 프론트엔드가 곧바로 /api/auth/reissue 를 호출해 받는다.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final String redirectUri;

    public OAuth2LoginSuccessHandler(AuthService authService, JwtProvider jwtProvider,
                                     @Value("${auth.oauth2.redirect-uri}") String redirectUri) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2LoginMember loginMember = (OAuth2LoginMember) authentication.getPrincipal();
        String refreshToken = authService.login(loginMember.memberId());

        response.addHeader(HttpHeaders.SET_COOKIE,
                RefreshTokenCookie.create(refreshToken, jwtProvider.getRefreshTokenExpiration()).toString());
        response.sendRedirect(redirectUri);
    }
}
