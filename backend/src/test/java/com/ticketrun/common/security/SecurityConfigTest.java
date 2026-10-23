package com.ticketrun.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketrun.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class,
        SecurityConfigTest.TestController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

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

    @DisplayName("인증 요청에도 세션을 생성하지 않는다.")
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

    @RestController
    static class TestController {

        @GetMapping("/test/secured")
        void secured() {
        }
    }
}
