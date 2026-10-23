package com.ticketrun.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 서명 키와 만료 시간. secret 은 application-secret.yml 에서 Base64 문자열로 주입한다.
 */
@ConfigurationProperties("auth.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}
