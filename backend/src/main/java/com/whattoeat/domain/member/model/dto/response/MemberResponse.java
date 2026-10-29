package com.whattoeat.domain.member.model.dto.response;

import com.whattoeat.domain.member.model.entity.Member;
import com.whattoeat.domain.member.model.entity.Role;
import com.whattoeat.domain.member.model.entity.SocialProvider;

public record MemberResponse(
        Long memberId,
        SocialProvider provider,
        String email,
        String nickname,
        String profileImageUrl,
        Role role
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getRole());
    }
}
