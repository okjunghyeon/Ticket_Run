package com.whattoeat.domain.member.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 실패 시 브라우저가 백엔드 주소에 머물지 않도록 프론트엔드로 error 파라미터와 함께 보낸다.
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final String ERROR_PARAM = "error";
    private static final String ERROR_VALUE = "login_failed";

    private final String redirectUri;

    public OAuth2LoginFailureHandler(@Value("${auth.oauth2.redirect-uri}") String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("소셜 로그인에 실패했습니다: {}", exception.getMessage());
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam(ERROR_PARAM, ERROR_VALUE)
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}
