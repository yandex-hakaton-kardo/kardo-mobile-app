package ru.yandex.kardomoblieapp.participation.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.model.EventType;
import ru.yandex.kardomoblieapp.event.service.EventService;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationUpdateRequest;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;
import ru.yandex.kardomoblieapp.participation.model.Score;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ParticipationServiceImplTest {

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private User user;

    private User judge;

    private Event event1;

    private Event event2;

    private Event event3;

    private long unknownId = 999L;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_VERSION);

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        event1 = eventService.createEvent(createRequest(1));
        event2 = eventService.createEvent(createRequest(2));
        event3 = eventService.createEvent(createRequest(3));

        User newUser = User.builder()
                .username("username")
                .name("Имя")
                .secondName("Отчество")
                .surname("Фамилия")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        user = userService.createUser(newUser);

        User newJudge = User.builder()
                .username("judge_dredd")
                .name("Judge")
                .secondName("Отчество")
                .surname("Dredd")
                .email("judge_dredd@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        judge = userService.createUser(newJudge);
    }

    @Test
    @DisplayName("Добавление заявки на участие")
    void addParticipation_whenNewParticipation_statusShouldBeCreatedAndRatingIsZero() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();

        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        assertThat(participation, notNullValue());
        assertThat(participation.getId(), greaterThan(0L));
        assertThat(participation.getStatus(), is(ParticipationStatus.CREATED));
        assertThat(participation.getAvgScore(), closeTo(0.0, 0.01));
        assertThat(user.getName(), is(participationRequest.getName()));
        assertThat(user.getOverview(), is(participationRequest.getOverview()));
        assertThat(user.getEmail(), is("test@mail.ru"));
    }

    @Test
    @DisplayName("Добавление заявки на судейство")
    void addParticipation_whenNewJudge_statusShouldBeCreated() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.JUDGE)
                .build();

        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        assertThat(participation, notNullValue());
        assertThat(participation.getId(), greaterThan(0L));
        assertThat(participation.getStatus(), is(ParticipationStatus.CREATED));
        assertThat(participation.getAvgScore(), closeTo(0.0, 0.01));
        assertThat(user.getName(), is(participationRequest.getName()));
        assertThat(user.getOverview(), is(participationRequest.getOverview()));
        assertThat(user.getEmail(), is("test@mail.ru"));
    }

    @Test
    @DisplayName("Добавление заявки спонсора")
    void addParticipation_whenNewSponsor_statusShouldBeCreated() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.SPONSOR)
                .build();

        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        assertThat(participation, notNullValue());
        assertThat(participation.getId(), greaterThan(0L));
        assertThat(participation.getStatus(), is(ParticipationStatus.CREATED));
        assertThat(participation.getAvgScore(), closeTo(0.0, 0.01));
        assertThat(user.getName(), is(participationRequest.getName()));
        assertThat(user.getOverview(), is(participationRequest.getOverview()));
        assertThat(user.getEmail(), is("test@mail.ru"));
    }

    @Test
    @DisplayName("Добавление заявки зрителя")
    void addParticipation_whenParticipatorIsSpectator_statusShouldBeApproved() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.SPECTATOR)
                .build();

        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        assertThat(participation, notNullValue());
        assertThat(participation.getId(), greaterThan(0L));
        assertThat(participation.getStatus(), is(ParticipationStatus.APPROVED));
        assertThat(participation.getAvgScore(), closeTo(0.0, 0.01));
        assertThat(user.getName(), is(participationRequest.getName()));
        assertThat(user.getOverview(), is(participationRequest.getOverview()));
        assertThat(user.getEmail(), is("test@mail.ru"));
    }

    @Test
    @DisplayName("Попытка добавить две заявки на одно мероприятие")
    void addParticipation_whenUserSendsTwoRequestToSameEvent_shouldThrowDataIntegrityViolationException() {
        ParticipationRequest participationRequest1 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();

        participationService.addParticipation(participationRequest1, event1.getId(), user.getId());

        ParticipationRequest participationRequest2 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.JUDGE)
                .build();

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> participationService.addParticipation(participationRequest2, event1.getId(), user.getId()));
    }

    @Test
    @DisplayName("Добавление заявки на участие, событие не найдено")
    void addParticipation_whenEventNotFound_shouldThrowNotFoundException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.addParticipation(participationRequest, unknownId, user.getId()));

        assertThat(ex.getLocalizedMessage(), is("Мероприятие с id '" + unknownId + "' не найдено."));
    }

    @Test
    @DisplayName("Добавление заявки на участие, пользователь не найден")
    void addParticipation_whenUserNotFound_shouldThrowNotFoundException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.addParticipation(participationRequest, event1.getId(), unknownId));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Изменение статуса заявки на ПОДТВЕРЖДЕНО")
    void changeParticipationStatus_whenChangeStatusToApproved_shouldChangeStatusToApproved() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        Participation result = participationService.changeParticipationStatus(participation.getId(), ParticipationStatus.APPROVED);

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(participation.getId()));
        assertThat(result.getStatus(), is(ParticipationStatus.APPROVED));
        assertThat(result.getCreatedOn(), is(participation.getCreatedOn()));
    }

    @Test
    @DisplayName("Изменение статуса заявки на ОТКЛОНЕНА")
    void changeParticipationStatus_whenChangeStatusToDeclined_shouldChangeStatusToDeclined() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        Participation result = participationService.changeParticipationStatus(participation.getId(), ParticipationStatus.DECLINED);

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(participation.getId()));
        assertThat(result.getStatus(), is(ParticipationStatus.DECLINED));
        assertThat(result.getCreatedOn(), is(participation.getCreatedOn()));
    }

    @Test
    @DisplayName("Изменение статуса заявки, заявка не найдена")
    void changeParticipationStatus_whenParticipationNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.changeParticipationStatus(unknownId, ParticipationStatus.APPROVED));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Обновление заявки")
    void updateParticipation_whenUserIsParticipationAuthor_shouldUpdateParticipation() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        ParticipationUpdateRequest updateRequest = ParticipationUpdateRequest.builder()
                .linkToContestFile("new link to doc")
                .build();

        Participation result = participationService.updateParticipation(participation.getId(), updateRequest, user.getUsername());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(participation.getId()));
        assertThat(result.getCreatedOn(), is(participation.getCreatedOn()));
        assertThat(result.getLinkToContestFile(), is(updateRequest.getLinkToContestFile()));
    }

    @Test
    @DisplayName("Обновление заявки, заявка не найдена")
    void updateParticipation_whenParticipationNotFound_shouldThrowNotFoundException() {
        ParticipationUpdateRequest updateRequest = ParticipationUpdateRequest.builder()
                .linkToContestFile("new link to doc")
                .build();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.updateParticipation(unknownId, updateRequest, user.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Обновление заявки, пользователь не найден")
    void updateParticipation_whenUserNotFound_shouldThrowNotFoundException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());
        ParticipationUpdateRequest updateRequest = ParticipationUpdateRequest.builder()
                .linkToContestFile("new link to doc")
                .build();
        String unknownUsername = "unknownUsername";

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.updateParticipation(participation.getId(), updateRequest, unknownUsername));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Обновление заявки, пользователь не имеет прав на редактирование")
    void updateParticipation_whenUserIsNotParticipationAuthor_shouldThrowNotAuthorizedException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());
        ParticipationUpdateRequest updateRequest = ParticipationUpdateRequest.builder()
                .linkToContestFile("new link to doc")
                .build();

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> participationService.updateParticipation(participation.getId(), updateRequest, judge.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Пользователь не имеет прав на редактирование заявки!"));
    }

    @Test
    @DisplayName("Удаление заявки")
    void deleteParticipation_whenParticipationExits_shouldDeleteParticipation() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        participationService.deleteParticipation(participation.getId(), user.getUsername());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.findParticipationById(participation.getId()));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + participation.getId() + "' не найдена."));
    }

    @Test
    @DisplayName("Удаление заявки, заявка не найдена")
    void deleteParticipation_whenParticipationNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.deleteParticipation(unknownId, user.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Удаление заявки, пользователь не найден")
    void deleteParticipation_whenUserNotFound_shouldThrowNotFoundException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());
        String unknownUsername = "unknownUsername";

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.deleteParticipation(participation.getId(), unknownUsername));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Удаление заявки, пользователь не имеет прав на удаление заявки")
    void deleteParticipation_whenUserNotAuthorizedToDelete_shouldThrowNotAuthorizedException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> participationService.deleteParticipation(participation.getId(), judge.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Пользователь не имеет прав на редактирование заявки!"));
    }

    @Test
    @DisplayName("Получение всех заявок пользователя")
    void findUsersParticipations_whenTypeIsNull_shouldReturnAllUsersParticipationsOrderedByEventStartAsc() {
        ParticipationRequest participationRequest1 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation1 = participationService.addParticipation(participationRequest1, event1.getId(), user.getId());
        ParticipationRequest participationRequest2 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.SPECTATOR)
                .linkToContestFile("link to doc")
                .build();
        Participation participation2 = participationService.addParticipation(participationRequest2, event2.getId(), user.getId());
        ParticipationRequest participationRequest3 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.JUDGE)
                .linkToContestFile("link to doc")
                .build();
        Participation participation3 = participationService.addParticipation(participationRequest3, event1.getId(), judge.getId());

        List<Participation> participations = participationService.findUsersParticipations(user.getId(), null);

        assertThat(participations, notNullValue());
        assertThat(participations.size(), is(2));
        assertThat(participations.get(0).getId(), is(participation2.getId()));
        assertThat(participations.get(1).getId(), is(participation1.getId()));
    }

    @Test
    @DisplayName("Получение всех заявок пользователя, когда у пользователя нет заявок")
    void findUsersParticipations_whenUserDoesNotHaveParticipations_shouldReturnEmptyList() {
        List<Participation> participations = participationService.findUsersParticipations(user.getId(), null);

        assertThat(participations, notNullValue());
        assertThat(participations, emptyIterable());
    }

    @Test
    @DisplayName("Получение всех заявок пользователя c ролью УЧАСТНИК")
    void findUsersParticipations_whenTypeIsParticipant_shouldReturnParticipationsWithTypeParticipantOrderedByEventStartAsc() {
        ParticipationRequest participationRequest1 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation1 = participationService.addParticipation(participationRequest1, event1.getId(), user.getId());
        ParticipationRequest participationRequest2 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.SPECTATOR)
                .linkToContestFile("link to doc")
                .build();
        Participation participation2 = participationService.addParticipation(participationRequest2, event2.getId(), user.getId());
        ParticipationRequest participationRequest3 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation3 = participationService.addParticipation(participationRequest3, event3.getId(), user.getId());

        List<Participation> participations = participationService.findUsersParticipations(user.getId(), ParticipantType.PARTICIPANT);

        assertThat(participations, notNullValue());
        assertThat(participations.size(), is(2));
        assertThat(participations.get(0).getId(), is(participation3.getId()));
        assertThat(participations.get(1).getId(), is(participation1.getId()));
    }

    @Test
    @DisplayName("Получение всех заявок пользователя, у которого нет заявок с искомой ролью")
    void findUsersParticipations_whenUserIsNotJudge_shouldReturnEmptyList() {
        ParticipationRequest participationRequest1 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        participationService.addParticipation(participationRequest1, event1.getId(), user.getId());

        List<Participation> participations = participationService.findUsersParticipations(user.getId(), ParticipantType.JUDGE);

        assertThat(participations, notNullValue());
        assertThat(participations, emptyIterable());
    }

    @Test
    @DisplayName("Получение заявки по идентификатору")
    void findParticipationById_whenParticipationExists_ShouldReturnParticipation() {
        ParticipationRequest participationRequest1 = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest1, event1.getId(), user.getId());

        Participation result = participationService.findParticipationById(participation.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(participation.getId()));
        assertThat(result.getLinkToContestFile(), is(participation.getLinkToContestFile()));
        assertThat(result.getCreatedOn(), is(participation.getCreatedOn()));
    }

    @Test
    @DisplayName("Получение заявки по идентификатору, заявка не найдена")
    void findParticipationById_whenParticipationNotExists_ShouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.findParticipationById(unknownId));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Оценка заявки судьей, одинаковые оценки")
    void rateParticipation_whenAll5_shouldReturnAvg5() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        ParticipationRequest judgeRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("i am the law")
                .type(ParticipantType.JUDGE)
                .build();
        participationService.addParticipation(judgeRequest, event1.getId(), judge.getId());

        Score score = Score.builder()
                .scoreType1(5)
                .scoreType2(5)
                .scoreType3(5)
                .build();
        participationService.changeParticipationStatus(participation.getId(), ParticipationStatus.APPROVED);
        Participation ratedParticipation = participationService.rateParticipation(participation.getId(), score, judge.getUsername());


        assertThat(ratedParticipation.getId(), is(participation.getId()));
        assertThat(ratedParticipation.getAvgScore(), closeTo(5.0, 0.1));
    }

    @Test
    @DisplayName("Оценка заявки судьей, разные оценки")
    void rateParticipation_whenAllDifferentRates_shouldReturnAvg() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        ParticipationRequest judgeRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("i am the law")
                .type(ParticipantType.JUDGE)
                .build();
        participationService.addParticipation(judgeRequest, event1.getId(), judge.getId());

        Score score = Score.builder()
                .scoreType1(2)
                .scoreType2(9)
                .scoreType3(5)
                .build();
        participationService.changeParticipationStatus(participation.getId(), ParticipationStatus.APPROVED);
        Participation ratedParticipation = participationService.rateParticipation(participation.getId(), score, judge.getUsername());


        assertThat(ratedParticipation.getAvgScore(), closeTo(5.33, 0.1));
    }

    @Test
    @DisplayName("Оценка неподтвержденной заявки")
    void rateParticipation_whenNotApproved_shouldThrowNotAuthorizedException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        ParticipationRequest judgeRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("i am the law")
                .type(ParticipantType.JUDGE)
                .build();
        participationService.addParticipation(judgeRequest, event1.getId(), judge.getId());

        Score score = Score.builder()
                .scoreType1(2)
                .scoreType2(9)
                .scoreType3(5)
                .build();

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> participationService.rateParticipation(participation.getId(), score, judge.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + judge.getId() + "' не может судить " +
                "мероприятие с id '" + participation.getEvent().getId() + "'."));
    }

    @Test
    @DisplayName("Оценка заявки не судьей")
    void rateParticipation_whenNotUserIsNotJudge_shouldThrowNotAuthorizedException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());

        Score score = Score.builder()
                .scoreType1(2)
                .scoreType2(9)
                .scoreType3(5)
                .build();

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> participationService.rateParticipation(participation.getId(), score, user.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + user.getId() + "' не может судить " +
                "мероприятие с id '" + participation.getEvent().getId() + "'."));
    }

    @Test
    @DisplayName("Оценка заявки, заявка не найдена")
    void rateParticipation_whenParticipationNotFound_shouldThrowNotFoundException() {
        Score score = Score.builder()
                .scoreType1(2)
                .scoreType2(9)
                .scoreType3(5)
                .build();


        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.rateParticipation(unknownId, score, user.getUsername()));

        assertThat(ex.getLocalizedMessage(), is("Заявка на участие с id '" + unknownId + "' не найдена."));
    }

    @Test
    @DisplayName("Оценка заявки, пользователь не найден")
    void rateParticipation_whenUserNotFound_shouldThrowNotFoundException() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();
        Participation participation = participationService.addParticipation(participationRequest, event1.getId(), user.getId());
        Score score = Score.builder()
                .scoreType1(2)
                .scoreType2(9)
                .scoreType3(5)
                .build();
        String unknownUsername = "unknownUsername";

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> participationService.rateParticipation(participation.getId(), score, unknownUsername));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    private NewEventRequest createRequest(int id) {
        return NewEventRequest.builder()
                .activityId(4)
                .eventName("event name" + id)
                .description("event description" + id)
                .eventType(EventType.VIDEO_CONTEST)
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00).minusDays(id))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00).minusDays(id))
                .prize(100_000)
                .countryId(4L)
                .city("City " + id)
                .build();
    }
}