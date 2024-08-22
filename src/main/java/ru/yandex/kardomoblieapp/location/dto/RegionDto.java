package ru.yandex.kardomoblieapp.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Регион в стране")
public record RegionDto(@Schema(description = "Идентификатор региона")
                        long id,
                        @Schema(description = "Название региона")
                        String name,
                        @Schema(description = "Идентификатор страны, в которой находится регион")
                        long countryId) {

}
