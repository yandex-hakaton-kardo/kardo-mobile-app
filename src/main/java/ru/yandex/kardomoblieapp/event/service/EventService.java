package ru.yandex.kardomoblieapp.event.service;

import ru.yandex.kardomoblieapp.event.dto.EventUpdateRequest;
import ru.yandex.kardomoblieapp.event.dto.NewEventRequest;
import ru.yandex.kardomoblieapp.event.dto.NewSubEventRequest;
import ru.yandex.kardomoblieapp.event.model.Event;

public interface EventService {
    Event createEvent(NewEventRequest newEvent);

    Event createSubEvent(long masterEventId, NewSubEventRequest newSubEventRequest);

    Event updateEvent(long eventId, EventUpdateRequest eventUpdateRequest);

    void deleteEvent(long eventId);

    Event findEventById(long eventId);
}
