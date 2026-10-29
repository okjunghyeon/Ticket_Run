package com.whattoeat.domain.member.auth.model.dto;

import com.whattoeat.domain.member.model.entity.SocialProvider;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * 제공자마다 다른 사용자 정보 응답 형식을 회원 가입에 필요한 값으로 통일한다.
 */
public record OAuth2UserInfo(
        SocialProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) {

    private static final String UNSUPPORTED_PROVIDER = "unsupported_provider";
    private static final String DEFAULT_NICKNAME = "회원";

    public OAuth2UserInfo {
        if (nickname == null || nickname.isBlank()) {
            nickname = DEFAULT_NICKNAME;
        }
    }

    /**
     * @param registrationId application.yml 의 registration 이름 (kakao, google)
     * @param attributes     제공자 사용자 정보 응답 (구글은 OIDC 클레임)
     * @throws OAuth2AuthenticationException 지원하지 않는 제공자인 경우
     */
    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> ofKakao(attributes);
            case "google" -> ofGoogle(attributes);
            default -> throw new OAuth2AuthenticationException(new OAuth2Error(UNSUPPORTED_PROVIDER));
        };
    }

    /* ===== private Method ===== */

    // { "id": 123, "kakao_account": { "email": "...", "profile": { "nickname": "...", "profile_image_url": "..." } } }
    @SuppressWarnings("unchecked")
    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
        return new OAuth2UserInfo(
                SocialProvider.KAKAO,
                String.valueOf(attributes.get("id")),
                (String) account.get("email"),
                (String) profile.get("nickname"),
                (String) profile.get("profile_image_url"));
    }

    // { "sub": "...", "email": "...", "name": "...", "picture": "..." }
    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                SocialProvider.GOOGLE,
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture"));
    }
}
