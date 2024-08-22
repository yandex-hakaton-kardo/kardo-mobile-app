package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;

@Builder
@Schema(description = "Краткое представление пользователя")
public record ShortUserDto(@Schema(description = "Идентификатор пользователя")
                           long id,
                           @Schema(description = "Никнейм пользователя")
                           String username,
                           @Schema(description = "Фотография профиля")
                           DataFileDto profilePicture) {

}
