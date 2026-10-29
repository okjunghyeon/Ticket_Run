package com.whattoeat.domain.place.model.dto.response;

import com.whattoeat.domain.place.model.entity.Place;

public record PlaceResponse(
        Long placeId,
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

    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getExternalId(),
                place.getName(),
                place.getCategoryName(),
                place.getPhone(),
                place.getAddressName(),
                place.getRoadAddressName(),
                place.getLongitude(),
                place.getLatitude(),
                place.getPlaceUrl());
    }
}
