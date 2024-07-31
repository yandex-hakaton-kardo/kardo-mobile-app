package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Дружба")
public class FriendshipDto {

    @Schema(description = "Статус дружбы между пользователями")
    private FriendshipStatus status;
}
