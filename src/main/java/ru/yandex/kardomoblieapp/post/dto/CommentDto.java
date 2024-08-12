package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Комментарий")
public class CommentDto {

    @Schema(description = "Идентификатор комментария")
    private long id;

    @Schema(description = "Содержание комментария")
    private String text;

    @Schema(description = "Идентификатор автора комментария")
    private long authorId;
}
