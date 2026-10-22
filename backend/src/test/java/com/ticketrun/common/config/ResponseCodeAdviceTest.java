package com.ticketrun.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketrun.common.model.BaseResponse;
import com.ticketrun.common.model.PageResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

class ResponseCodeAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 시큐리티 필터 없이 advice의 code 반영만 검증하기 위해 standalone으로 구성한다.
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ResponseCodeAdvice())
                .build();
    }

    @DisplayName("GET 성공 응답의 code에 200을 채울 수 있다.")
    @Test
    void test1() throws Exception {
        // when & then
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("data"));
    }

    @DisplayName("@ResponseStatus(CREATED) 응답의 code에 201을 채울 수 있다.")
    @Test
    void test2() throws Exception {
        // when & then
        mockMvc.perform(post("/test/created"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @DisplayName("ResponseEntity로 지정한 상태를 code에 채울 수 있다.")
    @Test
    void test3() throws Exception {
        // when & then
        mockMvc.perform(post("/test/accepted"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @DisplayName("목록 응답의 code에도 실제 상태 코드를 채울 수 있다.")
    @Test
    void test4() throws Exception {
        // when & then
        mockMvc.perform(get("/test/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ok")
        @ResponseStatus(HttpStatus.OK)
        BaseResponse<String> ok() {
            return new BaseResponse<>("data", "조회했습니다.");
        }

        @PostMapping("/test/created")
        @ResponseStatus(HttpStatus.CREATED)
        BaseResponse<String> created() {
            return new BaseResponse<>("data", "생성했습니다.");
        }

        @PostMapping("/test/accepted")
        ResponseEntity<BaseResponse<Void>> accepted() {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new BaseResponse<>("접수했습니다."));
        }

        @GetMapping("/test/page")
        @ResponseStatus(HttpStatus.OK)
        PageResponse<List<String>> page() {
            return PageResponse.of(new PageImpl<>(List.of("a"), PageRequest.of(0, 10), 1), "조회했습니다.");
        }
    }
}
