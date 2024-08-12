package ru.yandex.kardomoblieapp.event.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.event.model.EventType;

import java.time.LocalDate;
import java.util.List;

@UtilityClass
public class EventSpecification {

    public static Specification<Event> eventTypeEquals(List<EventType> eventTypes) {
        if (eventTypes == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("eventType")).value(eventTypes);
    }

    public static Specification<Event> textInActivityNameIgnoreCase(String text) {
        if (text == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("activity").get("name")),
                        "%" + text.toLowerCase() + "%");
    }

    public static Specification<Event> eventStartAfter(LocalDate date) {
        if (date == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventStart").as(LocalDate.class), date);
    }

    public static Specification<Event> eventStartInRange(LocalDate startRange, LocalDate endRange) {
        if (endRange == null) {
            return eventStartAfter(startRange);
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventStart"), startRange, endRange);
    }

    public static Specification<Event> textInNameOrDescription(String text) {
        if (text == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("eventName")),
                                "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + text.toLowerCase() + "%")
                );
    }

    public static Specification<Event> textInCountyName(String text) {
        if (text == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("country").get("name")),
                        "%" + text.toLowerCase() + "%");
    }

    public static Specification<Event> textInRegionName(String text) {
        if (text == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("region").get("name")),
                        "%" + text.toLowerCase() + "%");
    }

    public static Specification<Event> textInCityName(String text) {
        if (text == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("city").get("name")),
                        "%" + text.toLowerCase() + "%");
    }

    public static Specification<Event> orderByPrize(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("prize")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<Event> orderById(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("id")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<Event> orderByEventStartDate(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.asc(root.get("eventStart")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
