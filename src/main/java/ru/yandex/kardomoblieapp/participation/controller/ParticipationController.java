package ru.yandex.kardomoblieapp.participation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.participation.dto.NewScoreRequest;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationDto;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationUpdateRequest;
import ru.yandex.kardomoblieapp.participation.mapper.ParticipationMapper;
import ru.yandex.kardomoblieapp.participation.mapper.ScoreMapper;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.Score;
import ru.yandex.kardomoblieapp.participation.service.ParticipationService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/participations")
@RequiredArgsConstructor
@Slf4j
public class ParticipationController {

    private final ParticipationService participationService;

    private final ParticipationMapper participationMapper;

    private final ScoreMapper scoreMapper;

    @PatchMapping("/{participationId}")
    @Operation(summary = "Обновление заявки на участие")
    @SecurityRequirement(name = "JWT")
    public ParticipationDto updateParticipation(@PathVariable
                                                @Parameter(description = "Идентификатор заявки")
                                                long participationId,
                                                @RequestBody
                                                @Parameter(description = "Запрос обновления заявки")
                                                ParticipationUpdateRequest updateRequest,
                                                @Parameter(hidden = true) Principal principal) {
        log.info("Обновленные заявки на участие");
        final Participation participation = participationService.updateParticipation(participationId, updateRequest, principal.getName());
        return participationMapper.toDto(participation);
    }

    @DeleteMapping("/{participationId}")
    @Operation(summary = "Удаление заявки на участие")
    @SecurityRequirement(name = "JWT")
    public void deleteParticipation(@PathVariable @Parameter(description = "Идентификатор заявки") long participationId,
                                    @Parameter(hidden = true) Principal principal) {
        log.info("Удаление заявки на участие");
        participationService.deleteParticipation(participationId, principal.getName());
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Поиск заявок пользователя")
    @SecurityRequirement(name = "JWT")
    public List<ParticipationDto> findUsersParticipations(@PathVariable
                                                          @Parameter(description = "Идентификатор пользователя")
                                                          long userId,
                                                          @RequestParam(required = false)
                                                          @Parameter(description = "Роль пользователя в мероприятии")
                                                          ParticipantType type) {
        log.info("Получение заявок пользователя c id '{} и ролью '{}'.", userId, type);
        final List<Participation> participations = participationService.findUsersParticipations(userId, type);
        return participationMapper.toDtoList(participations);
    }

    @GetMapping("/{participationId}")
    @Operation(summary = "Поиск заявки по идентификатору")
    @SecurityRequirement(name = "JWT")
    public ParticipationDto findParticipationById(@Parameter(description = "Идентификатор заявки")
                                                  @PathVariable long participationId) {
        log.info("Получение заявки c id '{}'.", participationId);
        final Participation participation = participationService.findParticipationById(participationId);
        return participationMapper.toDto(participation);
    }

    @PostMapping("/{participationId}/score")
    @Operation(summary = "Оценка заявки судьей")
    @SecurityRequirement(name = "JWT")
    public ParticipationDto rateParticipation(@Parameter(description = "Идентификатор заявки")
                                              @PathVariable long participationId,
                                              @Parameter(description = "Запрос на установку оценки заявки")
                                              @RequestBody @Valid NewScoreRequest newScoreRequest,
                                              @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь '{}' ставит оценку заявке с id '{}.", principal.getName(), participationId);
        final Score score = scoreMapper.toModel(newScoreRequest);
        final Participation ratedParticipation = participationService.rateParticipation(participationId, score,
                principal.getName());
        return participationMapper.toDto(ratedParticipation);
    }
}
