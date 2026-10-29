package com.whattoeat.domain.place.model.dto.response;

import com.whattoeat.common.client.kakao.dto.KakaoPlaceDocument;

public record PlaceSearchResponse(
        String externalId,
        String name,
        String categoryName,
        String phone,
        String addressName,
        String roadAddressName,
        Double longitude,
        Double latitude,
        String placeUrl
) {

    public static PlaceSearchResponse from(KakaoPlaceDocument document) {
        return new PlaceSearchResponse(
                document.id(),
                document.placeName(),
                document.categoryName(),
                document.phone(),
                document.addressName(),
                document.roadAddressName(),
                Double.valueOf(document.x()),
                Double.valueOf(document.y()),
                document.placeUrl());
    }
}
