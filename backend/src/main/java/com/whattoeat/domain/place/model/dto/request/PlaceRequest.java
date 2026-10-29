package com.whattoeat.domain.place.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 검색 결과에서 고른 맛집 정보. 리스트에 맛집을 추가할 때 Place 로 저장하는 데 사용한다.
 */
@Builder
public record PlaceRequest(
        @NotBlank(message = "외부 장소 ID는 필수입니다.")
        String externalId,

        @NotBlank(message = "맛집 이름은 필수입니다.")
        String name,

        String categoryName,
        String phone,
        String addressName,
        String roadAddressName,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        String placeUrl
) {
}
