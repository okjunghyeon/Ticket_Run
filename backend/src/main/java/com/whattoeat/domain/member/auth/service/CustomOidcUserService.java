package com.whattoeat.domain.member.auth.service;

import com.whattoeat.domain.member.auth.model.dto.OAuth2LoginMember;
import com.whattoeat.domain.member.auth.model.dto.OAuth2UserInfo;
import com.whattoeat.domain.member.model.dto.response.MemberResponse;
import com.whattoeat.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * scope 에 openid 가 있는 제공자(구글)의 로그인 처리. ID 토큰 검증은 상위 OidcUserService 가 담당한다.
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberService memberService;

    // 제공자 사용자 정보 조회(외부 HTTP)는 트랜잭션 밖에서 하고, 가입 처리만 MemberService 트랜잭션에서 한다.
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oidcUser.getAttributes());
        MemberResponse member = memberService.findOrRegister(userInfo);

        return OAuth2LoginMember.ofOidc(member.memberId(), member.role(), oidcUser);
    }
}
