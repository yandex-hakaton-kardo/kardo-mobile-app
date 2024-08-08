package ru.yandex.kardomoblieapp.participation.dto;

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
public class ParticipationDto {

    private long id;

    private long eventId;

    private long userId;

    private ParticipantType participantType;

    private ParticipationStatus status;

}
