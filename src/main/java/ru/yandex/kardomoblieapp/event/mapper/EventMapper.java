package ru.yandex.kardomoblieapp.event.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "country.name", target = "country")
    @Mapping(source = "region.name", target = "region")
    @Mapping(source = "city.name", target = "city")
    @Mapping(source = "activity.name", target = "activity")
    @Mapping(source = "masterEvent.id", target = "masterEvent")
    EventDto toDto(Event event);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "activity", ignore = true)
    Event toModel(NewSubEventRequest newSubEventRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "city", ignore = true)
    void updateEvent(EventUpdateRequest updateRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "city", ignore = true)
    void createNewEvent(NewEventRequest newEventRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "city", ignore = true)
    void createSubEvent(NewSubEventRequest newSubEventRequest, @MappingTarget Event event);
}
