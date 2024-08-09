package ru.yandex.kardomoblieapp.participation.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус заявки")
public enum ParticipationStatus {
    @Schema(description = "Заявка создана")
    CREATED,
    @Schema(description = "Заявка подтверждена")
    APPROVED,
    @Schema(description = "Заявка отклонена")
    DECLINED
}
