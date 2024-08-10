package ru.yandex.kardomoblieapp.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.service.EventService;
import ru.yandex.kardomoblieapp.shared.exception.IncorrectEventDatesException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Мероприятия", description = "Администрирование мероприятий")
public class EventAdminController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание нового мероприятия")
    @SecurityRequirement(name = "JWT")
    public EventDto createEvent(@RequestBody @Valid @Parameter(description = "Новое мероприятие") NewEventRequest newEvent) {
        log.info("Добавление нового мероприятия.");
        validateDateRange(newEvent.getEventStart(), newEvent.getEventEnd());
        final Event savedEvent = eventService.createEvent(newEvent);
        return eventMapper.toDto(savedEvent);
    }

    @PostMapping("/{masterEventId}")
    @Operation(summary = "Создание нового этапа мероприятия")
    @SecurityRequirement(name = "JWT")
    public EventDto createSubEvent(@PathVariable
                                   @Parameter(description = "Идентификатор родительского мероприятия")
                                   long masterEventId,
                                   @RequestBody @Valid @Parameter(description = "Новый этап мероприятия")
                                   NewSubEventRequest subEventDto) {
        log.info("Добавление нового этапа мероприятия с id '{}'.", masterEventId);
        validateDateRange(subEventDto.getEventStart(), subEventDto.getEventEnd());
        final Event savedSubEvent = eventService.createSubEvent(masterEventId, subEventDto);
        return eventMapper.toDto(savedSubEvent);
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "Обновление данных мероприятия")
    @SecurityRequirement(name = "JWT")
    public EventDto updateEvent(@PathVariable @Parameter(description = "Идентификатор мероприятия") long eventId,
                                @RequestBody @Valid @Parameter(description = "Обновленное мероприятие") EventUpdateRequest updateRequest) {
        log.info("Обновление данных мероприятия c id '{}'.", eventId);
        final Event updatedEvent = eventService.updateEvent(eventId, updateRequest);
        return eventMapper.toDto(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Удаление мероприятия")
    @SecurityRequirement(name = "JWT")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable @Parameter(description = "Идентификатор мероприятия") long eventId) {
        log.info("Обновление данных мероприятия c id '{}'.", eventId);
        eventService.deleteEvent(eventId);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IncorrectEventDatesException("Wrong date range");
            }
        }
    }
}
