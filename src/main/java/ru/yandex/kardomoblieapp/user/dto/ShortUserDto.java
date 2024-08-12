package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Краткое представление пользователя")
public class ShortUserDto {

    @Schema(description = "Идентификатор пользователя")
    private long id;

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Фотография профиля")
    private DataFileDto profilePicture;
}
