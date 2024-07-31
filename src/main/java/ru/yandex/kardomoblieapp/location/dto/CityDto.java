package ru.yandex.kardomoblieapp.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityDto {

    private long id;

    private String name;

    private long countryId;

    private long regionId;
}
