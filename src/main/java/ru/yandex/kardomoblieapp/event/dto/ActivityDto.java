package ru.yandex.kardomoblieapp.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Сущность направления")
public class ActivityDto {

    @Schema(description = "Идентификатор направления")
    private Long id;

    @Schema(description = "Название направления")
    private String name;

    @Schema(description = "Описание направления")
    private String description;

}
