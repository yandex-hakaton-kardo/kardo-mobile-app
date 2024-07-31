package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.model.Gender;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Сущность пользователя")
public class UserDto {

    @Schema(description = "Идентификатор пользователя")
    private Long id;

    @Schema(description = "Никнейм пользователя")
    private String username;

    @Schema(description = "Имя пользователя")
    private String name;

    @Schema(description = "Отчество пользователя")
    private String secondName;

    @Schema(description = "Фамилия пользователя")
    private String surname;

    @Schema(description = "Дата рождения пользователя")
    private LocalDate dateOfBirth;

    @Schema(description = "Электронная почта пользователя")
    private String email;

    @Schema(description = "Страна проживания")
    private String country;

    @Schema(description = "Город проживания")
    private String city;

    @Schema(description = "Пол")
    private Gender gender;

    @Schema(description = "Фотография профиля")
    private DataFile profilePicture;

    @Schema(description = "Номер телефона")
    private String phoneNumber;

    @Schema(description = "О себе")
    private String overview;

    @Schema(description = "Ссылка на соцсети")
    private String website;

    @Schema(description = "Список друзей")
    private List<ShortUserDto> friends;
}
