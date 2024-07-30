package ru.yandex.kardomoblieapp.user.dto;

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
public class NewUserRequest {

    @NotBlank(message = "Никнейм не может быть пустым и должно содержать от 2 до 30 символов.")
    @Pattern(regexp = "^[a-z0-9]{2,30}$", message = "Никнейм не может быть пустым и должно содержать от 2 до 30 символов.")
    private String username;

    @NotBlank(message = "Некорректный формат электронной почты.")
    @Email(message = "Некорректный формат электронной почты.")
    @Size(min = 5, max = 50, message = "Некорректный формат электронной почты.")
    private String email;


    @NotBlank(message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$",
            message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    private String password;
}
