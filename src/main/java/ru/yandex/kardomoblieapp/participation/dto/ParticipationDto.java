package ru.yandex.kardomoblieapp.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.ParticipationStatus;

@Builder
@Schema(description = "Заявка на участие в мероприятии")
public record ParticipationDto(@Schema(description = "Идентификатор заявка")
                               long id,
                               @Schema(description = "Идентификатор мероприятия, на которое подана заявка")
                               long eventId,
                               @Schema(description = "Идентификатор пользователя, который подал заявку")
                               long userId,
                               @Schema(description = "Роль пользователя в мероприятии")
                               ParticipantType participantType,
                               @Schema(description = "Статус заявки")
                               ParticipationStatus status,
                               @Schema(description = "Средняя оценка")
                               Double avgRating) {

}
