package com.whattoeat.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.whattoeat.domain.member.auth.model.dto.OAuth2UserInfo;
import com.whattoeat.domain.member.exception.MemberException;
import com.whattoeat.domain.member.exception.MemberExceptionCode;
import com.whattoeat.domain.member.model.dto.response.MemberResponse;
import com.whattoeat.domain.member.model.entity.Member;
import com.whattoeat.domain.member.model.entity.Role;
import com.whattoeat.domain.member.model.entity.SocialProvider;
import com.whattoeat.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("처음 로그인한 소셜 사용자는 관객으로 가입할 수 있다.")
    @Test
    void test1() {
        // given
        OAuth2UserInfo userInfo = new OAuth2UserInfo(SocialProvider.KAKAO, "12345", null, "관객",
                "http://k.kakaocdn.net/profile.jpg");
        Member savedMember = createMember(1L, userInfo);

        given(memberRepository.findByProviderAndProviderId(SocialProvider.KAKAO, "12345")).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // when
        MemberResponse result = memberService.findOrRegister(userInfo);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.AUDIENCE);
        assertThat(result.profileImageUrl()).isEqualTo("http://k.kakaocdn.net/profile.jpg");
        verify(memberRepository).save(any(Member.class));
    }

    @DisplayName("이미 가입한 소셜 사용자는 다시 가입하지 않고 기존 회원으로 로그인할 수 있다.")
    @Test
    void test2() {
        // given
        OAuth2UserInfo userInfo = new OAuth2UserInfo(SocialProvider.GOOGLE, "google-sub", "a@gmail.com", "관객",
                null);
        Member existingMember = createMember(2L, userInfo);

        given(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "google-sub"))
                .willReturn(Optional.of(existingMember));

        // when
        MemberResponse result = memberService.findOrRegister(userInfo);

        // then
        assertThat(result.memberId()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("a@gmail.com");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @DisplayName("존재하지 않는 회원은 조회할 수 없다.")
    @Test
    void test3() {
        // given
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMember(99L))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberExceptionCode.MEMBER_NOT_FOUND);
    }

    /* ===== private Method ===== */

    private Member createMember(Long id, OAuth2UserInfo userInfo) {
        Member member = Member.builder()
                .provider(userInfo.provider())
                .providerId(userInfo.providerId())
                .email(userInfo.email())
                .nickname(userInfo.nickname())
                .profileImageUrl(userInfo.profileImageUrl())
                .build();
        // id 는 DB가 채우는 값이라 setter 가 없으므로 테스트에서만 직접 주입한다.
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
