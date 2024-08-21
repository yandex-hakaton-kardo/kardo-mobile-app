package ru.yandex.kardomoblieapp.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на установку оценки заявки")
public record NewScoreRequest(
        @Schema(description = "Первая оценка")
        @Min(value = 1, message = "Оценка должна быть больше 0")
        @Max(value = 10, message = "Оценка должна быть не больше 10")
        @NotNull(message = "Оценка должна быть проставлена")
        Long scoreType1,
        @Schema(description = "Вторая оценка")
        @Min(value = 1, message = "Оценка должна быть больше 0")
        @Max(value = 10, message = "Оценка должна быть не больше 10")
        @NotNull(message = "Оценка должна быть проставлена")
        Long scoreType2,
        @Schema(description = "Третья оценка")
        @Min(value = 1, message = "Оценка должна быть больше 0")
        @Max(value = 10, message = "Оценка должна быть не больше 10")
        @NotNull(message = "Оценка должна быть проставлена")
        Long scoreType3) {

}
