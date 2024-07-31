package ru.yandex.kardomoblieapp.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.user.model.Gender;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {


    @Pattern(regexp = "^[a-z0-9]{2,30}$", message = "Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")
    @Schema(description = "Никнейм пользователя", minLength = 2, maxLength = 30)
    private String username;

    @Size(min = 2, max = 20, message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    @Schema(description = "Имя пользователя", minLength = 2, maxLength = 20)
    private String name;

    @Size(min = 2, max = 20, message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
    @Schema(description = "Отчество пользователя", minLength = 2, maxLength = 20)
    private String secondName;

    @Size(min = 2, max = 20, message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
    @Schema(description = "Фамилия пользователя", minLength = 2, maxLength = 20)
    private String surname;

    @Email(regexp = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$", message = "Некорректный формат электронной почты.")
    @Size(min = 5, max = 50, message = "Некорректный формат электронной почты.")
    @Schema(description = "Электронная почта пользователя")
    private String email;

    @Schema(description = "Пол")
    private Gender gender;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$",
            message = "Пароль не может быть пустым и должен содержать от 8 до 15 символов.")
    @Schema(description = "Пароль", minLength = 8, maxLength = 15)
    private String password;

    @Schema(description = "Дата рождения пользователя")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateOfBirth;

    @Schema(description = "Страна проживания")
    private String country;

    @Schema(description = "Город проживания")
    private String city;

    @Pattern(regexp = "^[0-9+]${12,15}", message = "Неверный формат номера телефона.")
    @Schema(description = "Номер телефона", minLength = 12, maxLength = 15)
    private String phoneNumber;

    @Size(min = 2, max = 500, message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
    @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
    @Schema(description = "О себе")
    private String overview;

    @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
    @Schema(description = "Ссылка на соцсети")
    @Pattern(regexp = "^[a-z0-9:/@.#$%&?,*]{2,30}$", message = "Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")
    private String website;
}
