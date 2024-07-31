package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Место проживания")
public class Location {

    @Schema(description = "Страна проживания")
    private Long countryId;

    @Schema(description = "Регион проживания")
    private Long regionId;

    @Schema(description = "Город проживания")
    @Size(min = 2, max = 20, message = "Название города должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ -]+$", message = "Название города должно содержать от 2 до 20 символов.")
    private String city;
}
