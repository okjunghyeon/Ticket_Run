package com.ticketrun.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @SpringBootApplication 에 두면 JPA 빈이 없는 @WebMvcTest 슬라이스 테스트가 실패하므로 별도 설정으로 분리한다.
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
