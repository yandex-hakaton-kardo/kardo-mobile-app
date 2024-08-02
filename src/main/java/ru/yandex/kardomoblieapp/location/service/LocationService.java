package ru.yandex.kardomoblieapp.location.service;

import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    List<Country> getAllCountries();

    Country getCountryById(long countryId);

    List<Region> getAllCountryRegions(long countryId);

    Region getRegionById(long regionId);

    City addCity(City city);

    Optional<City> findCityByNameCountryAndRegion(String cityName, Long countryId, Long regionId);

    void deleteAllCities();
}
