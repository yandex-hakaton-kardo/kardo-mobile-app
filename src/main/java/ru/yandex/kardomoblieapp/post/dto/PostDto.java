package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Пост пользователя")
public class PostDto {

    @Schema(description = "Идентификатор поста")
    private long id;

    @Schema(description = "Имя поста")
    private String title;

    @Schema(description = "Автор поста")
    private ShortUserDto author;

    @Schema(description = "Прикрепленный файл")
    private DataFileDto file;

    @Schema(description = "Количество лайков")
    private long likes;

    @Schema(description = "Количество просмотров")
    private long views;

    @Schema(description = "Комментарии к посту")
    private List<CommentDto> comments;
}
