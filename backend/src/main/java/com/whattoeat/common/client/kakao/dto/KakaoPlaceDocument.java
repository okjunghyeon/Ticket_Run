package com.whattoeat.common.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 로컬 장소 정보. 좌표는 WGS84 이며 x 가 경도, y 가 위도다.
 */
public record KakaoPlaceDocument(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("category_name") String categoryName,
        String phone,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("road_address_name") String roadAddressName,
        String x,
        String y,
        @JsonProperty("place_url") String placeUrl
) {
}
