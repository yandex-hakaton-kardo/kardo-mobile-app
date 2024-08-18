package ru.yandex.kardomoblieapp.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.kardomoblieapp.event.dto.EventSearchFilter;
import ru.yandex.kardomoblieapp.event.dto.EventSort;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Activity;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.repository.ActivityRepository;
import ru.yandex.kardomoblieapp.event.repository.EventRepository;
import ru.yandex.kardomoblieapp.location.dto.Location;
import ru.yandex.kardomoblieapp.location.service.LocationService;
import ru.yandex.kardomoblieapp.shared.exception.IncorrectEventDatesException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.eventStartInRange;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.eventTypeEquals;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.orderByEventStartDate;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.orderById;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.orderByPrize;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.textInActivityNameIgnoreCase;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.textInCityName;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.textInCountyName;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.textInNameOrDescription;
import static ru.yandex.kardomoblieapp.event.repository.EventSpecification.textInRegionName;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final ActivityRepository activityRepository;

    private final EventMapper eventMapper;

    private final LocationService locationService;

    /**
     * Добавление нового мероприятия.
     *
     * @param newEvent данные о новом мероприятии
     * @return сохраненное мероприятие
     */
    @Override
    @Transactional
    public Event createEvent(NewEventRequest newEvent) {
        final Activity activity = getActivity(newEvent.getActivityId());
        final Event event = new Event();
        eventMapper.createNewEvent(newEvent, event);
        event.setActivity(activity);
        setLocationToEvent(event, newEvent.getCountryId(), newEvent.getRegionId(), newEvent.getCity());
        final Event savedEvent = eventRepository.save(event);
        log.debug("Добавлено новое мероприятие с id '{}'.", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Добавление нового этапа мероприятия.
     *
     * @param masterEventId идентификатор основного мероприятия
     * @param newSubEvent   данные о новом этапе
     * @return сохраненный этап мероприятия
     */
    @Override
    @Transactional
    public Event createSubEvent(long masterEventId, NewSubEventRequest newSubEvent) {
        final Event masterEvent = getEvent(masterEventId);
        final Event subEvent = new Event();
        eventMapper.createSubEvent(newSubEvent, subEvent);
        subEvent.setEventType(masterEvent.getEventType());
        subEvent.setActivity(masterEvent.getActivity());
        subEvent.setMasterEvent(masterEvent);
        setLocationToEvent(subEvent, newSubEvent.getCountryId(), newSubEvent.getRegionId(), newSubEvent.getCity());
        validateSubEventDates(subEvent, masterEvent);
        final Event savedSubEvent = eventRepository.save(subEvent);
        log.debug("Добавлен этап с id '{}' для мероприятия с id '{}'.", savedSubEvent.getId(), masterEventId);
        return savedSubEvent;
    }

    /**
     * Обновление данных о мероприятии.
     *
     * @param eventId     идентификатор мероприятия
     * @param updateEvent обновленные данные о мероприятии
     * @return обновленное мероприятие
     */
    @Override
    @Transactional
    public Event updateEvent(long eventId, EventUpdateRequest updateEvent) {
        final Event event = getEvent(eventId);
        eventMapper.updateEvent(updateEvent, event);
        setLocationToEvent(event, updateEvent.getCountryId(), updateEvent.getRegionId(), updateEvent.getCity());
        final Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено мероприятие с id '{}'.", eventId);
        return updatedEvent;
    }

    /**
     * Удаление мероприятие по идентификатору.
     *
     * @param eventId идентификатор мероприятия
     */
    @Override
    public void deleteEvent(long eventId) {
        getEvent(eventId);
        eventRepository.deleteById(eventId);
        log.info("Удалено мероприятие с id '{}'.", eventId);
    }

    /**
     * Поиск мероприятия по идентификатору.
     *
     * @param eventId идентификатор мероприятия
     * @return найденное мероприятие
     */
    @Override
    public Event findEventById(long eventId) {
        final Event event = getFullEvent(eventId);
        log.debug("Найдено мероприятие с id '{}'.", eventId);
        return event;
    }

    /**
     * Поиск мероприятий по фильтру. Найденные мероприятия возвращаются постранично.
     *
     * @param searchFilter фильтр поиска
     * @param page         номер страницы
     * @param size         количество элементов на странице
     * @return список найденных мероприятий
     */
    @Override
    public List<Event> searchEvents(EventSearchFilter searchFilter, Integer page, Integer size) {
        final Pageable pageable = PageRequest.of(page, size);
        final List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        final Specification<Event> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        final List<Event> events = eventRepository.findAll(getSort(searchFilter.getSort(), resultSpec), pageable).getContent();
        log.debug("Получен список событий размером '{}'.", events.size());
        return events;
    }

    private List<Specification<Event>> eventSearchFilterToSpecifications(EventSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(eventTypeEquals(searchFilter.getTypes()));
        resultSpecification.add(textInActivityNameIgnoreCase(searchFilter.getActivity()));
        resultSpecification.add(eventStartInRange(searchFilter.getStartDate(), searchFilter.getEndDate()));
        resultSpecification.add(textInNameOrDescription(searchFilter.getText()));
        resultSpecification.add(textInCountyName(searchFilter.getCountry()));
        resultSpecification.add(textInRegionName(searchFilter.getRegion()));
        resultSpecification.add(textInCityName(searchFilter.getCity()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Specification<Event> getSort(EventSort eventSort, Specification<Event> spec) {
        if (eventSort == null) {
            return orderById(spec);
        }
        return switch (eventSort) {
            case EVENT_START -> orderByEventStartDate(spec);
            case PRIZE -> orderByPrize(spec);
        };
    }

    private Event getFullEvent(long eventId) {
        return eventRepository.findFullEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id '" + eventId + "' не найдено."));
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id '" + eventId + "' не найдено."));
    }

    private Activity getActivity(long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Направление с id '" + activityId + "' не найдено."));
    }

    private void validateSubEventDates(Event newSubEvent, Event masterEvent) {
        if (newSubEvent.getEventStart().isBefore(masterEvent.getEventStart()) || newSubEvent.getEventEnd().isAfter(masterEvent.getEventEnd())) {
            throw new IncorrectEventDatesException("Этап мероприятия должен быть в рамках главного мероприятия");
        }
    }

    private void setLocationToEvent(Event event, Long countryId, Long regionId, String cityName) {
        final Location location = locationService.getLocation(countryId,
                regionId, cityName);
        if (location.getCountry() != null) {
            event.setCountry(location.getCountry());
        }
        if (location.getRegion() != null) {
            event.setRegion(location.getRegion());
        }
        if (location.getCity() != null) {
            event.setCity(location.getCity());
        }
    }
}
