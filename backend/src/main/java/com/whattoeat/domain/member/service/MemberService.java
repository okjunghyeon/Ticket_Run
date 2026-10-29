package com.whattoeat.domain.member.service;

import static com.whattoeat.domain.member.exception.MemberExceptionCode.MEMBER_NOT_FOUND;

import com.whattoeat.domain.member.auth.model.dto.OAuth2UserInfo;
import com.whattoeat.domain.member.exception.MemberException;
import com.whattoeat.domain.member.model.dto.response.MemberResponse;
import com.whattoeat.domain.member.model.entity.Member;
import com.whattoeat.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 소셜 로그인 사용자를 조회하고, 처음 로그인한 경우 일반 회원(USER)으로 가입시킨다.
     *
     * @param userInfo 소셜 제공자에게 받은 사용자 정보
     * @return 로그인한 회원 정보
     */
    @Transactional
    public MemberResponse findOrRegister(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .provider(userInfo.provider())
                        .providerId(userInfo.providerId())
                        .email(userInfo.email())
                        .nickname(userInfo.nickname())
                        .profileImageUrl(userInfo.profileImageUrl())
                        .build()));
        return MemberResponse.from(member);
    }

    /**
     * @param memberId 조회할 회원 ID
     * @return 회원 정보
     * @throws MemberException 회원이 존재하지 않는 경우 (MEMBER_NOT_FOUND)
     */
    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::from)
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }
}
