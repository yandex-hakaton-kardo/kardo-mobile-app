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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.user.dto.NewUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserDto newUserDto) {
        log.info("Регистрация нового пользователя с email '{}'.", newUserDto.getEmail());
        User userToAdd = userMapper.toModel(newUserDto);
        User addedUser = userService.createUser(userToAdd);
        return userMapper.toDto(addedUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestHeader("X-Kardo-User-Id") long requesterId,
                              @PathVariable long userId,
                              @RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        log.info("Обновление данных пользователя с id '{}'.", userId);
        User updatedUser = userService.updateUser(requesterId, userId, userUpdateRequest);
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
        User user = userService.findUserById(userId);
        return userMapper.toDto(user);
    }
}
