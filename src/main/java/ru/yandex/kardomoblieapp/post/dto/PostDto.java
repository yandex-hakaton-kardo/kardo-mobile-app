package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;

import java.util.List;

@Builder
@Schema(description = "Пост пользователя")
public record PostDto(@Schema(description = "Идентификатор поста")
                      long id,
                      @Schema(description = "Имя поста")
                      String title,
                      @Schema(description = "Автор поста")
                      ShortUserDto author,
                      @Schema(description = "Прикрепленный файл")
                      DataFileDto file,
                      @Schema(description = "Количество лайков")
                      long likes,
                      @Schema(description = "Количество просмотров")
                      long views,
                      @Schema(description = "Комментарии к посту")
                      List<CommentDto> comments) {

}
