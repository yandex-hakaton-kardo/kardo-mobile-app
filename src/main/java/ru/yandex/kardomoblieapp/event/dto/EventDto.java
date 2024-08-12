package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Сущность мероприятия")
public class EventDto {

    @Schema(description = "Идентификатор мероприятия")
    private Long id;

    @Schema(description = "Название мероприятия")
    private String eventName;

    @Schema(description = "Описание мероприятия")
    private String description;

    @Schema(description = "Дата начала мероприятия")
    private LocalDateTime eventStart;

    @Schema(description = "Дата окончания мероприятия")
    private LocalDateTime eventEnd;

    @Schema(description = "Направление мероприятия")
    private String activity;

    @Schema(description = "Тип мероприятия")
    private String eventType;

    @Schema(description = "Идентификатор главного мероприятия")
    private long masterEvent;

    @Schema(description = "Награда мероприятия")
    private int prize;

    @Schema(description = "Страна проживания")
    private String country;

    @Schema(description = "Регион страны проживания")
    private String region;

    @Schema(description = "Город проживания")
    private String city;
}
