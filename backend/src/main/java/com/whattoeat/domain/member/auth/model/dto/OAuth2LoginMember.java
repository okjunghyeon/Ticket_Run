package com.whattoeat.domain.member.auth.model.dto;

import com.whattoeat.domain.member.model.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * 소셜 로그인 직후 성공 핸들러까지 회원 ID를 전달하는 principal.
 * OIDC(구글)도 같은 성공 핸들러를 타도록 OidcUser 를 구현하며, OAuth2(카카오)에서는 idToken·userInfo 가 null 이다.
 */
public record OAuth2LoginMember(
        Long memberId,
        Role role,
        Map<String, Object> attributes,
        OidcIdToken idToken,
        OidcUserInfo userInfo
) implements OidcUser {

    public static OAuth2LoginMember ofOAuth2(Long memberId, Role role, Map<String, Object> attributes) {
        return new OAuth2LoginMember(memberId, role, attributes, null, null);
    }

    public static OAuth2LoginMember ofOidc(Long memberId, Role role, OidcUser oidcUser) {
        return new OAuth2LoginMember(memberId, role, oidcUser.getAttributes(), oidcUser.getIdToken(),
                oidcUser.getUserInfo());
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
