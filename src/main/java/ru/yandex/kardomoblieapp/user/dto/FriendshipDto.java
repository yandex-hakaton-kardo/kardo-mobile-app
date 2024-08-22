package ru.yandex.kardomoblieapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;

@Builder
@Schema(description = "Дружба")
public record FriendshipDto(@Schema(description = "Статус дружбы между пользователями")
                            FriendshipStatus status) {

}
