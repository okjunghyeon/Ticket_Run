package com.whattoeat.common.client.kakao;

import com.whattoeat.common.client.kakao.dto.KakaoKeywordSearchResponse;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 카카오 로컬 API 클라이언트. 실패 시 RestClientException 을 그대로 던지며, 도메인 예외 변환은 호출하는 Service 가 한다.
 */
@Component
public class KakaoLocalClient {

    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
    private static final String RESTAURANT_CATEGORY_CODE = "FD6";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient restClient;

    @Autowired
    public KakaoLocalClient(KakaoLocalProperties properties) {
        this(RestClient.builder().requestFactory(createRequestFactory()), properties);
    }

    // 테스트에서 MockRestServiceServer 를 연결한 Builder 를 넘기기 위한 생성자
    KakaoLocalClient(RestClient.Builder builder, KakaoLocalProperties properties) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
                .build();
    }

    /**
     * 키워드로 음식점(FD6)을 검색한다. 좌표가 null 이면 위치 제한 없이 검색한다.
     *
     * @param query     검색어 (예: "강남 파스타")
     * @param longitude 검색 중심 경도 (nullable)
     * @param latitude  검색 중심 위도 (nullable)
     * @param radius    검색 반경 미터 (nullable, 최대 20000)
     * @param page      1부터 시작하는 페이지 번호 (최대 45)
     * @param size      페이지 크기 (최대 15)
     * @return 카카오 검색 결과
     */
    public KakaoKeywordSearchResponse searchRestaurants(String query, Double longitude, Double latitude,
                                                        Integer radius, int page, int size) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(KEYWORD_SEARCH_PATH)
                        .queryParam("query", query)
                        .queryParam("category_group_code", RESTAURANT_CATEGORY_CODE)
                        .queryParamIfPresent("x", Optional.ofNullable(longitude))
                        .queryParamIfPresent("y", Optional.ofNullable(latitude))
                        .queryParamIfPresent("radius", Optional.ofNullable(radius))
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(KakaoKeywordSearchResponse.class);
    }

    /* ===== private Method ===== */

    private static SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }
}
