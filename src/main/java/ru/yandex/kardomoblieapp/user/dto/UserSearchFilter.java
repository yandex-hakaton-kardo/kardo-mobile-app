package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Фильтр поиска пользователей")
public class UserSearchFilter {

    @Schema(description = "Поиск данного значения в никнейме или электронной почте")
    private String name;
}
