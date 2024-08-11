package ru.yandex.kardomoblieapp.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.model.UserRole;
import ru.yandex.kardomoblieapp.user.service.UserService;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Пользователи", description = "Администрирование пользователей")
public class UserAdminController {

    private final UserService userService;

    private final UserMapper userMapper;

    @PatchMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Изменение роли пользователя")
    public UserDto updateUser(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId,
                              @Parameter(description = "Роль пользователя")
                              @RequestParam @Valid UserRole newRole) {
        log.info("Обновление данных пользователя с id '{}'.", userId);
        final User updatedUser = userService.changeUserRole(userId, newRole);
        return userMapper.toDto(updatedUser);
    }

}
