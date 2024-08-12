package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Данные аутентификации пользователя")
public class UserLoginDetails {

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Пароль пользователя")
    private String password;
}
