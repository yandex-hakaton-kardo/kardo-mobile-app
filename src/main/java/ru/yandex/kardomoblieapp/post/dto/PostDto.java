package ru.yandex.kardomoblieapp.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto {

    private long id;

    private String content;

    private ShortUserDto author;

    private DataFileDto file;
}
