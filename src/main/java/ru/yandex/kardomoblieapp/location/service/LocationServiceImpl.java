package ru.yandex.kardomoblieapp.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;
import ru.yandex.kardomoblieapp.location.repository.CityRepository;
import ru.yandex.kardomoblieapp.location.repository.CountryRepository;
import ru.yandex.kardomoblieapp.location.repository.RegionRepository;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final CountryRepository countryRepository;

    private final RegionRepository regionRepository;

    private final CityRepository cityRepository;

    /**
     * Получение списка всех стран.
     *
     * @return список стран
     */
    @Override
    public List<Country> getAllCountries() {
        List<Country> countries = countryRepository.findAllCountriesOrderByName();
        log.info("Получен список всех стран");
        return countries;
    }

    /**
     * Получение страны по идентификатору.
     *
     * @param countryId идентификатор страны
     * @return найденная страна
     */
    @Override
    public Country getCountryById(long countryId) {
        Country country = getCountry(countryId);
        log.info("Получена страна с id '{}'.", countryId);
        return country;
    }

    /**
     * Получение списка регионов страны.
     *
     * @param countryId идентификатор страны
     * @return список регионов страны
     */
    @Override
    public List<Region> getAllCountryRegions(long countryId) {
        List<Region> regions = regionRepository.findAllByCountryIdOrderByName(countryId);
        log.info("Получены все регионы страны с id '{}'.", countryId);
        return regions;
    }

    /**
     * Получение региона по идентификатору.
     *
     * @param regionId идентификатор региона
     * @return найденный регион
     */
    @Override
    public Region getRegionById(long regionId) {
        Region region = getRegion(regionId);
        log.info("Получен регион с id '{}'.", regionId);
        return region;
    }

    /**
     * Добавление города.
     *
     * @param city город
     * @return сохраненный город
     */
    @Override
    public City addCity(City city) {
        City savedCity = cityRepository.save(city);
        log.info("Добавлен город c id '{}'.", savedCity.getId());
        return savedCity;
    }

    /**
     * Поиск города по его названию, стране и региону.
     *
     * @param cityName  название города
     * @param countryId идентификатор страны
     * @param regionId  идентификатор региона
     * @return найденный город
     */
    @Override
    public Optional<City> findCityByNameCountryAndRegion(String cityName, Long countryId, Long regionId) {
        Optional<City> city = cityRepository.findByNameAndCountryIdAndRegionId(cityName, countryId, regionId);
        log.info("Поиск наличия города с названием '{}' и регионом с id'{}'.", cityName, regionId);
        return city;
    }

    /**
     * Удаление всех добавленных городов.
     */
    @Override
    public void deleteAllCities() {
        cityRepository.deleteAll();
        log.info("Удаление всех городов.");
    }

    private Country getCountry(long countryId) {
        return countryRepository.findById(countryId).orElseThrow(
                () -> new NotFoundException("Страна с id '" + countryId + "' не найдена."));
    }

    private Region getRegion(long regionId) {
        return regionRepository.findByRegionId(regionId)
                .orElseThrow(() -> new NotFoundException("Регион с id '" + regionId + "' не найден."));
    }
}
