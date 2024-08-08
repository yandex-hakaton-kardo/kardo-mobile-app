package ru.yandex.kardomoblieapp.participation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationDto;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationUpdateRequest;
import ru.yandex.kardomoblieapp.participation.mapper.ParticipationMapper;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
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

    @PatchMapping("/{participationId}")
    public ParticipationDto updateParticipation(@PathVariable long participationId,
                                                @RequestBody ParticipationUpdateRequest updateRequest,
                                                Principal principal) {
        log.info("Обновленные заявки на участие");
        Participation participation = participationService.updateParticipation(participationId, updateRequest, principal.getName());
        return participationMapper.toDto(participation);
    }

    @DeleteMapping("/{participationId}")
    public void deleteParticipation(@PathVariable long participationId,
                                    Principal principal) {
        log.info("Удаление заявки на участие");
        participationService.deleteParticipation(participationId, principal.getName());
    }

    @GetMapping("/users/{userId}")
    public List<ParticipationDto> findUsersParticipations(@PathVariable long userId,
                                                          @RequestParam ParticipantType type) {
        log.info("Получение заявок пользователя c id '{} и ролью '{}'.", userId, type);
        List<Participation> participations = participationService.findUsersParticipations(userId, type);
        return participationMapper.toDtoList(participations);
    }

    @GetMapping("/{participationId}")
    public ParticipationDto findParticipationById(@PathVariable long participationId) {
        log.info("Получение заявки c id '{}'.", participationId);
        Participation participation = participationService.findParticipationById(participationId);
        return participationMapper.toDto(participation);
    }
}
