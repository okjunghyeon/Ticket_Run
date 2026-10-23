package com.ticketrun.domain.member.auth.model.dto;

import com.ticketrun.domain.member.model.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 소셜 로그인 직후 성공 핸들러까지 회원 ID를 전달하는 principal.
 */
public record OAuth2LoginMember(
        Long memberId,
        Role role,
        Map<String, Object> attributes
) implements OAuth2User {

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
