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
@Schema(description = "Регион в стране")
public class RegionDto {

    @Schema(description = "Идентификатор региона")
    private long id;

    @Schema(description = "Название региона")
    private String name;

    @Schema(description = "Идентификатор страны, в которой находится регион")
    private long countryId;
}
