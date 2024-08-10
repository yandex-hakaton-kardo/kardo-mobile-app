package ru.yandex.kardomoblieapp.participation.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя в мероприятии")
public enum ParticipantType {
    @Schema(description = "Участник мероприятия")
    PARTICIPANT,
    @Schema(description = "Судья мероприятия")
    JUDGE,
    @Schema(description = "Зритель мероприятия")
    SPECTATOR,
    @Schema(description = "Спонсор мероприятия")
    SPONSOR
}
