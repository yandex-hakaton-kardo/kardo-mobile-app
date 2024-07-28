package ru.yandex.kardomoblieapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
    private String name;

    @Size(min = 2, max = 20, message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
    private String secondName;

    @Size(min = 2, max = 20, message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
    private String surname;

    @Email(message = "Некорректный формат электронной почты.")
    @Size(min = 6, max = 254, message = "Некорректный формат электронной почты.")
    private String email;


    @Size(min = 6, max = 100, message = "Пароль не может быть пустым и должен содержать от 6 до 100 символов.")
    private String password;

    private LocalDate dateOfBirth;

    private String country;

    private String city;

    @Size(min = 2, max = 1000, message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
    private String overview;

    @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
    private String website;
}
