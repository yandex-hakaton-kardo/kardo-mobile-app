package ru.yandex.kardomoblieapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр поиска постов")
public record PostSearchFilter(@Schema(description = "Поиск данного значения в названии поста")
                               String title) {

}
