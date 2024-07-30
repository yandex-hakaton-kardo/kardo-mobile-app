package ru.yandex.kardomoblieapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortUserDto {

    private long id;

    private String username;

    private DataFileDto profilePicture;
}
