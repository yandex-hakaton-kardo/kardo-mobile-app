package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Сущность направления")
public record ActivityDto(@Schema(description = "Идентификатор направления")
                          Long id,
                          @Schema(description = "Название направления")
                          String name,
                          @Schema(description = "Описание направления")
                          String description) {

}
