package ru.yandex.kardomoblieapp.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Activity;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.repository.ActivityRepository;
import ru.yandex.kardomoblieapp.event.repository.EventRepository;
import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;
import ru.yandex.kardomoblieapp.location.service.LocationService;
import ru.yandex.kardomoblieapp.shared.exception.IncorrectEventDatesException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.LocationInfo;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final ActivityRepository activityRepository;

    private final EventMapper eventMapper;

    private final LocationService locationService;

    @Override
    public Event createEvent(NewEventRequest newEvent) {
        final Activity activity = getActivity(newEvent.getActivityId());
        final Event event = new Event();
        eventMapper.createNewEvent(newEvent, event);
        event.setActivity(activity);
        final LocationInfo locationInfo = LocationInfo.builder()
                .countryId(newEvent.getCountryId())
                .regionId(newEvent.getRegionId())
                .city(newEvent.getCity())
                .build();
        setLocationToEvent(locationInfo, event);
        final Event savedEvent = eventRepository.save(event);
        log.info("Добавлено новое мероприятие с id '{}'.", savedEvent.getId());
        return savedEvent;
    }

    @Override
    @Transactional
    public Event createSubEvent(long masterEventId, NewSubEventRequest newSubEvent) {
        final Event masterEvent = getEvent(masterEventId);
        final Event subEvent = new Event();
        eventMapper.createSubEvent(newSubEvent, subEvent);
        subEvent.setEventType(masterEvent.getEventType());
        subEvent.setActivity(masterEvent.getActivity());
        subEvent.setMasterEvent(masterEvent);
        final LocationInfo locationInfo = LocationInfo.builder()
                .countryId(newSubEvent.getCountryId())
                .regionId(newSubEvent.getRegionId())
                .city(newSubEvent.getCity())
                .build();
        setLocationToEvent(locationInfo, subEvent);
        validateSubEventDates(subEvent, masterEvent);
        final Event savedSubEvent = eventRepository.save(subEvent);
        log.info("Добавлен этап с id '{}' для мероприятия с id '{}'.", savedSubEvent.getId(), masterEventId);
        return savedSubEvent;
    }

    @Override
    @Transactional
    public Event updateEvent(long eventId, EventUpdateRequest updateEvent) {
        final Event event = getEvent(eventId);
        eventMapper.updateEvent(updateEvent, event);
        final LocationInfo locationInfo = LocationInfo.builder()
                .countryId(updateEvent.getCountryId())
                .regionId(updateEvent.getRegionId())
                .city(updateEvent.getCity())
                .build();
        setLocationToEvent(locationInfo, event);
        final Event updatedEvent = eventRepository.save(event);
        log.info("Обновлено мероприятие с id '{}'.", eventId);
        return updatedEvent;
    }

    @Override
    public void deleteEvent(long eventId) {
        getEvent(eventId);
        eventRepository.deleteById(eventId);
        log.info("Удалено мероприятие с id '{}'.", eventId);
    }

    @Override
    public Event findEventById(long eventId) {
        Event event = getFullEvent(eventId);
        log.info("Найдено мероприятие с id '{}'.", eventId);
        return event;
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

    private void setLocationToEvent(LocationInfo locationInfo, Event event) {
        if (locationInfo.getCountryId() != null) {
            Country country = locationService.getCountryById(locationInfo.getCountryId());
            event.setCountry(country);
        }

        if (locationInfo.getRegionId() != null) {
            Region region = locationService.getRegionById(locationInfo.getRegionId());
            event.setRegion(region);
        }

        if (locationInfo.getCity() != null) {
            Optional<City> city = locationService.findCityByNameCountryAndRegion(locationInfo.getCity(),
                    event.getCountry() != null ? event.getCountry().getId() : null,
                    event.getRegion() != null ? event.getRegion().getId() : null);

            if (city.isPresent()) {
                event.setCity(city.get());
            } else {
                City newCity = City.builder()
                        .name(locationInfo.getCity())
                        .country(event.getCountry())
                        .region(event.getRegion())
                        .build();
                City savedCity = locationService.addCity(newCity);
                event.setCity(savedCity);
            }
        }
    }
}
