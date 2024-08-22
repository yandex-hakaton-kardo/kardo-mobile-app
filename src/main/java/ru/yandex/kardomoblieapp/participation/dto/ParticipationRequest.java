package ru.yandex.kardomoblieapp.participation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.user.model.Gender;

import java.time.LocalDate;

@Builder
public record ParticipationRequest(
        @Schema(description = "Имя пользователя", minLength = 2, maxLength = 20)
        @Size(min = 2, max = 20, message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
        @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Имя не может быть пустым и должно содержать от 2 до 20 символов.")
        String name,
        @Schema(description = "Отчество пользователя", minLength = 2, maxLength = 20)
        @Size(min = 2, max = 20, message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
        @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Отчество не может быть пустым и должно содержать от 2 до 20 символов.")
        String secondName,
        @Schema(description = "Фамилия пользователя", minLength = 2, maxLength = 20)
        @Size(min = 2, max = 20, message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
        @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ-]+$", message = "Фамилия не может быть пустой и должно содержать от 2 до 20 символов.")
        String surname,
        @Schema(description = "Электронная почта пользователя")
        @Email(regexp = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])$",
                message = "Некорректный формат электронной почты.")
        @Size(min = 5, max = 50, message = "Некорректный формат электронной почты.")
        String email,
        @Schema(description = "Пол")
        Gender gender,
        @Schema(description = "Страна проживания")
        Long countryId,
        @Schema(description = "Регион проживания")
        Long regionId,
        @Schema(description = "Город проживания")
        @Size(min = 2, max = 20, message = "Название города должно содержать от 2 до 20 символов.")
        @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ -]+$", message = "Название города должно содержать от 2 до 20 символов.")
        String city,
        @Schema(description = "Дата рождения пользователя") @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate dateOfBirth,
        @Schema(description = "Номер телефона", minLength = 12, maxLength = 15)
        @Pattern(regexp = "^[0-9+]${12,15}", message = "Неверный формат номера телефона.")
        String phoneNumber,
        @Schema(description = "О себе")
        @Size(min = 2, max = 500, message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
        @Pattern(regexp = "^[a-zA-zа-яА-ЯёЁ -]+$",
                message = "Описание не может быть пустым и должно содержать от 2 до 1000 символов.")
        String overview,
        @Schema(description = "Ссылка на соцсети")
        @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
        @Pattern(regexp = "^[a-z0-9:/@.#$%&?,*]{2,30}$",
                message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
        String website,
        @Schema(description = "Ссылка на на файл для участия в соревновании")
        @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
        @Pattern(regexp = "^[a-z0-9:/@.#$%&?,*]{2,30}$",
                message = "Ссылка не может быть пустой и должа содержать от 2 до 50 символов.")
        String linkToContestFile,
        @NotNull(message = "Должна быть указана роль.")
        ParticipantType type) {

}
