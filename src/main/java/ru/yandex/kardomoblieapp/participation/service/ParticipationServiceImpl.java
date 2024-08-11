package ru.yandex.kardomoblieapp.participation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.service.EventService;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationUpdateRequest;
import ru.yandex.kardomoblieapp.participation.mapper.ParticipationMapper;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;
import ru.yandex.kardomoblieapp.participation.model.Score;
import ru.yandex.kardomoblieapp.participation.repository.ParticipationRepository;
import ru.yandex.kardomoblieapp.participation.repository.ScoreRepository;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.model.UserRole;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static ru.yandex.kardomoblieapp.participation.model.ParticipationStatus.APPROVED;
import static ru.yandex.kardomoblieapp.participation.model.ParticipationStatus.CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;

    private final ScoreRepository scoreRepository;

    private final ParticipationMapper participationMapper;

    private final EventService eventService;

    private final UserService userService;

    /**
     * Создание заявки на участии в мероприятии. Для участников, судей и спонсоров заявка получается статус СОЗДАНА и
     * требует подтверждения со стороны администратора. Для зрителей дополнительного подтверждения не требуется, заявка
     * сразу получает статус ПОДТВЕРЖДЕНА.
     *
     * @param participationRequest заявка на участие
     * @param eventId              идентификатор мероприятия
     * @param userId               идентификатор пользователя
     * @return сохраненная заявка на участие
     */
    @Override
    @Transactional
    public Participation addParticipation(ParticipationRequest participationRequest, long eventId, long userId) {
        final Event event = eventService.findEventById(eventId);
        final User user = userService.findUserById(userId);
        final UserUpdateRequest userUpdateRequest = participationMapper.toUserUpdateRequest(participationRequest);
        userService.updateUser(user.getId(), userUpdateRequest);
        final Participation participation = Participation.builder()
                .event(event)
                .user(user)
                .type(participationRequest.getType())
                .linkToContestFile(participationRequest.getLinkToContestFile())
                .build();
        setAutomaticApprove(participationRequest, participation);
        final Participation savedParticipation = participationRepository.save(participation);
        log.info("Добавлена заявка на участие с id '{}'.", participation.getId());
        return savedParticipation;
    }

    /**
     * Изменение статуса заявки.
     *
     * @param participationId идентификатор заявки на участие
     * @param status          новый статус заявки
     * @return обновленная заявка
     */
    @Override
    @Transactional
    public Participation changeParticipationStatus(long participationId, ParticipationStatus status) {
        final Participation participation = getParticipation(participationId);
        participation.setStatus(status);
        final Participation savedParticipation = participationRepository.save(participation);
        log.info("Статус заявки с id '{}' изменен на '{}'.", participationId, status.name());
        return savedParticipation;
    }

    /**
     * Обновление данных заявки. Пользователь может вносить изменения в заявку до того, как она получит статус ПОДТВЕРЖДЕНА.
     * После получения данного статуса модификация возможно только администратором.
     *
     * @param participationId идентификатор заявки
     * @param updateRequest   новые данные заявки
     * @param username        никнейм пользователя, делающего запрос
     * @return обновленная заявка
     */
    @Override
    @Transactional
    public Participation updateParticipation(long participationId, ParticipationUpdateRequest updateRequest, String username) {
        final Participation participation = getParticipation(participationId);
        final User user = userService.findByUsername(username);
        checkIfUserCanModifyParticipation(participation, user);
        participation.setLinkToContestFile(updateRequest.getLinkToContestFile());
        final Participation updatedParticipation = participationRepository.save(participation);
        log.info("Заявка с id '{}' обновлена.", participationId);
        return updatedParticipation;
    }

    /**
     * Удаление заявки. Пользователь может удалить заявку до того, как она получит статус ПОДТВЕРЖДЕНА. После получения
     * данного статуса удаление возможно только администратором.
     *
     * @param participationId идентификатор заявки
     * @param username        никнейм пользователя, делащего запрос
     */
    @Override
    @Transactional
    public void deleteParticipation(long participationId, String username) {
        final Participation participation = getParticipation(participationId);
        final User user = userService.findByUsername(username);
        checkIfUserCanModifyParticipation(participation, user);
        participationRepository.deleteById(participationId);
        log.info("Заявка с id '{}' удалена.", participationId);
    }

    /**
     * Поиск заявок пользователя. Может быть указана роль пользователя в заявках. Если роль не указана, возвращаются все
     * заявки пользователя.
     *
     * @param userId идентификатор пользователя
     * @param type   роль пользователя в мероприятии
     * @return список заявок пользователя
     */
    @Override
    public List<Participation> findUsersParticipations(long userId, ParticipantType type) {
        userService.findUserById(userId);
        List<Participation> participations;
        if (type == null) {
            participations = participationRepository.findUsersParticipations(userId);
        } else {
            participations = participationRepository.findParticipationsByUserIdAndType(userId, type);
        }
        log.info("Получен список заявок пользователя с id '{}'.", userId);
        return participations;
    }

    /**
     * Поиск заявки на участие по идентификатору.
     *
     * @param participationId идентификатор заявки
     * @return найденная заявка
     */
    @Override
    public Participation findParticipationById(long participationId) {
        final Participation participation = getParticipation(participationId);
        log.info("Получена заявка с id '{}'.", participationId);
        return participation;
    }

    /**
     * Установка оценки заявки судьей. Общая оценка формируется как средняя из трех пунктов, которые подлежат оценки.
     *
     * @param participationId идентификатор заявки
     * @param score           оценка
     * @param username        никнейм пользователя, выставляющего оценку
     * @return оцененная заявка
     */
    @Override
    @Transactional
    public Participation rateParticipation(long participationId, Score score, String username) {
        final Participation participation = getParticipation(participationId);
        final User judge = userService.findByUsername(username);
        checkIfUserCanRateParticipation(participation, judge);
        score.setParticipation(participation);
        score.setJudge(judge);
        scoreRepository.save(score);
        participation.setAvgScore(scoreRepository.findAvgRatingOfParticipation(participationId));
        final Participation result = participationRepository.save(participation);
        log.info("Оценка для заявки с id '{}' сохранена.", participationId);
        return result;
    }

    private void checkIfUserCanRateParticipation(Participation participation, User user) {
        Optional<Participation> userParticipation = participationRepository.findByEventIdUserIdAndParticipantType(participation.getEvent().getId(),
                user.getId(), ParticipantType.JUDGE);
        if (userParticipation.isEmpty() || !participation.getStatus().equals(APPROVED)) {
            throw new NotAuthorizedException("Пользователь с id '" + user.getId() + "' не может судить " +
                    "мероприятие с id '" + participation.getEvent().getId() + "'.");
        }
    }

    private void checkIfUserCanModifyParticipation(Participation participation, User user) {
        if (!user.getRole().equals(UserRole.ADMIN)) {
            if (!participation.getUser().getId().equals(user.getId()) || !participation.getStatus().equals(CREATED)) {
                throw new NotAuthorizedException("Пользователь не имеет прав на редактирование заявки!");
            }
        }
    }

    private Participation getParticipation(long participationId) {
        return participationRepository.findFullParticipationById(participationId)
                .orElseThrow(() -> new NotFoundException("Заявка на участие с id '" + participationId + "' не найдена."));
    }

    private void setAutomaticApprove(ParticipationRequest participationRequest, Participation participation) {
        switch (participationRequest.getType()) {
            case SPECTATOR -> participation.setStatus(APPROVED);
            case JUDGE, SPONSOR, PARTICIPANT -> participation.setStatus(CREATED);
        }
    }
}
