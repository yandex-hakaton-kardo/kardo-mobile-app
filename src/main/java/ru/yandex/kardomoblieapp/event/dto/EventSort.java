package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Виды сортировок мероприятий")
public enum EventSort {
    @Schema(description = "Сортировка по дате начала событий")
    EVENT_START,
    @Schema(description = "Сортировка по размеру призового фонда")
    PRIZE
}
