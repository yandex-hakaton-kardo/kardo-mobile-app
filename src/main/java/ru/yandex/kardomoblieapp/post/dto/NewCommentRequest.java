package ru.yandex.kardomoblieapp.post.dto;

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
public class NewCommentRequest {

    @NotBlank(message = "Имя поста не может быть пустым и должно содержать от 2 до 20 символов.")
    @Size(min = 2, max = 20, message = "Имя поста не может быть пустым и должно содержать от 2 до 20 символов.")
    private String text;
}
