package ru.yandex.kardomoblieapp.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.yandex.kardomoblieapp.shared.exception.ErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Географические локации", description = "Взаимодействие с географическими локациями")
public class LocationController {

    private final LocationService locationService;

    private final CountryMapper countryMapper;

    private final RegionMapper regionMapper;

    private final CityMapper cityMapper;

    @GetMapping
    @Operation(summary = "Получение списка всех стран в алфавитном порядке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список стран получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CountryDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<CountryDto> getAllCountries() {
        log.debug("Получение списка стран.");
        List<Country> countries = locationService.getAllCountries();
        return countryMapper.toDtoList(countries);
    }

    @GetMapping("/{countryId}")
    @Operation(summary = "Получение страны по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страна найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CountryDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Страна не найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public CountryDto getCountryById(@PathVariable @Parameter(description = "Идентификатор страны") long countryId) {
        log.debug("Получение списка стран.");
        Country country = locationService.getCountryById(countryId);
        return countryMapper.toDto(country);
    }

    @GetMapping("/{countryId}/regions/")
    @Operation(summary = "Получение регионов по идентификатору страны")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список регионов страны получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RegionDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Страна не найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<RegionDto> getAllRegionsByCountryId(@PathVariable @Parameter(description = "Идентификатор страны") long countryId) {
        log.debug("Получение списка регионов страны с id '{}'.", countryId);
        List<Region> regions = locationService.getAllCountryRegions(countryId);
        return regionMapper.toDtoList(regions);
    }

    @GetMapping("/{countryId}/regions/{regionId}")
    @Operation(summary = "Получение региона по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Регион найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = RegionDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Регион не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public RegionDto getRegionById(@PathVariable @Parameter(description = "Идентификатор страны") long countryId,
                                   @PathVariable @Parameter(description = "Идентификатор региона") long regionId) {
        log.debug("Получение региона с id '{}' страны с id '{}'.", regionId, countryId);
        Region region = locationService.getRegionById(regionId);
        return regionMapper.toDto(region);
    }
}
