package ru.yandex.kardomoblieapp.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.kardomoblieapp.location.dto.RegionDto;
import ru.yandex.kardomoblieapp.location.model.Region;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    @Mapping(source = "country.id", target = "countryId")
    RegionDto toDto(Region region);

    List<RegionDto> toDtoList(List<Region> regions);
}
