package com.whattoeat.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * dev 전용 H2 콘솔 접근 설정.
 * 메인 SecurityFilterChain 의 규칙(JWT, frame 차단)을 건드리지 않도록 /h2-console 경로만 담당하는 체인을 먼저 적용한다.
 */
@Configuration
@Profile("dev")
public class H2ConsoleSecurityConfig {

    private static final String H2_CONSOLE_URLS = "/h2-console/**";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(H2_CONSOLE_URLS)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 콘솔 화면이 form 제출과 같은 출처 iframe 으로 동작하므로 CSRF 를 끄고 frame 을 허용한다.
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .build();
    }
}
