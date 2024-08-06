package ru.yandex.kardomoblieapp.location.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class LocationServiceImplTest {

    @Autowired
    private LocationService locationService;

    private long id = 1L;

    private long unknownId = 9999L;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_VERSION);

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Получение списка всех стран")
    void getAllCountries_shouldReturnNotEmptyList() {
        List<Country> countries = locationService.getAllCountries();

        assertThat(countries, notNullValue());
        assertThat(countries.size(), greaterThan(0));
    }

    @Test
    @DisplayName("Получение страны по идентификатору")
    void getCountryById_whenIdExists_shouldReturnCountry() {
        Country country = locationService.getCountryById(id);

        assertThat(country, notNullValue());
        assertThat(country.getId(), is(id));
    }

    @Test
    @DisplayName("Получение страны по неизвестному идентификатору")
    void getCountryById_whenIdNotExists_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> locationService.getCountryById(unknownId));

        assertThat(ex.getMessage(), is("Страна с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Получение списка регионов страны")
    void getAllCountryRegions_whenCountryHasRegions_shouldReturnNotEmptyList() {
        List<Region> regions = locationService.getAllCountryRegions(id);

        assertThat(regions, notNullValue());
        assertThat(regions.size(), greaterThan(0));
    }

    @Test
    @DisplayName("Получение списка регионов страны без регионов")
    void getAllCountryRegions_whenCountryHasNoRegions_shouldReturnEmptyList() {
        List<Region> regions = locationService.getAllCountryRegions(5L);

        assertThat(regions, notNullValue());
        assertThat(regions, emptyIterable());
    }

    @Test
    @DisplayName("Получение региона по id")
    void getRegionById_whenRegionExists_shouldReturnRegion() {
        Region region = locationService.getRegionById(id);

        assertThat(region, notNullValue());
        assertThat(region.getId(), is(id));
    }

    @Test
    @DisplayName("Получение региона по неизвестному идентификатору")
    void getRegionById_whenRegionNotExists_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> locationService.getRegionById(unknownId));

        assertThat(ex.getMessage(), is("Регион с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Добавление города без страны и региона")
    void addCity_whenNoCountryAndRegion_shouldReturnCityWithNotNullId() {
        City city = City.builder()
                .country(null)
                .name("city")
                .region(null)
                .build();

        City addedCity = locationService.addCity(city);

        assertThat(addedCity, notNullValue());
        assertThat(addedCity.getId(), greaterThan(0L));
        assertThat(addedCity.getCountry(), nullValue());
        assertThat(addedCity.getRegion(), nullValue());
    }

    @Test
    @DisplayName("Добавление города без страны и региона")
    void addCity_whenWithCountryAndNoRegion_shouldReturnCityWithNotNullId() {
        Country country = locationService.getCountryById(id);
        City city = City.builder()
                .country(country)
                .name("city")
                .region(null)
                .build();

        City addedCity = locationService.addCity(city);

        assertThat(addedCity, notNullValue());
        assertThat(addedCity.getId(), greaterThan(0L));
        assertThat(addedCity.getCountry().getId(), is(country.getId()));
        assertThat(addedCity.getRegion(), nullValue());
    }

    @Test
    @DisplayName("Добавление города без страны и региона")
    void addCity_whenWithCountryAndRegion_shouldReturnCityWithNotNullId() {
        Country country = locationService.getCountryById(id);
        Region region = locationService.getRegionById(id);
        City city = City.builder()
                .country(country)
                .name("city")
                .region(region)
                .build();

        City addedCity = locationService.addCity(city);

        assertThat(addedCity, notNullValue());
        assertThat(addedCity.getId(), greaterThan(0L));
        assertThat(addedCity.getCountry().getId(), is(country.getId()));
        assertThat(addedCity.getRegion().getId(), is(region.getId()));
    }

    @Test
    @DisplayName("Поиск города, когда страна и регион null")
    void findCityByNameCountryAndRegion_whenCountyAndRegionAreNull_shouldReturnCity() {
        City city = City.builder()
                .country(null)
                .name("new city")
                .region(null)
                .build();
        City addedCity = locationService.addCity(city);

        Optional<City> cityOptional = locationService.findCityByNameCountryAndRegion(city.getName(), null, null);

        assertThat(cityOptional.isPresent(), is(true));
        assertThat(cityOptional.get().getId(), is(addedCity.getId()));
    }

    @Test
    @DisplayName("Поиск города, когда все null")
    void findCityByNameCountryAndRegion_whenAllNull_shouldEmptyOptional() {

        Optional<City> cityOptional = locationService.findCityByNameCountryAndRegion(null, null, null);

        assertThat(cityOptional.isEmpty(), is(true));
    }
}