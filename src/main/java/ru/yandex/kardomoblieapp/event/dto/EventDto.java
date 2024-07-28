package ru.yandex.kardomoblieapp.event.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import ru.yandex.kardomoblieapp.event.model.Event;

import java.time.LocalDateTime;
import java.util.Set;

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
    private Set<Event> subEvents;
    private String prize;

}
