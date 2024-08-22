package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Сущность мероприятия")
public record EventDto(@Schema(description = "Идентификатор мероприятия")
                       Long id,
                       @Schema(description = "Название мероприятия")
                       String eventName,
                       @Schema(description = "Описание мероприятия")
                       String description,
                       @Schema(description = "Дата начала мероприятия")
                       LocalDateTime eventStart,
                       @Schema(description = "Дата окончания мероприятия")
                       LocalDateTime eventEnd,
                       @Schema(description = "Направление мероприятия")
                       String activity,
                       @Schema(description = "Тип мероприятия")
                       String eventType,
                       @Schema(description = "Идентификатор главного мероприятия")
                       long masterEvent,
                       @Schema(description = "Награда мероприятия")
                       int prize,
                       @Schema(description = "Страна проживания")
                       String country,
                       @Schema(description = "Регион страны проживания")
                       String region,
                       @Schema(description = "Город проживания")
                       String city) {

}
