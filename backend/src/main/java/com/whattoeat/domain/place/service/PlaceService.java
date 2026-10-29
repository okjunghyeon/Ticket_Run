package com.whattoeat.domain.place.service;

import static com.whattoeat.domain.place.exception.PlaceExceptionCode.EXTERNAL_API_ERROR;
import static com.whattoeat.domain.place.exception.PlaceExceptionCode.PLACE_NOT_FOUND;
import static com.whattoeat.domain.place.exception.PlaceExceptionCode.SEARCH_PAGE_OUT_OF_RANGE;

import com.whattoeat.common.client.kakao.KakaoLocalClient;
import com.whattoeat.common.client.kakao.dto.KakaoKeywordSearchResponse;
import com.whattoeat.common.model.PageResponse;
import com.whattoeat.domain.place.exception.PlaceException;
import com.whattoeat.domain.place.model.dto.request.PlaceRequest;
import com.whattoeat.domain.place.model.dto.request.PlaceSearchRequest;
import com.whattoeat.domain.place.model.dto.response.PlaceResponse;
import com.whattoeat.domain.place.model.dto.response.PlaceSearchResponse;
import com.whattoeat.domain.place.model.entity.Place;
import com.whattoeat.domain.place.repository.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

    // 카카오 키워드 검색은 페이지당 최대 15개, 검색어당 최대 45개까지만 결과를 준다.
    private static final int MAX_PAGE_SIZE = 15;
    private static final int MAX_SEARCH_RESULT_COUNT = 45;

    private final PlaceRepository placeRepository;
    private final KakaoLocalClient kakaoLocalClient;

    /**
     * 키워드로 맛집을 검색한다. DB를 사용하지 않으므로 외부 API 호출 동안 트랜잭션을 열지 않는다.
     *
     * @param request  검색어
     * @param pageable 0부터 시작하는 페이지 번호와 크기 (크기는 최대 15로 보정)
     * @return 검색된 맛집 목록
     * @throws PlaceException 45번째 결과 이후를 요청한 경우 (SEARCH_PAGE_OUT_OF_RANGE),
     *                        외부 API 호출에 실패한 경우 (EXTERNAL_API_ERROR)
     */
    public PageResponse<List<PlaceSearchResponse>> searchPlaces(PlaceSearchRequest request, Pageable pageable) {
        Pageable searchPageable = PageRequest.of(pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        if (searchPageable.getOffset() >= MAX_SEARCH_RESULT_COUNT) {
            throw new PlaceException(SEARCH_PAGE_OUT_OF_RANGE);
        }

        KakaoKeywordSearchResponse response = searchKakaoRestaurants(request.keyword(), searchPageable);
        List<PlaceSearchResponse> places = response.documents().stream()
                .map(PlaceSearchResponse::from)
                .toList();
        return PageResponse.of(new PageImpl<>(places, searchPageable, response.meta().pageableCount()),
                "맛집을 검색했습니다.");
    }

    /**
     * @param placeId 조회할 맛집 ID
     * @return 맛집 상세 정보
     * @throws PlaceException 맛집이 존재하지 않는 경우 (PLACE_NOT_FOUND)
     */
    @Transactional(readOnly = true)
    public PlaceResponse getPlace(Long placeId) {
        return placeRepository.findById(placeId)
                .map(PlaceResponse::from)
                .orElseThrow(() -> new PlaceException(PLACE_NOT_FOUND));
    }

    /**
     * [리스트 도메인용] 외부 장소 ID로 맛집을 조회하고, 처음 담기는 맛집이면 저장한다.
     *
     * @param request 검색 결과에서 고른 맛집 정보
     * @return 맛집 ID
     */
    // TODO: 같은 맛집을 동시에 처음 담으면 uk_place_external_id 위반이 날 수 있다. 리스트 맛집 추가(#28)에서 재시도 방식을 정한다.
    @Transactional
    public Long getOrCreatePlace(PlaceRequest request) {
        return placeRepository.findByExternalId(request.externalId())
                .orElseGet(() -> placeRepository.save(Place.builder()
                        .externalId(request.externalId())
                        .name(request.name())
                        .categoryName(request.categoryName())
                        .phone(request.phone())
                        .addressName(request.addressName())
                        .roadAddressName(request.roadAddressName())
                        .longitude(request.longitude())
                        .latitude(request.latitude())
                        .placeUrl(request.placeUrl())
                        .build()))
                .getId();
    }

    /* ===== private Method ===== */

    private KakaoKeywordSearchResponse searchKakaoRestaurants(String keyword, Pageable pageable) {
        try {
            // 카카오 페이지 번호는 1부터 시작한다.
            return kakaoLocalClient.searchRestaurants(keyword, null, null, null,
                    pageable.getPageNumber() + 1, pageable.getPageSize());
        } catch (RestClientException e) {
            log.error("카카오 장소 검색 실패: keyword={}", keyword, e);
            throw new PlaceException(EXTERNAL_API_ERROR);
        }
    }
}
