package com.whattoeat.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.whattoeat.common.client.kakao.KakaoLocalClient;
import com.whattoeat.common.client.kakao.dto.KakaoKeywordSearchResponse;
import com.whattoeat.common.client.kakao.dto.KakaoPlaceDocument;
import com.whattoeat.common.model.PageResponse;
import com.whattoeat.domain.place.exception.PlaceException;
import com.whattoeat.domain.place.exception.PlaceExceptionCode;
import com.whattoeat.domain.place.model.dto.request.PlaceRequest;
import com.whattoeat.domain.place.model.dto.request.PlaceSearchRequest;
import com.whattoeat.domain.place.model.dto.response.PlaceResponse;
import com.whattoeat.domain.place.model.dto.response.PlaceSearchResponse;
import com.whattoeat.domain.place.model.entity.Place;
import com.whattoeat.domain.place.repository.PlaceRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @InjectMocks
    private PlaceService placeService;

    @DisplayName("키워드로 맛집을 검색할 수 있다.")
    @Test
    void test1() {
        // given
        PlaceSearchRequest request = new PlaceSearchRequest("강남 파스타", null, null, null);
        KakaoKeywordSearchResponse response = new KakaoKeywordSearchResponse(
                new KakaoKeywordSearchResponse.Meta(120, 45, false),
                List.of(createDocument("26338954")));

        given(kakaoLocalClient.searchRestaurants("강남 파스타", null, null, null, 1, 15)).willReturn(response);

        // when
        PageResponse<List<PlaceSearchResponse>> result = placeService.searchPlaces(request, PageRequest.of(0, 15));

        // then
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().externalId()).isEqualTo("26338954");
        assertThat(result.data().getFirst().longitude()).isEqualTo(127.0276);
        assertThat(result.data().getFirst().latitude()).isEqualTo(37.4979);
        assertThat(result.totalElements()).isEqualTo(45);
        assertThat(result.hasNext()).isTrue();
    }

    @DisplayName("페이지 크기가 15를 넘으면 15개로 보정해 검색할 수 있다.")
    @Test
    void test2() {
        // given
        PlaceSearchRequest request = new PlaceSearchRequest("을지로 노포", null, null, null);
        KakaoKeywordSearchResponse response = new KakaoKeywordSearchResponse(
                new KakaoKeywordSearchResponse.Meta(0, 0, true), List.of());

        given(kakaoLocalClient.searchRestaurants("을지로 노포", null, null, null, 2, 15)).willReturn(response);

        // when
        PageResponse<List<PlaceSearchResponse>> result = placeService.searchPlaces(request, PageRequest.of(1, 50));

        // then
        assertThat(result.size()).isEqualTo(15);
        verify(kakaoLocalClient).searchRestaurants("을지로 노포", null, null, null, 2, 15);
    }

    @DisplayName("45번째 이후의 검색 결과는 조회할 수 없다.")
    @Test
    void test3() {
        // given
        PlaceSearchRequest request = new PlaceSearchRequest("강남 파스타", null, null, null);

        // when & then
        assertThatThrownBy(() -> placeService.searchPlaces(request, PageRequest.of(3, 15)))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceExceptionCode.SEARCH_PAGE_OUT_OF_RANGE);

        verify(kakaoLocalClient, never()).searchRestaurants(anyString(), any(), any(), any(), anyInt(), anyInt());
    }

    @DisplayName("외부 장소 검색 API 호출에 실패하면 맛집을 검색할 수 없다.")
    @Test
    void test4() {
        // given
        PlaceSearchRequest request = new PlaceSearchRequest("강남 파스타", null, null, null);

        given(kakaoLocalClient.searchRestaurants("강남 파스타", null, null, null, 1, 15))
                .willThrow(new ResourceAccessException("timeout"));

        // when & then
        assertThatThrownBy(() -> placeService.searchPlaces(request, PageRequest.of(0, 15)))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceExceptionCode.EXTERNAL_API_ERROR);
    }

    @DisplayName("저장된 맛집의 상세 정보를 조회할 수 있다.")
    @Test
    void test5() {
        // given
        Place place = createPlace(1L, "26338954");

        given(placeRepository.findById(1L)).willReturn(Optional.of(place));

        // when
        PlaceResponse result = placeService.getPlace(1L);

        // then
        assertThat(result.placeId()).isEqualTo(1L);
        assertThat(result.externalId()).isEqualTo("26338954");
    }

    @DisplayName("존재하지 않는 맛집은 조회할 수 없다.")
    @Test
    void test6() {
        // given
        given(placeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> placeService.getPlace(1L))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceExceptionCode.PLACE_NOT_FOUND);
    }

    @DisplayName("이미 저장된 맛집은 다시 저장하지 않고 기존 맛집 ID를 반환할 수 있다.")
    @Test
    void test7() {
        // given
        PlaceRequest request = createPlaceRequest("26338954");
        Place place = createPlace(1L, "26338954");

        given(placeRepository.findByExternalId("26338954")).willReturn(Optional.of(place));

        // when
        Long result = placeService.getOrCreatePlace(request);

        // then
        assertThat(result).isEqualTo(1L);
        verify(placeRepository, never()).save(any(Place.class));
    }

    @DisplayName("처음 담기는 맛집은 저장하고 새 맛집 ID를 반환할 수 있다.")
    @Test
    void test8() {
        // given
        PlaceRequest request = createPlaceRequest("26338954");
        Place savedPlace = createPlace(2L, "26338954");

        given(placeRepository.findByExternalId("26338954")).willReturn(Optional.empty());
        given(placeRepository.save(any(Place.class))).willReturn(savedPlace);

        // when
        Long result = placeService.getOrCreatePlace(request);

        // then
        assertThat(result).isEqualTo(2L);
        verify(placeRepository).save(any(Place.class));
    }

    private KakaoPlaceDocument createDocument(String id) {
        return new KakaoPlaceDocument(id, "강남파스타", "음식점 > 양식 > 이탈리안", "02-123-4567",
                "서울 강남구 역삼동 1", "서울 강남구 테헤란로 1", "127.0276", "37.4979",
                "http://place.map.kakao.com/" + id);
    }

    private PlaceRequest createPlaceRequest(String externalId) {
        return PlaceRequest.builder()
                .externalId(externalId)
                .name("강남파스타")
                .longitude(127.0276)
                .latitude(37.4979)
                .build();
    }

    private Place createPlace(Long id, String externalId) {
        Place place = Place.builder()
                .externalId(externalId)
                .name("강남파스타")
                .longitude(127.0276)
                .latitude(37.4979)
                .build();
        ReflectionTestUtils.setField(place, "id", id);
        return place;
    }
}
