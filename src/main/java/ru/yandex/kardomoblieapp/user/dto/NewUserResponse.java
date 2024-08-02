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
@Schema(description = "Ответ на регистрацию пользователя")
public class NewUserResponse {

    @Schema(description = "Идентификатор пользователя")
    private long id;

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Электронная почта")
    private String email;
}
