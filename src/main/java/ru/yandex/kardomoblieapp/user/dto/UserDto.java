package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.model.Gender;

import java.time.LocalDate;

@Builder
@Schema(description = "Сущность пользователя")
public record UserDto(@Schema(description = "Идентификатор пользователя")
                      Long id,
                      @Schema(description = "Никнейм пользователя")
                      String username,
                      @Schema(description = "Имя пользователя")
                      String name,
                      @Schema(description = "Отчество пользователя")
                      String secondName,
                      @Schema(description = "Фамилия пользователя")
                      String surname,
                      @Schema(description = "Дата рождения пользователя")
                      LocalDate dateOfBirth,
                      @Schema(description = "Электронная почта пользователя")
                      String email,
                      @Schema(description = "Страна проживания")
                      String country,
                      @Schema(description = "Регион страны проживания")
                      String region,
                      @Schema(description = "Город проживания")
                      String city,
                      @Schema(description = "Пол")
                      Gender gender,
                      @Schema(description = "Фотография профиля")
                      DataFile profilePicture,
                      @Schema(description = "Номер телефона")
                      String phoneNumber,
                      @Schema(description = "О себе")
                      String overview,
                      @Schema(description = "Ссылка на соцсети")
                      String website) {

}
