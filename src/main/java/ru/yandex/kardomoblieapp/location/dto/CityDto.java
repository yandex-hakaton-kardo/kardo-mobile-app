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
@Schema(description = "Город")
public class CityDto {

    @Schema(description = "Идентификатор города")
    private long id;

    @Schema(description = "Название города")
    private String name;

    @Schema(description = "Идентификатор страны, в которой находится город")
    private long countryId;

    @Schema(description = "Идентификатор региона, в котором находится город")
    private long regionId;
}
