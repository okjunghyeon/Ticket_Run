package com.ticketrun.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketrun.common.config.SecurityConfig;
import com.ticketrun.domain.member.auth.handler.OAuth2LoginFailureHandler;
import com.ticketrun.domain.member.auth.handler.OAuth2LoginSuccessHandler;
import com.ticketrun.domain.member.auth.service.CustomOAuth2UserService;
import com.ticketrun.domain.member.auth.service.CustomOidcUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@ActiveProfiles("test")
@EnableConfigurationProperties(JwtProperties.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class,
        JwtProvider.class, SecurityConfigTest.TestController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @DisplayName("인증 없이 보호된 경로를 요청하면 GlobalExceptionHandler를 거쳐 401과 UNAUTHORIZED로 응답할 수 있다.")
    @Test
    void test1() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/secured"));

        // then
        result.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @DisplayName("인증된 사용자는 보호된 경로에 접근할 수 있다.")
    @WithMockUser
    @Test
    void test2() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/secured"));

        // then
        result.andExpect(status().isOk());
    }

    @DisplayName("인증 요청에도 세션이 생성되지 않는다.")
    @WithMockUser
    @Test
    void test3() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/secured"));

        // then
        result.andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("SPRING_SECURITY_CONTEXT"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @DisplayName("유효한 Access 토큰으로 요청하면 컨트롤러가 @AuthenticationPrincipal 로 회원 ID를 받을 수 있다.")
    @Test
    void test4() throws Exception {
        // given
        String accessToken = jwtProvider.createAccessToken(1L, "ROLE_AUDIENCE");

        // when
        ResultActions result = mockMvc.perform(get("/test/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @DisplayName("위조된 Access 토큰으로 요청하면 401과 UNAUTHORIZED로 응답할 수 있다.")
    @Test
    void test5() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/secured")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"));

        // then
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @DisplayName("Refresh 토큰은 Access 토큰으로 사용할 수 없다.")
    @Test
    void test6() throws Exception {
        // given
        String refreshToken = jwtProvider.createRefreshToken(1L);

        // when
        ResultActions result = mockMvc.perform(get("/test/secured")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @DisplayName("관객은 관리자 경로에 접근할 수 없다.")
    @Test
    void test7() throws Exception {
        // given
        String accessToken = jwtProvider.createAccessToken(1L, "ROLE_AUDIENCE");

        // when
        ResultActions result = mockMvc.perform(get("/api/admin/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @DisplayName("관리자는 권한 계층에 따라 주최자 경로에 접근할 수 있다.")
    @Test
    void test8() throws Exception {
        // given
        String accessToken = jwtProvider.createAccessToken(1L, "ROLE_ADMIN");

        // when
        ResultActions result = mockMvc.perform(get("/api/organizer/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @DisplayName("인증 API는 유효하지 않은 Bearer 헤더가 함께 와도 401로 막히지 않는다.")
    @Test
    void test9() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/auth/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer expired.token.value"));

        // then
        result.andExpect(status().isOk());
    }

    @RestController
    static class TestController {

        @GetMapping("/test/secured")
        void secured() {
        }

        @GetMapping("/test/me")
        Long me(@AuthenticationPrincipal Long memberId) {
            return memberId;
        }

        @GetMapping("/api/admin/test")
        void admin() {
        }

        @GetMapping("/api/organizer/test")
        void organizer() {
        }

        @GetMapping("/api/auth/test")
        void auth() {
        }
    }
}
