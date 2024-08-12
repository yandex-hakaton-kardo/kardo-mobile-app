package ru.yandex.kardomoblieapp.datafiles.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;

@Mapper(componentModel = "spring")
public interface DataFileMapper {

    DataFileDto toDto(DataFile dataFile);
}
