package ru.yandex.kardomoblieapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendshipDto {

    private FriendshipStatus status;

}
