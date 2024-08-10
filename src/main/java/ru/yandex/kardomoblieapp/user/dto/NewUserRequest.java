package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Запрос на добавление нового пользователя")
public class NewUserRequest {

    @NotBlank(message = "Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")
    @Pattern(regexp = "^[a-z0-9]{2,30}$", message = "Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")
    @Schema(description = "Никнейм пользователя", minLength = 2, maxLength = 30)
    private String username;

    @NotBlank(message = "Некорректный формат электронной почты.")
    @Email(regexp = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$", message = "Некорректный формат электронной почты.")
    @Size(min = 5, max = 50, message = "Некорректный формат электронной почты.")
    @Schema(description = "Электронная почта")
    private String email;


    @NotBlank(message = "Пароль не может быть пустым и должен содержать от 6 до 15 символов.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*?])[a-zA-Z0-9!@#$%^&*?]{6,15}$",
            message = "Пароль не может быть пустым и должен содержать от 6 до 15 символов.")
    @Schema(description = "Пароль", minLength = 6, maxLength = 15)
    private String password;
}
