package ru.yandex.kardomoblieapp.event.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto toDto(Event event);
}
