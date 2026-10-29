package com.whattoeat.domain.place.model.entity;

import com.whattoeat.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리스트에 담긴 맛집. 같은 맛집을 리스트 간에 같은 데이터로 식별하도록 외부 장소 ID(카카오 장소 ID)로 구분한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "place", uniqueConstraints = {
        @UniqueConstraint(name = "uk_place_external_id", columnNames = "external_id")
})
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 예: "음식점 > 한식 > 냉면"
    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address_name", length = 200)
    private String addressName;

    @Column(name = "road_address_name", length = 200)
    private String roadAddressName;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "place_url", length = 512)
    private String placeUrl;

    @Builder
    public Place(String externalId, String name, String categoryName, String phone, String addressName,
                 String roadAddressName, Double longitude, Double latitude, String placeUrl) {
        this.externalId = externalId;
        this.name = name;
        this.categoryName = categoryName;
        this.phone = phone;
        this.addressName = addressName;
        this.roadAddressName = roadAddressName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeUrl = placeUrl;
    }
}
