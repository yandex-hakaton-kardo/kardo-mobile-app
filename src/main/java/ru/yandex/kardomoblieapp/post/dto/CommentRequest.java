package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Новый комментарий")
public record CommentRequest(
        @Schema(description = "Комментарий")
        @NotBlank(message = "Комментарий не может быть пустым и должно содержать от 2 до 230 символов.")
        @Size(min = 2, max = 230, message = "Комментарий не может быть пустым и должно содержать от 2 до 230 символов.")
        String text) {

}
