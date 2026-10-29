package com.whattoeat.common.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 카카오 로컬 키워드 장소 검색 응답. 사용하는 필드만 매핑한다.
 */
public record KakaoKeywordSearchResponse(
        Meta meta,
        List<KakaoPlaceDocument> documents
) {

    public record Meta(
            @JsonProperty("total_count") int totalCount,
            // 카카오가 실제로 페이징해 주는 결과 수 (최대 45개)
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("is_end") boolean isEnd
    ) {
    }
}
