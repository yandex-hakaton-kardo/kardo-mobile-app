package ru.yandex.kardomoblieapp.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.datafiles.mapper.DataFileMapper;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.dto.FriendshipDto;
import ru.yandex.kardomoblieapp.user.dto.NewUserRequest;
import ru.yandex.kardomoblieapp.user.dto.NewUserResponse;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.FriendshipMapper;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Пользователи", description = "Взаимодействие с пользователями")
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    private final DataFileMapper dataFileMapper;

    private final FriendshipMapper friendshipMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation()
    public NewUserResponse createUser(@RequestBody @Valid NewUserRequest newUser) {
        log.info("Регистрация нового пользователя с email '{}'.", newUser.getEmail());
        final User userToAdd = userMapper.toModel(newUser);
        final User addedUser = userService.createUser(userToAdd);
        return userMapper.toNewUserDto(addedUser);
    }

    @PatchMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    public UserDto updateUser(@PathVariable long userId,
                              @RequestBody @Valid UserUpdateRequest userUpdateRequest,
                              Principal principal) {
        log.info("Обновление данных пользователя с id '{}'.", userId);
        final User updatedUser = userService.updateUser(userId, userUpdateRequest);
        return userMapper.toDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    public void deleteUser(@PathVariable long userId,
                           Principal principal) {
        log.info("Удаление пользователя с id '{}'.", userId);
        userService.deleteUser(principal.getName(), userId);
    }

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    public UserDto findUserById(@PathVariable long userId) {
        log.info("Получение данных пользователя с id '{}'.", userId);
        final User user = userService.findUserById(userId);
        return userMapper.toDto(user);
    }

    @PostMapping("/{userId}/avatar")
    @SecurityRequirement(name = "JWT")
    public DataFileDto uploadProfilePicture(@PathVariable long userId,
                                            @RequestParam("avatar") MultipartFile avatar,
                                            Principal principal) {
        log.info("Загрузка фотографии профиля '{}' пользователя с id '{}'.", avatar.getName(), userId);
        final DataFile savedFile = userService.uploadProfilePicture(userId, avatar);
        return dataFileMapper.toDto(savedFile);
    }

    @GetMapping(value = "/{userId}/avatar")
    @SecurityRequirement(name = "JWT")
    public DataFileDto getUserProfilePicture(@PathVariable long userId) {
        log.info("Получение фотографии профиля пользователя с id '{}'.", userId);
        DataFile profilePicture = userService.getProfilePicture(userId);
        return dataFileMapper.toDto(profilePicture);
    }

    @DeleteMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    public void deleteProfilePicture(@PathVariable long userId,
                                     Principal principal) {
        log.info("Пользователь с id '{}' удаляет фотографию профиля.", userId);
        userService.deleteProfilePicture(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @SecurityRequirement(name = "JWT")
    public FriendshipDto addFriend(@PathVariable long userId, @PathVariable long friendId) {
        log.info("Пользователь с id '{}' добавляет в друзья пользователя c id '{}'.", userId, friendId);
        Friendship friendship = userService.addFriend(userId, friendId);
        return friendshipMapper.toDto(friendship);
    }

    @GetMapping("/{userId}/friends")
    @SecurityRequirement(name = "JWT")
    public List<ShortUserDto> getFriendsList(@PathVariable long userId) {
        log.info("Получение списка друзей пользователя с id '{}'.", userId);
        List<User> friends = userService.getFriendsList(userId);
        return userMapper.toShortDtoList(friends);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @SecurityRequirement(name = "JWT")
    public void deleteFriend(@PathVariable long userId,
                             @PathVariable long friendId) {
        log.info("Пользователь с id '{}' удалил из друзей пользователя с id '{}'.", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }
}
