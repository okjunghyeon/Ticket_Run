package com.ticketrun.domain.member.model.dto.response;

import com.ticketrun.domain.member.model.entity.Member;
import com.ticketrun.domain.member.model.entity.Role;
import com.ticketrun.domain.member.model.entity.SocialProvider;

public record MemberResponse(
        Long memberId,
        SocialProvider provider,
        String email,
        String nickname,
        Role role
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getNickname(),
                member.getRole());
    }
}
