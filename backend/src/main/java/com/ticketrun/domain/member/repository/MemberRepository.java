package com.ticketrun.domain.member.repository;

import com.ticketrun.domain.member.model.entity.Member;
import com.ticketrun.domain.member.model.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
