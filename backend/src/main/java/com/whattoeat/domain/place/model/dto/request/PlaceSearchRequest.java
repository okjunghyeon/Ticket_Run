package com.whattoeat.domain.place.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 맛집 검색 조건. 좌표가 있으면 지도 중심 반경 안에서, 없으면 위치 제한 없이 검색한다.
 */
public record PlaceSearchRequest(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 100, message = "검색어는 100자 이하로 입력해 주세요.")
        String keyword,

        @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
        Double longitude,

        @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
        Double latitude,

        @Min(value = 1, message = "검색 반경은 1m 이상이어야 합니다.")
        @Max(value = 20000, message = "검색 반경은 20000m 이하여야 합니다.")
        Integer radius
) {

    private static final int DEFAULT_RADIUS = 1000;

    public PlaceSearchRequest {
        if (radius == null) {
            radius = DEFAULT_RADIUS;
        }
    }

    public boolean hasCoordinate() {
        return longitude != null && latitude != null;
    }

    @AssertTrue(message = "경도와 위도는 함께 입력해 주세요.")
    public boolean isCoordinatePaired() {
        return (longitude == null) == (latitude == null);
    }
}
