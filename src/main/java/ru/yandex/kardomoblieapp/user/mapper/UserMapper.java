package ru.yandex.kardomoblieapp.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.yandex.kardomoblieapp.user.dto.NewUserDto;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(NewUserDto newUserDto);

    UserDto toDto(User addedUser);

    ShortUserDto toShortDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateUser(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
