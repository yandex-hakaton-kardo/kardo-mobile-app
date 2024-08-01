package ru.yandex.kardomoblieapp.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.location.model.City;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndCountryIdAndRegionId(String cityName, Long countryId, Long regionId);
}
