package ru.yandex.kardomoblieapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.model.Gender;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;

    private String name;

    private String secondName;

    private String surname;

    private LocalDate dateOfBirth;

    private String email;

    private String country;

    private String city;

    private Gender gender;

    private DataFile profilePicture;

    private String overview;

    private String website;
}
