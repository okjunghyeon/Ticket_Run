package com.whattoeat.common.client.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.whattoeat.common.client.kakao.dto.KakaoKeywordSearchResponse;
import com.whattoeat.common.client.kakao.dto.KakaoPlaceDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoLocalClientTest {

    private static final String BASE_URL = "https://dapi.kakao.com";

    private MockRestServiceServer server;
    private KakaoLocalClient kakaoLocalClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        kakaoLocalClient = new KakaoLocalClient(builder, new KakaoLocalProperties(BASE_URL, "test-key"));
    }

    @DisplayName("키워드로 음식점을 검색하면 카카오 응답을 장소 목록으로 받을 수 있다.")
    @Test
    void test1() {
        // given
        String body = """
                {
                  "meta": {"total_count": 120, "pageable_count": 45, "is_end": false,
                           "same_name": {"region": [], "keyword": "강남 파스타", "selected_region": ""}},
                  "documents": [{
                    "id": "26338954", "place_name": "강남파스타", "category_name": "음식점 > 양식 > 이탈리안",
                    "category_group_code": "FD6", "category_group_name": "음식점", "phone": "02-123-4567",
                    "address_name": "서울 강남구 역삼동 1", "road_address_name": "서울 강남구 테헤란로 1",
                    "x": "127.0276", "y": "37.4979", "place_url": "http://place.map.kakao.com/26338954",
                    "distance": ""
                  }]
                }
                """;

        server.expect(requestTo(startsWith(BASE_URL + "/v2/local/search/keyword.json")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK test-key"))
                .andExpect(queryParam("category_group_code", "FD6"))
                .andExpect(queryParam("page", "2"))
                .andExpect(queryParam("size", "15"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        // when
        KakaoKeywordSearchResponse result = kakaoLocalClient.searchRestaurants("강남 파스타", 2, 15);

        // then
        assertThat(result.meta().pageableCount()).isEqualTo(45);
        assertThat(result.meta().isEnd()).isFalse();

        KakaoPlaceDocument document = result.documents().getFirst();
        assertThat(document.id()).isEqualTo("26338954");
        assertThat(document.placeName()).isEqualTo("강남파스타");
        assertThat(document.roadAddressName()).isEqualTo("서울 강남구 테헤란로 1");
        assertThat(document.x()).isEqualTo("127.0276");
        server.verify();
    }
}
