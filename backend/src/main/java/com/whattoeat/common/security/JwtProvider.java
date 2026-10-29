package com.whattoeat.common.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/**
 * Access/Refresh 토큰 발급과 검증을 담당한다.
 * 같은 키로 서명하므로 token_type 클레임으로 두 토큰을 구분해, Refresh 토큰을 Bearer 로 쓰는 것을 막는다.
 */
@Component
public class JwtProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;
    private final NimbusJwtDecoder accessTokenDecoder;
    private final NimbusJwtDecoder refreshTokenDecoder;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(jwtProperties.secret()), "HmacSHA256");
        this.jwtEncoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
        this.accessTokenDecoder = createDecoder(secretKey, ACCESS_TOKEN_TYPE);
        this.refreshTokenDecoder = createDecoder(secretKey, REFRESH_TOKEN_TYPE);
    }

    /**
     * @param memberId  토큰 subject 로 쓸 회원 ID
     * @param authority Spring Security 권한 문자열 (예: ROLE_USER)
     */
    public String createAccessToken(Long memberId, String authority) {
        JwtClaimsSet claims = baseClaims(memberId, ACCESS_TOKEN_TYPE, jwtProperties.accessTokenExpiration())
                .claim(ROLE_CLAIM, authority)
                .build();
        return encode(claims);
    }

    // jti 를 넣어 같은 시각에 재발급해도 이전 토큰과 값이 달라지게 한다 (RTR 재사용 감지에 필요).
    public String createRefreshToken(Long memberId) {
        JwtClaimsSet claims = baseClaims(memberId, REFRESH_TOKEN_TYPE, jwtProperties.refreshTokenExpiration())
                .id(UUID.randomUUID().toString())
                .build();
        return encode(claims);
    }

    /**
     * Resource Server 의 Bearer 토큰 검증에 사용한다.
     *
     * @throws JwtException 서명·만료·토큰 종류가 올바르지 않은 경우
     */
    public Jwt decodeAccessToken(String token) {
        return accessTokenDecoder.decode(token);
    }

    /**
     * @return Refresh 토큰의 회원 ID
     * @throws JwtException 서명·만료·토큰 종류가 올바르지 않은 경우
     */
    public Long getMemberIdFromRefreshToken(String token) {
        return Long.valueOf(refreshTokenDecoder.decode(token).getSubject());
    }

    // 검증된 Access 토큰을 인증 객체로 바꾼다. principal 을 회원 ID(Long)로 두어 컨트롤러가 @AuthenticationPrincipal Long 으로 받는다.
    public AbstractAuthenticationToken toAuthentication(Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(jwt.getClaimAsString(ROLE_CLAIM));
        return UsernamePasswordAuthenticationToken.authenticated(memberId, null, List.of(authority));
    }

    public Duration getRefreshTokenExpiration() {
        return jwtProperties.refreshTokenExpiration();
    }

    /* ===== private Method ===== */

    private JwtClaimsSet.Builder baseClaims(Long memberId, String tokenType, Duration expiration) {
        Instant now = Instant.now();
        return JwtClaimsSet.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .claim(TOKEN_TYPE_CLAIM, tokenType);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private NimbusJwtDecoder createDecoder(SecretKey secretKey, String tokenType) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                new JwtClaimValidator<String>(TOKEN_TYPE_CLAIM, tokenType::equals)));
        return decoder;
    }
}
