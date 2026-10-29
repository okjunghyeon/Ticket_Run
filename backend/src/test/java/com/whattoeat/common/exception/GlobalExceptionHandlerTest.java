package com.whattoeat.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 시큐리티 필터 없이 핸들러의 응답 형식만 검증하기 위해 standalone으로 구성한다.
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("CustomException은 ErrorCode의 상태, 코드, 메시지로 응답할 수 있다.")
    @Test
    void test1() throws Exception {
        // when & then
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.errorCode").value("TEST_NOT_FOUND"))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.message").value("테스트 리소스를 찾을 수 없습니다."));
    }

    @DisplayName("@Valid 검증에 실패하면 400과 필드별 사유로 응답할 수 있다.")
    @Test
    void test2() throws Exception {
        // given
        String body = """
                {"name": ""}
                """;

        // when & then
        mockMvc.perform(post("/test/valid").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value(""))
                .andExpect(jsonPath("$.errors[0].reason").value("이름은 필수입니다."))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."));
    }

    @DisplayName("요청 본문을 읽을 수 없으면 400으로 응답할 수 있다.")
    @Test
    void test3() throws Exception {
        // when & then
        mockMvc.perform(post("/test/valid").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_BODY"));
    }

    @DisplayName("Spring MVC 표준 예외는 500으로 덮지 않고 원래 상태로 응답할 수 있다.")
    @Test
    void test4() throws Exception {
        // when & then
        mockMvc.perform(post("/test/custom"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    @DisplayName("처리되지 않은 예외는 500으로 응답할 수 있다.")
    @Test
    void test5() throws Exception {
        // when & then
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @DisplayName("AuthenticationException은 500이 아닌 401과 UNAUTHORIZED로 응답할 수 있다.")
    @Test
    void test6() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/unauthenticated"));

        // then
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @DisplayName("컨트롤러 안에서 던져진 AccessDeniedException은 500이 아닌 403과 FORBIDDEN으로 응답할 수 있다.")
    @Test
    void test7() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/denied"));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/custom")
        void custom() {
            throw new CustomException(TestErrorCode.TEST_NOT_FOUND);
        }

        @PostMapping("/test/valid")
        void valid(@RequestBody @Valid TestRequest request) {
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("unexpected");
        }

        @GetMapping("/test/unauthenticated")
        void unauthenticated() {
            throw new InsufficientAuthenticationException("unauthenticated");
        }

        @GetMapping("/test/denied")
        void denied() {
            // @PreAuthorize 실패처럼 컨트롤러 안에서 던져지는 경우를 재현한다.
            throw new AccessDeniedException("denied");
        }
    }

    record TestRequest(
            @NotBlank(message = "이름은 필수입니다.")
            String name
    ) {
    }

    @Getter
    @RequiredArgsConstructor
    enum TestErrorCode implements ErrorCode {

        // ===== 404 Not Found =====
        TEST_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 리소스를 찾을 수 없습니다.");

        private final HttpStatus status;
        private final String message;
    }
}
