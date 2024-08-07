package ru.yandex.kardomoblieapp.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.activity a LEFT JOIN FETCH e.country c " +
            "LEFT JOIN FETCH e.region r LEFT JOIN FETCH e.city ct WHERE e.id = ?1")
    Optional<Event> findFullEventById(long eventId);
}
