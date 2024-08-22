package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.event.model.EventType;

import java.time.LocalDate;
import java.util.List;

@Builder
@Schema(description = "Фильтр поиска мероприятий")
public record EventSearchFilter(@Schema(description = "Список искомых типов мероприятий")
                                List<EventType> types,
                                @Schema(description = "Поиск по названию направления")
                                String activity,
                                @Schema(description = "Стартовая дата поиска для начала мероприятия")
                                LocalDate startDate,
                                @Schema(description = "Конечная дата поиска для окончания мероприятия")
                                LocalDate endDate,
                                @Schema(description = "Поиск по названию или описанию мероприятия")
                                String text,
                                @Schema(description = "Поиск по названию страны")
                                String country,
                                @Schema(description = "Поиск по названию региона")
                                String region,
                                @Schema(description = "Поиск по названию города")
                                String city,
                                @Schema(description = "Тип сортировки",
                                        defaultValue = "Сортировка по идентификатору мероприятия")
                                EventSort sort) {

}
