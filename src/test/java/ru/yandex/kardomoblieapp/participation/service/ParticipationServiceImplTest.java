package ru.yandex.kardomoblieapp.participation.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.model.EventType;
import ru.yandex.kardomoblieapp.event.service.EventService;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

    private Event event;

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
        NewEventRequest newEventRequest = NewEventRequest.builder()
                .activityId(4)
                .eventName("event name")
                .description("event description")
                .eventType(EventType.VIDEO_CONTEST)
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00))
                .prize(100_000)
                .countryId(4L)
                .city("City ")
                .build();
        event = eventService.createEvent(newEventRequest);

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
    }

    @Test
    @DisplayName("Добавление заявки на участие")
    void addParticipation() {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .overview("overview")
                .name("new username")
                .type(ParticipantType.PARTICIPANT)
                .linkToContestFile("link to doc")
                .build();

        Participation participation = participationService.addParticipation(participationRequest, event.getId(), user.getId());

        assertThat(participation, notNullValue());
        assertThat(participation.getId(), greaterThan(0L));
        assertThat(participation.getStatus(), is(ParticipationStatus.PUBLISHED));
        assertThat(user.getName(), is(participationRequest.getName()));
        assertThat(user.getOverview(), is(participationRequest.getOverview()));
        assertThat(user.getEmail(), is("test@mail.ru"));
    }

    @Test
    void changeParticipationStatus() {
    }

    @Test
    void updateParticipation() {
    }

    @Test
    void deleteParticipation() {
    }

    @Test
    void findUsersParticipations() {
    }

    @Test
    void findParticipationById() {
    }
}