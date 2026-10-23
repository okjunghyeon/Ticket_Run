package com.ticketrun.common.security;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @InjectMocks
    private CustomAccessDeniedHandler accessDeniedHandler;

    // 현재 인가 규칙(authenticated())만으로는 403이 나는 경로가 없어, 위임 여부만 단위로 검증한다.
    // 403 응답 형식은 GlobalExceptionHandlerTest에서 검증한다.
    @DisplayName("인가 실패 예외를 HandlerExceptionResolver에 위임할 수 있다.")
    @Test
    void test1() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("denied");

        // when
        accessDeniedHandler.handle(request, response, exception);

        // then
        then(handlerExceptionResolver).should().resolveException(request, response, null, exception);
    }
}
