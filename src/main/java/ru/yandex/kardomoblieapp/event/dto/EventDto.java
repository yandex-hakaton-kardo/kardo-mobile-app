package ru.yandex.kardomoblieapp.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDto {

    private Long id;
    private String eventName;
    private String description;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
    private String activity;
    private String eventType;
    private Event masterEvent;
    private List<Event> subEvents;
    private String prize;

}
