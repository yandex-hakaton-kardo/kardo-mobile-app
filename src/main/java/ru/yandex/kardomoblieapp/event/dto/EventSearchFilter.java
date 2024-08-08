package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.event.model.EventType;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Фильтр поиска мероприятий")
public class EventSearchFilter {

    @Schema(description = "Список искомых типов мероприятий")
    private List<EventType> types;

    @Schema(description = "Поиск по названию направления")
    private String activity;

    @Schema(description = "Стартовая дата поиска для начала мероприятия")
    private LocalDate startDate;

    @Schema(description = "Конечная дата поиска для окончания мероприятия")
    private LocalDate endDate;

    @Schema(description = "Поиск по названию или описанию мероприятия")
    private String text;

    @Schema(description = "Поиск по названию страны")
    private String country;

    @Schema(description = "Поиск по названию региона")
    private String region;

    @Schema(description = "Поиск по названию города")
    private String city;

    @Schema(description = "Тип сортировки", defaultValue = "Сортировка по идентификатору мероприятия")
    private EventSort sort;
}
