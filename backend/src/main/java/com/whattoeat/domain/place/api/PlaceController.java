package com.whattoeat.domain.place.api;

import com.whattoeat.common.model.BaseResponse;
import com.whattoeat.common.model.PageResponse;
import com.whattoeat.domain.place.model.dto.request.PlaceSearchRequest;
import com.whattoeat.domain.place.model.dto.response.PlaceResponse;
import com.whattoeat.domain.place.model.dto.response.PlaceSearchResponse;
import com.whattoeat.domain.place.service.PlaceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<List<PlaceSearchResponse>> searchPlaces(
            @Valid PlaceSearchRequest request,
            @PageableDefault(size = 15) Pageable pageable) {
        return placeService.searchPlaces(request, pageable);
    }

    @GetMapping("/{placeId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<PlaceResponse> getPlace(@PathVariable Long placeId) {
        return new BaseResponse<>(placeService.getPlace(placeId), "맛집 정보를 조회했습니다.");
    }
}
