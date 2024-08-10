package ru.yandex.kardomoblieapp.participation.controller;

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

@RestController
@RequestMapping("/admin/participations")
@RequiredArgsConstructor
@Slf4j
public class ParticipationAdminController {

    private final ParticipationService participationService;

    private final ParticipationMapper participationMapper;

    @PutMapping("/{participationId}/status")
    public ParticipationDto changeParticipationStatus(@PathVariable long participationId, ParticipationStatus status) {
        log.info("Подтверждение заявки на участие");
        final Participation participation = participationService.changeParticipationStatus(participationId, status);
        return participationMapper.toDto(participation);
    }
}
