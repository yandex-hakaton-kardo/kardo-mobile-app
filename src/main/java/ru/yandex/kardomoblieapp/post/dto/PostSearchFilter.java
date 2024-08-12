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
@Schema(description = "Фильтр поиска постов")
public class PostSearchFilter {

    @Schema(description = "Поиск данного значения в названии поста")
    private String title;
}
