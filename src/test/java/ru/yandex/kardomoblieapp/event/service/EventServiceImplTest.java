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
import ru.yandex.kardomoblieapp.event.dto.EventSearchFilter;
import ru.yandex.kardomoblieapp.event.dto.EventSort;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.model.EventType;
import ru.yandex.kardomoblieapp.shared.exception.IncorrectEventDatesException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private long unknownId;

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
        unknownId = 999L;

        eventUpdateRequest = EventUpdateRequest.builder()
                .eventName("updated name")
                .eventStart(LocalDateTime.of(2025, 1, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2025, 1, 23, 10, 10, 00))
                .countryId(1L)
                .regionId(2L)
                .activityId(2L)
                .eventType(EventType.VIDEO_CONTEST)
                .build();

        newEventRequest = createNewEventRequest(1);

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
    @DisplayName("Создание мероприятия, направление не найдено")
    void createEvent_whenActivityNotFound_shouldThrowNotFoundException() {
        newEventRequest.setActivityId(unknownId);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.createEvent(newEventRequest));

        assertThat(ex.getLocalizedMessage(), is("Направление с id '" + unknownId + "' не найдено."));
    }

    @Test
    @DisplayName("Создание этапа мероприятия")
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
    @DisplayName("Создание этапа мероприятия, главное мероприятие не найдено")
    void createSubEvent_whenMasterEventNotFound_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.createSubEvent(unknownId, newSubEventRequest));

        assertThat(ex.getLocalizedMessage(), is("Мероприятие с id '" + unknownId + "' не найдено."));
    }

    @Test
    @DisplayName("Создание этапа мероприятия, дата начала этапа раньше дата начала главного мероприятия")
    void createSubEvent_whenSubEventStartBeforeMasterEventStart_shouldThrowIncorrectEventDatesException() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        newSubEventRequest.setEventStart(LocalDateTime.of(2022, 12, 12, 11, 00, 00));
        IncorrectEventDatesException ex = assertThrows(IncorrectEventDatesException.class,
                () -> eventService.createSubEvent(savedEvent.getId(), newSubEventRequest));

        assertThat(ex.getLocalizedMessage(), is("Этап мероприятия должен быть в рамках главного мероприятия"));
    }

    @Test
    @DisplayName("Создание этапа мероприятия, дата начала этапа раньше дата начала главного мероприятия")
    void createSubEvent_whenSubEventEndAfterMasterEventEnd_shouldThrowIncorrectEventDatesException() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        newSubEventRequest.setEventEnd(LocalDateTime.of(2025, 12, 12, 11, 00, 00));
        IncorrectEventDatesException ex = assertThrows(IncorrectEventDatesException.class,
                () -> eventService.createSubEvent(savedEvent.getId(), newSubEventRequest));

        assertThat(ex.getLocalizedMessage(), is("Этап мероприятия должен быть в рамках главного мероприятия"));
    }

    @Test
    @DisplayName("Обновление мероприятия")
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
    @DisplayName("Обновление мероприятия, событие не найдено")
    void updateEvent_whenEventNotFound_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.updateEvent(unknownId, eventUpdateRequest));

        assertThat(ex.getLocalizedMessage(), is("Мероприятие с id '" + unknownId + "' не найдено."));
    }

    @Test
    @DisplayName("Удаление мероприятия")
    void deleteEvent() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        eventService.deleteEvent(savedEvent.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.findEventById(savedEvent.getId()));

        assertThat(ex.getMessage(), is("Мероприятие с id '" + savedEvent.getId() + "' не найдено."));
    }

    @Test
    @DisplayName("Удаление мероприятия, событие не найдено")
    void deleteEvent_whenEventNotFound_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.deleteEvent(unknownId));

        assertThat(ex.getMessage(), is("Мероприятие с id '" + unknownId + "' не найдено."));
    }

    @Test
    @DisplayName("Поиск мероприятия по id")
    void findEventById_whenEventExits_shouldReturnEvent() {
        Event savedEvent = eventService.createEvent(newEventRequest);

        Event event = eventService.findEventById(savedEvent.getId());

        assertThat(event, notNullValue());
        assertThat(event.getId(), is(savedEvent.getId()));
        assertThat(event.getEventName(), is(savedEvent.getEventName()));
        assertThat(event.getEventStart(), is(savedEvent.getEventStart()));
    }

    @Test
    @DisplayName("Поиск мероприятия по id, событие не найдено")
    void findEventById_whenEventNotExits_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> eventService.findEventById(unknownId));

        assertThat(ex.getMessage(), is("Мероприятие с id '" + unknownId + "' не найдено."));
    }


    @Test
    @DisplayName("Поиск мероприятий по названию активности")
    void searchEvents_whenFilterForActivityName_shouldReturnEventsWithThatActivityName() {
        eventService.createEvent(newEventRequest);
        NewEventRequest eventRequest = newEventRequest;
        eventRequest.setActivityId(1);
        Event secondEvent = eventService.createEvent(eventRequest);
        EventSearchFilter filter = EventSearchFilter.builder()
                .activity("брейк")
                .build();

        List<Event> events = eventService.searchEvents(filter, 0, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(secondEvent.getId()));
    }

    @Test
    @DisplayName("Поиск мероприятий по типу c сортировкой по дате старта")
    void searchEvents_whenFilterForEventTypeOrderByEventStart_shouldReturnEventsWithThatEventType() {
        Event firstEvent = eventService.createEvent(newEventRequest);
        newEventRequest.setActivityId(1);
        newEventRequest.setEventStart(newEventRequest.getEventStart().plusDays(2));
        Event secondEvent = eventService.createEvent(newEventRequest);
        EventSearchFilter filter = EventSearchFilter.builder()
                .types(List.of(EventType.VIDEO_CONTEST))
                .sort(EventSort.EVENT_START)
                .build();

        List<Event> events = eventService.searchEvents(filter, 0, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(firstEvent.getId()));
        assertThat(events.get(1).getId(), is(secondEvent.getId()));
    }

    @Test
    @DisplayName("Поиск мероприятий по типу c сортировкой по умолчанию")
    void searchEvents_whenFilterForEventType_shouldReturnEventsWithThatEventType() {
        Event firstEvent = eventService.createEvent(newEventRequest);
        newEventRequest.setActivityId(1);
        newEventRequest.setEventStart(newEventRequest.getEventStart().minusDays(2));
        Event secondEvent = eventService.createEvent(newEventRequest);
        EventSearchFilter filter = EventSearchFilter.builder()
                .types(List.of(EventType.VIDEO_CONTEST))
                .build();

        List<Event> events = eventService.searchEvents(filter, 0, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(secondEvent.getId()));
        assertThat(events.get(1).getId(), is(firstEvent.getId()));
    }

    @Test
    @DisplayName("Поиск мероприятий по типу c сортировкой по умолчанию")
    void searchEvents_whenFilterForEventTypeOrderByPrize_shouldReturnEventsWithThatEventType() {
        newEventRequest.setPrize(10);
        Event firstEvent = eventService.createEvent(newEventRequest);
        newEventRequest.setActivityId(1);
        newEventRequest.setPrize(5);
        Event secondEvent = eventService.createEvent(newEventRequest);
        EventSearchFilter filter = EventSearchFilter.builder()
                .types(List.of(EventType.VIDEO_CONTEST))
                .sort(EventSort.PRIZE)
                .build();

        List<Event> events = eventService.searchEvents(filter, 0, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is(firstEvent.getId()));
        assertThat(events.get(1).getId(), is(secondEvent.getId()));
    }

    @Test
    @DisplayName("Поиск мероприятий по типу")
    void searchEvents_whenFilterForEventTypeAndStartDate_shouldReturnEventsWithThatEventTypeAndStartDateAfterDesired() {
        Event firstEvent = eventService.createEvent(newEventRequest);
        NewEventRequest eventRequest = newEventRequest;
        eventRequest.setActivityId(1);
        eventRequest.setEventStart(LocalDateTime.of(2024, 12, 15, 10, 10, 00));
        Event secondEvent = eventService.createEvent(eventRequest);
        EventSearchFilter filter = EventSearchFilter.builder()
                .types(List.of(EventType.VIDEO_CONTEST))
                .startDate(LocalDate.of(2024, 12, 13))
                .build();

        List<Event> events = eventService.searchEvents(filter, 0, 10);

        assertThat(events, notNullValue());
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getId(), is(secondEvent.getId()));
    }


    private NewEventRequest createNewEventRequest(int id) {
        return NewEventRequest.builder()
                .activityId(4)
                .eventName("event name" + id)
                .description("event description" + id)
                .eventType(EventType.VIDEO_CONTEST)
                .eventStart(LocalDateTime.of(2024, 12, 12, 10, 10, 00))
                .eventEnd(LocalDateTime.of(2024, 12, 23, 10, 10, 00))
                .prize(100_000)
                .countryId(4L)
                .city("City " + id)
                .build();
    }
}