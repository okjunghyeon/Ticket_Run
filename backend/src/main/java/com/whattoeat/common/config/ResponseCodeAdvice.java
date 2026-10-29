package com.whattoeat.common.config;

import com.whattoeat.common.model.BaseResponse;
import com.whattoeat.common.model.PageResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 성공 응답의 code에 실제 HTTP 상태 코드를 채운다.
 * 컨트롤러가 code를 직접 넘기면 @ResponseStatus와 값이 어긋날 수 있으므로, 상태가 확정된 직후 한 곳에서 반영한다.
 */
@RestControllerAdvice
public class ResponseCodeAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(response instanceof ServletServerHttpResponse servletResponse)) {
            return body;
        }

        // @ResponseStatus, ResponseEntity의 상태는 본문을 쓰기 전에 서블릿 응답에 이미 반영되어 있다.
        int status = servletResponse.getServletResponse().getStatus();
        if (body instanceof BaseResponse<?> baseResponse) {
            return baseResponse.withCode(status);
        }
        if (body instanceof PageResponse<?> pageResponse) {
            return pageResponse.withCode(status);
        }
        return body;
    }
}
