package ru.yandex.kardomoblieapp.event.service;

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
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.model.EventType;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class EventServiceImplTest {

    @Autowired
    private EventService eventService;

    private Event event;

    private EventUpdateRequest eventUpdateRequest;

    private NewEventRequest newEventRequest;

    private NewSubEventRequest newSubEventRequest;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .eventName("event name")
                .description("event description")
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00))
                .prize(100_000)
                .build();

        eventUpdateRequest = EventUpdateRequest.builder()
                .eventName("updated name")
                .eventStart(LocalDateTime.of(2025, 1, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2025, 1, 23, 10, 10, 00))
                .countryId(1L)
                .regionId(2L)
                .activityId(2L)
                .eventType(EventType.VIDEO_CONTEST)
                .build();

        newEventRequest = NewEventRequest.builder()
                .activityId(4)
                .eventName("event name")
                .description("event description")
                .eventType(EventType.VIDEO_CONTEST)
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00))
                .prize(100_000)
                .countryId(4L)
                .city("City 1")
                .activityId(2L)
                .build();

        newSubEventRequest = NewSubEventRequest.builder()
                .eventName("event name")
                .description("event description")
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00))
                .countryId(2L)
                .city("City 3")
                .build();
    }

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

    @Test
    @DisplayName("Создание мероприятия")
    void createEvent_shouldReturnEventWithPositiveId() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        assertThat(savedEvent, notNullValue());
        assertThat(savedEvent.getId(), greaterThan(0L));
        assertThat(savedEvent.getActivity().getId(), is(newEventRequest.getActivityId()));
        assertThat(savedEvent.getCountry().getId(), is(newEventRequest.getCountryId()));
        assertThat(savedEvent.getCity().getName(), is(newEventRequest.getCity()));
    }

    @Test
    @DisplayName("Создание этапа события")
    void createSubEvent_shouldReturnEventWithPositiveIdAndMasterId() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        Event subEvent = eventService.createSubEvent(savedEvent.getId(), newSubEventRequest);

        assertThat(subEvent, notNullValue());
        assertThat(subEvent.getId(), greaterThan(savedEvent.getId()));
        assertThat(subEvent.getMasterEvent().getId(), is(savedEvent.getId()));
        assertThat(subEvent.getEventType(), is(savedEvent.getEventType()));
        assertThat(subEvent.getEventName(), is(newSubEventRequest.getEventName()));
        assertThat(subEvent.getEventStart(), is(newSubEventRequest.getEventStart()));
        assertThat(subEvent.getCity().getName(), is(newSubEventRequest.getCity()));
    }

    @Test
    @DisplayName("Обновление события")
    void updateEvent_shouldUpdateNotNullFields() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        Event updatedEvent = eventService.updateEvent(savedEvent.getId(), eventUpdateRequest);

        assertThat(updatedEvent, notNullValue());
        assertThat(updatedEvent.getId(), is(savedEvent.getId()));
        assertThat(updatedEvent.getCity().getName(), is(savedEvent.getCity().getName()));
        assertThat(updatedEvent.getEventName(), is(eventUpdateRequest.getEventName()));
        assertThat(updatedEvent.getCountry().getId(), is(eventUpdateRequest.getCountryId()));
        assertThat(updatedEvent.getEventStart(), is(eventUpdateRequest.getEventStart()));
    }

    @Test
    @DisplayName("Удаление события")
    void deleteEvent() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        eventService.deleteEvent(savedEvent.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.findEventById(savedEvent.getId()));

        assertThat(ex.getMessage(), is("Мероприятие с id '" + savedEvent.getId() + "' не найдено."));
    }
}