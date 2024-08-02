package ru.yandex.kardomoblieapp.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.kardomoblieapp.user.dto.NewUserRequest;
import ru.yandex.kardomoblieapp.user.dto.NewUserResponse;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(NewUserRequest newUserRequest);

    @Mapping(source = "country.id", target = "country")
    @Mapping(source = "region.id", target = "region")
    @Mapping(source = "city.id", target = "city")
    UserDto toDto(User addedUser);

    NewUserResponse toNewUserDto(User user);

    ShortUserDto toShortDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "city", ignore = true)
    void updateUser(UserUpdateRequest userUpdateRequest, @MappingTarget User user);

    List<ShortUserDto> toShortDtoList(List<User> friends);

    List<UserDto> toDtoList(List<User> users);
}
