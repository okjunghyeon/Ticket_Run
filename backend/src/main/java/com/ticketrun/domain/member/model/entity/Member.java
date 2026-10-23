package com.ticketrun.domain.member.model.entity;

import com.ticketrun.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_provider_provider_id", columnNames = {"provider", "provider_id"})
})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    // 필수 동의여도 카카오 계정에 이메일이 없으면 응답에 빠질 수 있어 null 을 허용한다.
    @Column(name = "email")
    private String email;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Builder
    public Member(SocialProvider provider, String providerId, String email, String nickname,
                  String profileImageUrl) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        // TODO: 주최자 승격·관리자 지정 기능 추가 전까지 모든 신규 회원은 관객으로 가입한다.
        this.role = Role.AUDIENCE;
    }
}
