package ru.yandex.kardomoblieapp.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Страна")
public class CountryDto {

    @Schema(description = "Идентификатор страны")
    private long id;

    @Schema(description = "Название страны")
    private String name;
}
