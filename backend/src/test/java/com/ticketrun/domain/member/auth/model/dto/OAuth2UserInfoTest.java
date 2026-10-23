package com.ticketrun.domain.member.auth.model.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketrun.domain.member.model.entity.SocialProvider;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

class OAuth2UserInfoTest {

    @DisplayName("카카오 사용자 정보에서 제공자 ID, 이메일, 닉네임을 꺼낼 수 있다.")
    @Test
    void test1() {
        // given
        Map<String, Object> attributes = Map.of(
                "id", 12345L,
                "kakao_account", Map.of(
                        "email", "kakao@kakao.com",
                        "profile", Map.of("nickname", "카카오관객")));

        // when
        OAuth2UserInfo result = OAuth2UserInfo.of("kakao", attributes);

        // then
        assertThat(result.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.providerId()).isEqualTo("12345");
        assertThat(result.email()).isEqualTo("kakao@kakao.com");
        assertThat(result.nickname()).isEqualTo("카카오관객");
    }

    @DisplayName("카카오 이메일 동의가 없어도 사용자 정보를 만들 수 있다.")
    @Test
    void test2() {
        // given
        Map<String, Object> attributes = Map.of("id", 12345L);

        // when
        OAuth2UserInfo result = OAuth2UserInfo.of("kakao", attributes);

        // then
        assertThat(result.email()).isNull();
        assertThat(result.nickname()).isEqualTo("회원");
    }

    @DisplayName("구글 사용자 정보에서 제공자 ID, 이메일, 닉네임을 꺼낼 수 있다.")
    @Test
    void test3() {
        // given
        Map<String, Object> attributes = Map.of(
                "sub", "google-sub",
                "email", "google@gmail.com",
                "name", "구글관객");

        // when
        OAuth2UserInfo result = OAuth2UserInfo.of("google", attributes);

        // then
        assertThat(result.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(result.providerId()).isEqualTo("google-sub");
        assertThat(result.email()).isEqualTo("google@gmail.com");
        assertThat(result.nickname()).isEqualTo("구글관객");
    }

    @DisplayName("지원하지 않는 제공자의 사용자 정보는 만들 수 없다.")
    @Test
    void test4() {
        // when & then
        assertThatThrownBy(() -> OAuth2UserInfo.of("naver", Map.of()))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
