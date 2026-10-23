package com.ticketrun.domain.member.auth.service;

import com.ticketrun.domain.member.auth.model.dto.OAuth2LoginMember;
import com.ticketrun.domain.member.auth.model.dto.OAuth2UserInfo;
import com.ticketrun.domain.member.model.dto.response.MemberResponse;
import com.ticketrun.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    // 제공자 사용자 정보 조회(외부 HTTP)는 트랜잭션 밖에서 하고, 가입 처리만 MemberService 트랜잭션에서 한다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oAuth2User.getAttributes());
        MemberResponse member = memberService.findOrRegister(userInfo);

        return OAuth2LoginMember.ofOAuth2(member.memberId(), member.role(), oAuth2User.getAttributes());
    }
}
