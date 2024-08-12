package ru.yandex.kardomoblieapp.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.location.model.Country;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {
    @Query("SELECT c FROM Country c ORDER BY c.name ASC")
    List<Country> findAllCountriesOrderByName();
}
