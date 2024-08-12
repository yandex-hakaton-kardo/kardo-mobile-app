package ru.yandex.kardomoblieapp.participation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationDto;
import ru.yandex.kardomoblieapp.participation.mapper.ParticipationMapper;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;
import ru.yandex.kardomoblieapp.participation.service.ParticipationService;
import ru.yandex.kardomoblieapp.shared.exception.ErrorResponse;

@RestController
@RequestMapping("/admin/participations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Заявки на участие в мероприятиях", description = "Администрирование заявок")
public class ParticipationAdminController {

    private final ParticipationService participationService;

    private final ParticipationMapper participationMapper;

    @PutMapping("/{participationId}/status")
    @Operation(summary = "Изменение статуса заявки администратором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заявки успешно изменен", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipationDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public ParticipationDto changeParticipationStatus(@Parameter(description = "Идентификатор заявки")
                                                      @PathVariable long participationId,
                                                      @Parameter(description = "Новый статус заявки")
                                                      ParticipationStatus status) {
        log.info("Подтверждение заявки на участие");
        final Participation participation = participationService.changeParticipationStatus(participationId, status);
        return participationMapper.toDto(participation);
    }
}
