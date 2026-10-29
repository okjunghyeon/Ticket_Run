package com.whattoeat.domain.place.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceSearchRequest(
        @NotBlank(message = "검색어는 필수입니다.")
        @Size(max = 100, message = "검색어는 100자 이하로 입력해 주세요.")
        String keyword
) {
}
