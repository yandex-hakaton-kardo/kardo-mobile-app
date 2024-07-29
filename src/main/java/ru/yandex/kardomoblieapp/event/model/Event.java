package ru.yandex.kardomoblieapp.event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import java.util.List;


import java.time.LocalDateTime;

//@Entity
//@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Event {
    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    private String description;

    @Column(name = "start_date")
    private LocalDateTime eventStart;

    @Column(name = "end_date")
    private LocalDateTime eventEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @Formula("(SELECT a.name FROM activities a WHERE a.activity_id = activity_id)")
    private String activity;

    @OneToOne(fetch = FetchType.LAZY)
    @Formula("(SELECT et.name FROM event_types et WHERE et.event_type_id = event_type_id)")
    private String eventType;

    @OneToMany(mappedBy = "masterEvent")
    private List<Event> subEvents;

    @JoinColumn(name = "prize")
    private String prize;
}
