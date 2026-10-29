package com.whattoeat.domain.place.repository;

import com.whattoeat.domain.place.model.entity.Place;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByExternalId(String externalId);
}
