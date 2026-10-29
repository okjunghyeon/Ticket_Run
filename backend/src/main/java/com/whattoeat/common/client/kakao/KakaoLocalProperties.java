package com.whattoeat.common.client.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오 로컬 API 접속 정보. restApiKey 는 application-secret.yml 에서 주입한다.
 */
@ConfigurationProperties("kakao.local")
public record KakaoLocalProperties(
        String baseUrl,
        String restApiKey
) {
}
