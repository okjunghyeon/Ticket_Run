package com.whattoeat.domain.member.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.whattoeat.common.security.JwtProvider;
import com.whattoeat.domain.member.auth.model.dto.AuthTokens;
import com.whattoeat.domain.member.auth.repository.RefreshTokenRepository;
import com.whattoeat.domain.member.exception.MemberException;
import com.whattoeat.domain.member.exception.MemberExceptionCode;
import com.whattoeat.domain.member.model.dto.response.MemberResponse;
import com.whattoeat.domain.member.model.entity.Role;
import com.whattoeat.domain.member.model.entity.SocialProvider;
import com.whattoeat.domain.member.service.MemberService;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private AuthService authService;

    @DisplayName("로그인하면 Refresh 토큰을 발급해 회원의 유효 토큰으로 저장할 수 있다.")
    @Test
    void test1() {
        // given
        given(jwtProvider.createRefreshToken(MEMBER_ID)).willReturn("refresh-token");
        given(jwtProvider.getRefreshTokenExpiration()).willReturn(REFRESH_TOKEN_EXPIRATION);

        // when
        String result = authService.login(MEMBER_ID);

        // then
        assertThat(result).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(MEMBER_ID, "refresh-token", REFRESH_TOKEN_EXPIRATION);
    }

    @DisplayName("저장된 Refresh 토큰과 같으면 Access 토큰과 새 Refresh 토큰을 재발급할 수 있다.")
    @Test
    void test2() {
        // given
        MemberResponse member = new MemberResponse(MEMBER_ID, SocialProvider.KAKAO, null, "회원", null,
                Role.USER);

        given(jwtProvider.getMemberIdFromRefreshToken("old-refresh")).willReturn(MEMBER_ID);
        given(refreshTokenRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of("old-refresh"));
        given(memberService.getMember(MEMBER_ID)).willReturn(member);
        given(jwtProvider.createAccessToken(MEMBER_ID, "ROLE_USER")).willReturn("access-token");
        given(jwtProvider.createRefreshToken(MEMBER_ID)).willReturn("new-refresh");
        given(jwtProvider.getRefreshTokenExpiration()).willReturn(REFRESH_TOKEN_EXPIRATION);

        // when
        AuthTokens result = authService.reissue("old-refresh");

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRepository).save(MEMBER_ID, "new-refresh", REFRESH_TOKEN_EXPIRATION);
    }

    @DisplayName("Refresh 토큰 쿠키가 없으면 재발급할 수 없다.")
    @Test
    void test3() {
        // when & then
        assertThatThrownBy(() -> authService.reissue(null))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberExceptionCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @DisplayName("서명·만료 검증에 실패한 Refresh 토큰으로는 재발급할 수 없다.")
    @Test
    void test4() {
        // given
        given(jwtProvider.getMemberIdFromRefreshToken("expired-refresh")).willThrow(new JwtException("expired"));

        // when & then
        assertThatThrownBy(() -> authService.reissue("expired-refresh"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberExceptionCode.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository, never()).findByMemberId(anyLong());
    }

    @DisplayName("이미 회전된 Refresh 토큰이 다시 쓰이면 재발급할 수 없고 저장된 토큰도 삭제된다.")
    @Test
    void test5() {
        // given
        given(jwtProvider.getMemberIdFromRefreshToken("rotated-refresh")).willReturn(MEMBER_ID);
        given(refreshTokenRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of("current-refresh"));

        // when & then
        assertThatThrownBy(() -> authService.reissue("rotated-refresh"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberExceptionCode.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
        verify(refreshTokenRepository, never()).save(anyLong(), anyString(), any(Duration.class));
    }

    @DisplayName("로그아웃하면 저장된 Refresh 토큰을 삭제할 수 있다.")
    @Test
    void test6() {
        // given
        given(jwtProvider.getMemberIdFromRefreshToken("refresh-token")).willReturn(MEMBER_ID);
        given(refreshTokenRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of("refresh-token"));

        // when
        authService.logout("refresh-token");

        // then
        verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
    }

    @DisplayName("저장된 토큰과 다른 Refresh 토큰으로는 다른 기기의 로그인을 해제할 수 없다.")
    @Test
    void test7() {
        // given
        given(jwtProvider.getMemberIdFromRefreshToken("old-refresh")).willReturn(MEMBER_ID);
        given(refreshTokenRepository.findByMemberId(MEMBER_ID)).willReturn(Optional.of("current-refresh"));

        // when
        authService.logout("old-refresh");

        // then
        verify(refreshTokenRepository, never()).deleteByMemberId(anyLong());
    }
}
