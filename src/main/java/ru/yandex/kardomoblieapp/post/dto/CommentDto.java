package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Комментарий")
public record CommentDto(@Schema(description = "Идентификатор комментария")
                         long id,
                         @Schema(description = "Содержание комментария")
                         String text,
                         @Schema(description = "Идентификатор автора комментария")
                         long authorId) {

}
