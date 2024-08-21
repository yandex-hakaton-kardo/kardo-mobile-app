package ru.yandex.kardomoblieapp.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Страна")
public record CountryDto(@Schema(description = "Идентификатор страны")
                         long id,
                         @Schema(description = "Название страны")
                         String name) {

}
