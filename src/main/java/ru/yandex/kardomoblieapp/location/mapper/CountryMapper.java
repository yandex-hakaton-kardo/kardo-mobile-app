package ru.yandex.kardomoblieapp.location.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.location.dto.CountryDto;
import ru.yandex.kardomoblieapp.location.model.Country;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDto toDto(Country country);

    List<CountryDto> toDtoList(List<Country> countries);
}
