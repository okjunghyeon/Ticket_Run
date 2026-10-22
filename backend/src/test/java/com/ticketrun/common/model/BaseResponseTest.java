package com.ticketrun.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;

class BaseResponseTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @DisplayName("단건 성공 응답은 SUCCESS 코드를 가장 먼저 내려줄 수 있다.")
    @Test
    void test1() {
        // given
        BaseResponse<String> response = new BaseResponse<>("data", "조회했습니다.");

        // when
        String json = jsonMapper.writeValueAsString(response);

        // then
        assertThat(json).isEqualTo("{\"code\":\"SUCCESS\",\"data\":\"data\",\"message\":\"조회했습니다.\"}");
    }

    @DisplayName("메시지만 있는 성공 응답도 SUCCESS 코드를 내려줄 수 있다.")
    @Test
    void test2() {
        // given
        BaseResponse<Void> response = new BaseResponse<>("삭제했습니다.");

        // when
        String json = jsonMapper.writeValueAsString(response);

        // then
        assertThat(json).isEqualTo("{\"code\":\"SUCCESS\",\"data\":null,\"message\":\"삭제했습니다.\"}");
    }

    @DisplayName("목록 성공 응답은 SUCCESS 코드를 가장 먼저 내려줄 수 있다.")
    @Test
    void test3() {
        // given
        PageResponse<List<String>> response =
                PageResponse.of(new PageImpl<>(List.of("a"), PageRequest.of(0, 10), 1), "조회했습니다.");

        // when
        String json = jsonMapper.writeValueAsString(response);

        // then
        assertThat(json).startsWith("{\"code\":\"SUCCESS\",\"data\":[\"a\"]");
    }
}
