package ru.yandex.kardomoblieapp.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Город")
public record CityDto(@Schema(description = "Идентификатор города")
                      long id,
                      @Schema(description = "Название города")
                      String name,
                      @Schema(description = "Идентификатор страны, в которой находится город")
                      long countryId,
                      @Schema(description = "Идентификатор региона, в котором находится город")
                      long regionId) {

}
