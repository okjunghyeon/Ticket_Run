package com.ticketrun.domain.member.auth.repository;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 회원별로 현재 유효한 Refresh 토큰 하나만 Redis 에 보관한다 (화이트리스트).
 * 저장값과 다른 토큰은 이미 회전됐거나 위조된 것이므로 거부한다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_FORMAT = "member:%d:refresh-token";

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(memberId), refreshToken, ttl);
    }

    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId)));
    }

    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    /* ===== private Method ===== */

    private String key(Long memberId) {
        return KEY_FORMAT.formatted(memberId);
    }
}
