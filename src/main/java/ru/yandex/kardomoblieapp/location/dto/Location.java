package ru.yandex.kardomoblieapp.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {

    private Country country;

    private Region region;

    private City city;
}
