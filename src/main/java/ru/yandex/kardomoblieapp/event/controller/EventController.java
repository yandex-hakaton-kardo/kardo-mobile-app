package ru.yandex.kardomoblieapp.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.service.EventService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Мероприятия", description = "Взаимодействие с мероприятиями")
public class EventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @GetMapping("/{eventId}")
    @Operation(summary = "Поиск мероприятия по идентификатору")
    @SecurityRequirement(name = "JWT")
    public EventDto findEventById(@PathVariable @Parameter(description = "Идентификатор мероприятия") long eventId) {
        log.info("Поиск мероприятия с id '{}'.", eventId);
        final Event event = eventService.findEventById(eventId);
        return eventMapper.toDto(event);
    }
}
