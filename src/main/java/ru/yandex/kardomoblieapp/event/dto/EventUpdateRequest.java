package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.event.model.EventType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Обновление мероприятия")
public class EventUpdateRequest {

    @Schema(description = "Название мероприятия")
    @Size(min = 5, max = 100, message = "Название мероприятия не может быть пустым и должно содержать от 5 до 100 символов")
    private String eventName;

    @Schema(description = "Описание мероприятия")
    @Size(min = 5, max = 1000, message = "Описание мероприятия не может быть пустым и должно содержать от 5 до 1000 символов")
    private String description;

    @Schema(description = "Дата начала мероприятия")
    private LocalDateTime eventStart;

    @Schema(description = "Дата окончания мероприятия")
    private LocalDateTime eventEnd;

    @Schema(description = "Идентификатор направления мероприятия")
    private long activityId;

    @Schema(description = "Тип мероприятия")
    private EventType eventType;

    @Schema(description = "Призовой фонд")
    @NotNull(message = "Призовой фонд должен быть больше нуля")
    private int prize;

    @Schema(description = "Страна проживания")
    private Long countryId;

    @Schema(description = "Регион проживания")
    private Long regionId;

    @Schema(description = "Город проживания")
    @Size(min = 2, max = 20, message = "Название города должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ -]+$", message = "Название города должно содержать от 2 до 20 символов.")
    private String city;
}
