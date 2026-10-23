package com.ticketrun.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.json.JsonMapper;

class CustomAccessDeniedHandlerTest {

    // 현재 인가 규칙(authenticated())만으로는 403이 나는 경로가 없어 핸들러를 직접 호출해 검증한다.
    private final CustomAccessDeniedHandler accessDeniedHandler =
            new CustomAccessDeniedHandler(new SecurityResponseWriter(new JsonMapper()));

    @DisplayName("인가에 실패하면 403과 FORBIDDEN으로 응답할 수 있다.")
    @Test
    void test1() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        accessDeniedHandler.handle(request, response, new AccessDeniedException("denied"));

        // then
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString())
                .contains("\"code\":403")
                .contains("\"errorCode\":\"FORBIDDEN\"")
                .contains("\"message\":\"접근 권한이 없습니다.\"")
                .doesNotContain("\"errors\"");
    }
}
