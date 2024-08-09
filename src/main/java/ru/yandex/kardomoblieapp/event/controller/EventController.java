package ru.yandex.kardomoblieapp.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.event.dto.ActivityDto;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.dto.EventSearchFilter;
import ru.yandex.kardomoblieapp.event.mapper.ActivityMapper;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Activity;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.service.ActivityService;
import ru.yandex.kardomoblieapp.event.service.EventService;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationDto;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.mapper.ParticipationMapper;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Мероприятия", description = "Взаимодействие с мероприятиями")
public class EventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    private final ActivityMapper activityMapper;

    private final ActivityService activityService;

    private final ParticipationService participationService;

    private final ParticipationMapper participationMapper;

    @GetMapping("/{eventId}")
    @Operation(summary = "Поиск мероприятия по идентификатору")
    @SecurityRequirement(name = "JWT")
    public EventDto findEventById(@PathVariable @Parameter(description = "Идентификатор мероприятия") long eventId) {
        log.info("Поиск мероприятия с id '{}'.", eventId);
        final Event event = eventService.findEventById(eventId);
        return eventMapper.toDto(event);
    }

    @GetMapping
    @Operation(summary = "Поиск мероприятий")
    @SecurityRequirement(name = "JWT")
    public List<EventDto> searchEvents(@Parameter(description = "Фильтр поиска") EventSearchFilter searchFilter,
                                       @RequestParam(defaultValue = "0")
                                       @Parameter(description = "Номер страницы") Integer page,
                                       @RequestParam(defaultValue = "10")
                                       @Parameter(description = "Количество постов на странице") Integer size) {
        log.info("Поиск мероприятий.");
        final List<Event> events = eventService.searchEvents(searchFilter, page, size);
        return eventMapper.toDtoList(events);
    }

    @GetMapping("/activities")
    @Operation(summary = "Получение списка всех направлений")
    @SecurityRequirement(name = "JWT")
    public List<ActivityDto> findAllActivities() {
        log.info("Получение списка направлений");
        final List<Activity> activities = activityService.findAll();
        return activityMapper.toDtoList(activities);
    }

    @PostMapping("/{eventId}/participation/{userId}")
    @Operation(summary = "Добавление заявки на участие в мероприятии")
    public ParticipationDto addParticipation(@Parameter(description = "Заявка на участие")
                                             @RequestBody ParticipationRequest participationRequest,
                                             @Parameter(description = "Идентификатор мероприятия")
                                             @PathVariable long eventId,
                                             @Parameter(description = "Идентификатор пользователя")
                                             @PathVariable long userId) {
        log.info("Пользователь с id '{} оставляет заявку на участие в мероприятии с id '{}'.", userId, eventId);
        Participation participation = participationService.addParticipation(participationRequest, eventId, userId);
        return participationMapper.toDto(participation);
    }
}
