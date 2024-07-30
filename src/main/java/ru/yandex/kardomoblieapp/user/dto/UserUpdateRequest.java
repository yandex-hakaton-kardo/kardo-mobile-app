package ru.yandex.kardomoblieapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.shared.validation.Censored;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {


    //TODO добавить проверку на пустое поле
    @NotEmpty(message = "Никнейм не может быть пустым и должно содержать от 2 до 30 символов.")
    @Pattern(regexp = "^[a-z0-9]{2,30}$", message = "Никнейм не может быть пустым и должно содержать от 2 до 30 символов.")
    private String username;

    @Size(min = 2, max = 20, message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @NotEmpty(message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[^!@#$%^&0-9 ]+$")
    private String name;

    @Size(min = 2, max = 20, message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
    @NotEmpty(message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[^!@#$%^&0-9 ]+$")

    private String secondName;

    @Size(min = 2, max = 20, message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
    @NotEmpty(message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[^!@#$%^&0-9 ]+$")
    private String surname;

    @Email(message = "Некорректный формат электронной почты.")
    @Size(min = 5, max = 50, message = "Некорректный формат электронной почты.")
    @NotEmpty(message = "Некорректный формат электронной почты.")
    private String email;

    @NotEmpty(message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$",
            message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    private String password;

    private LocalDate dateOfBirth;

    private String country;

    private String city;

    @Pattern(regexp = "^[0-9+]${12,15}", message = "Неверный формат номера телефона.")
    private String phoneNumber;

    @Size(min = 2, max = 1000, message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
    @NotEmpty(message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
    private String overview;

    @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
    @NotEmpty(message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
    private String website;
}
