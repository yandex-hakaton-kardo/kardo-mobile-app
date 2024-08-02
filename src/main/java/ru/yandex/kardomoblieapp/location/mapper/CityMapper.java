package ru.yandex.kardomoblieapp.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.kardomoblieapp.location.dto.CityDto;
import ru.yandex.kardomoblieapp.location.model.City;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "country.id", target = "countryId")
    CityDto toDto(City city);
}
