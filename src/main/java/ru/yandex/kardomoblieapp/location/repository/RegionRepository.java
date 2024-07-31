package ru.yandex.kardomoblieapp.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.location.model.Region;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    @Query("SELECT r FROM Region r LEFT JOIN FETCH r.country c WHERE c.id = ?1")
    List<Region> findAllByCountryId(long countryId);

    @Query("SELECT r FROM Region r LEFT JOIN FETCH r.country c WHERE r.id = ?1")
    Optional<Region> findByRegionId(long regionId);
}
