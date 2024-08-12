package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Новый комментарий")
public class CommentRequest {

    @NotBlank(message = "Название поста не может быть пустым и должно содержать от 2 до 100 символов.")
    @Size(min = 2, max = 10, message = "Название поста не может быть пустым и должно содержать от 2 до 100 символов.")
    @Schema(description = "Название комментария")
    private String text;
}
