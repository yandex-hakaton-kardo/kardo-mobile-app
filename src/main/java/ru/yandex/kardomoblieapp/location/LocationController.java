package ru.yandex.kardomoblieapp.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.location.dto.CountryDto;
import ru.yandex.kardomoblieapp.location.dto.RegionDto;
import ru.yandex.kardomoblieapp.location.mapper.CityMapper;
import ru.yandex.kardomoblieapp.location.mapper.CountryMapper;
import ru.yandex.kardomoblieapp.location.mapper.RegionMapper;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;
import ru.yandex.kardomoblieapp.location.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    private final CountryMapper countryMapper;

    private final RegionMapper regionMapper;

    private final CityMapper cityMapper;

    @GetMapping("")
    public CountryDto getAllCountries() {
        log.info("Получение списка стран.");
        List<Country> countries = locationService.getAllCountries();
        return countryMapper.toDtoList(countries);
    }

    @GetMapping("/{countryId}")
    public CountryDto getCountryById(@PathVariable long countryId) {
        log.info("Получение списка стран.");
        Country country = locationService.getCountryById(countryId);
        return countryMapper.toDto(country);
    }

    @GetMapping("/{countryId}/regions/")
    public List<RegionDto> getAllRegionsByCountryId(@PathVariable long countryId) {
        log.info("Получение списка регионов страны с id '{}'.", countryId);
        List<Region> regions = locationService.getAllCountryRegions(countryId);
        return regionMapper.toDtoList(regions);
    }

    @GetMapping("/{countryId}/regions/{regionId}")
    public RegionDto getRegionById(@PathVariable long countryId,
                                   @PathVariable long regionId) {
        log.info("Получение региона с id '{}'.");
        Region region = locationService.getRegionById(regionId);
        return regionMapper.toDto(region);
    }
}
