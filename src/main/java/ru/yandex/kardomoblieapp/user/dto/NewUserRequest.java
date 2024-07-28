package ru.yandex.kardomoblieapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.shared.validation.Censored;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewUserRequest {

    @NotBlank(message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @Size(min = 2, max = 20, message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @Censored
    private String username;

    @NotBlank(message = "Некорректный формат электронной почты.")
    @Email(message = "Некорректный формат электронной почты.")
    @Size(min = 6, max = 254, message = "Некорректный формат электронной почты.")
    private String email;


    @NotBlank(message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    @Size(min = 6, max = 100, message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    private String password;
}
