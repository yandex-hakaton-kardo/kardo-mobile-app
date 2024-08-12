package ru.yandex.kardomoblieapp.participation.service;

import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationUpdateRequest;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;
import ru.yandex.kardomoblieapp.participation.model.Score;

import java.util.List;

public interface ParticipationService {
    Participation addParticipation(ParticipationRequest participationRequest, long eventId, long userId);

    Participation changeParticipationStatus(long participationId, ParticipationStatus status);

    Participation updateParticipation(long participationId, ParticipationUpdateRequest updateRequest, String username);

    void deleteParticipation(long participationId, String username);

    List<Participation> findUsersParticipations(long userId, ParticipantType type);

    Participation findParticipationById(long participationId);

    Participation rateParticipation(long participationId, Score score, String username);
}
