package ru.yandex.kardomoblieapp.event.service;

import ru.yandex.kardomoblieapp.event.dto.EventSearchFilter;
import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;

import java.util.List;

public interface EventService {
    Event createEvent(NewEventRequest newEvent);

    Event createSubEvent(long masterEventId, NewSubEventRequest newSubEventRequest);

    Event updateEvent(long eventId, EventUpdateRequest eventUpdateRequest);

    void deleteEvent(long eventId);

    Event findEventById(long eventId);

    List<Event> searchEvents(EventSearchFilter searchFilter, Integer page, Integer size);
}
