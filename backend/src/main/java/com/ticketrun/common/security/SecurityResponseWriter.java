package com.ticketrun.common.security;

import com.ticketrun.common.exception.ErrorCode;
import com.ticketrun.common.model.ErrorDetail;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 시큐리티 필터에서 끝나는 실패 응답을 ErrorDetail 형식으로 쓴다.
 * 필터 단계의 예외는 DispatcherServlet 밖이라 GlobalExceptionHandler가 처리하지 못하므로 직접 직렬화한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {

    private final JsonMapper jsonMapper;

    public void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(),
                ErrorDetail.of(errorCode.getStatus().value(), errorCode.getCode(), errorCode.getMessage()));
    }
}
