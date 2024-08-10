package ru.yandex.kardomoblieapp.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Заявка на участие в мероприятии")
public class ParticipationDto {

    @Schema(description = "Идентификатор заявка")
    private long id;

    @Schema(description = "Идентификатор мероприятия, на которое подана заявка")
    private long eventId;

    @Schema(description = "Идентификатор пользователя, который подал заявку")
    private long userId;

    @Schema(description = "Роль пользователя в мероприятии")
    private ParticipantType participantType;

    @Schema(description = "Статус заявки")
    private ParticipationStatus status;

    @Schema(description = "Средняя оценка")
    private double avgRating;
}
