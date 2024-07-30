package ru.yandex.kardomoblieapp.user.controller;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.datafiles.mapper.DataFileMapper;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.dto.NewUserRequest;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    private final DataFileMapper dataFileMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUser) {
        log.info("Регистрация нового пользователя с email '{}'.", newUser.getEmail());
        final User userToAdd = userMapper.toModel(newUser);
        final User addedUser = userService.createUser(userToAdd);
        return userMapper.toDto(addedUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestHeader("X-Kardo-User-Id") long requesterId,
                              @PathVariable long userId,
                              @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        log.info("Обновление данных пользователя с id '{}'.", userId);
        final User updatedUser = userService.updateUser(requesterId, userId, userUpdateRequest);
        return userMapper.toDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@RequestHeader("X-Kardo-User-Id") long requesterId, @PathVariable long userId) {
        log.info("Удаление пользователя с id '{}'.", userId);
        userService.deleteUser(requesterId, userId);
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@RequestHeader("X-Kardo-User-Id") long requesterId, @PathVariable long userId) {
        log.info("Получение данных пользователя с id '{}'.", userId);
        final User user = userService.findUserById(userId);
        return userMapper.toDto(user);
    }

    @PostMapping("/{userId}/avatar")
    public DataFileDto uploadProfilePicture(@RequestHeader("X-Kardo-User-Id") long requesterId,
                                            @PathVariable long userId,
                                            @RequestParam("avatar") MultipartFile avatar) {
        log.info("Загрузка фотографии профиля '{}' пользователя с id '{}'.", avatar.getName(), userId);
        final DataFile savedFile = userService.uploadProfilePicture(requesterId, userId, avatar);
        return dataFileMapper.toDto(savedFile);
    }

    @GetMapping(value = "/{userId}/avatar")
    public DataFileDto getUserProfilePicture(@RequestHeader("X-Kardo-User-Id") long requesterId,
                                             @PathVariable long userId) {
        log.info("Получение фотографии профиля пользователя с id '{}'.", userId);
        DataFile profilePicture = userService.getProfilePicture(userId);
        return dataFileMapper.toDto(profilePicture);
    }

    @DeleteMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfilePicture(@RequestHeader("X-Kardo-User-Id") long requesterId,
                                     @PathVariable long userId) {
        log.info("Пользователь с id '{}' удаляет фотографию профиля.", userId);
        userService.deleteProfilePicture(requesterId, userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public Friendship addFriend(@PathVariable long userId, @PathVariable long friendId) {
        log.info("Пользователь с id '{}' добавляет в друзья пользователя c id '{}'.", userId, friendId);
        return userService.addFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<ShortUserDto> getFriendsList(@PathVariable long userId) {
        log.info("Получение списка друзей пользователя с id '{}'.", userId);
        List<User> friends = userService.getFriendsList(userId);
        return userMapper.toShortDtoList(friends);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable long userId,
                             @PathVariable long friendId) {
        log.info("Пользователь с id '{}' удалил из друзей пользователя с id '{}'.", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }
}
