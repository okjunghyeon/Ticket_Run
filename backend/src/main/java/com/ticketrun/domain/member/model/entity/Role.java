package com.ticketrun.domain.member.model.entity;

public enum Role {

    AUDIENCE,
    ORGANIZER,
    ADMIN;

    // Spring Security 권한 문자열. hasRole("ADMIN") 이 ROLE_ 접두사를 붙여 비교한다.
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
