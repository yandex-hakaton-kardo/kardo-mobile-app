package ru.yandex.kardomoblieapp.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
import ru.yandex.kardomoblieapp.shared.exception.ErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Мероприятие найдено", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EventDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Мероприятие не найдено", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @SecurityRequirement(name = "JWT")
    public EventDto findEventById(@PathVariable @Parameter(description = "Идентификатор мероприятия") long eventId) {
        log.info("Поиск мероприятия с id '{}'.", eventId);
        final Event event = eventService.findEventById(eventId);
        return eventMapper.toDto(event);
    }

    @GetMapping
    @Operation(summary = "Поиск мероприятий")
    @SecurityRequirement(name = "JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список мероприятий получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список направлений получен", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ActivityDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<ActivityDto> findAllActivities() {
        log.info("Получение списка направлений");
        final List<Activity> activities = activityService.findAll();
        return activityMapper.toDtoList(activities);
    }

    @PostMapping("/{eventId}/participation/{userId}")
    @Operation(summary = "Добавление заявки на участие в мероприятии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка успешно создана", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipationDto.class))}),
            @ApiResponse(responseCode = "400", description = "Введены некорректные обновленные данные заявки", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Мероприятие не найдено", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
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
