package ru.yandex.kardomoblieapp.user.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.user.dto.FriendshipDto;
import ru.yandex.kardomoblieapp.user.model.Friendship;

@Mapper(componentModel = "spring")
public interface FriendshipMapper {

    FriendshipDto toDto(Friendship friendship);
}
