package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Ответ на регистрацию пользователя")
public record NewUserResponse(@Schema(description = "Идентификатор пользователя")
                              long id,
                              @Schema(description = "Никнейм пользователя")
                              String username,
                              @Schema(description = "Электронная почта")
                              String email) {

}
