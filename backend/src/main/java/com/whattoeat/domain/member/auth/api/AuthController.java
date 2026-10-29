package com.whattoeat.domain.member.auth.api;

import com.whattoeat.common.model.BaseResponse;
import com.whattoeat.domain.member.auth.model.dto.AuthTokens;
import com.whattoeat.domain.member.auth.model.dto.response.TokenResponse;
import com.whattoeat.domain.member.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    /**
     * 로그인 직후와 새로고침·Access 토큰 만료 시 호출해 Access 토큰을 받는다.
     */
    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<TokenResponse> reissue(
            @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        AuthTokens tokens = authService.reissue(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(tokens.refreshToken()).toString());
        return new BaseResponse<>(new TokenResponse(tokens.accessToken()), "토큰을 재발급했습니다.");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString());
    }
}
