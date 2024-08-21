package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр поиска пользователей")
public record UserSearchFilter(
        @Schema(description = "Поиск данного значения в никнейме или электронной почте")
        String name) {

}
