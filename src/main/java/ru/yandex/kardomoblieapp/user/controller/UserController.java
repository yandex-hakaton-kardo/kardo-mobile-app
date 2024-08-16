package ru.yandex.kardomoblieapp.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import ru.yandex.kardomoblieapp.security.jwt.model.TokensResponse;
import ru.yandex.kardomoblieapp.shared.exception.ErrorResponse;
import ru.yandex.kardomoblieapp.user.dto.FriendshipDto;
import ru.yandex.kardomoblieapp.user.dto.NewUserRequest;
import ru.yandex.kardomoblieapp.user.dto.NewUserResponse;
import ru.yandex.kardomoblieapp.user.dto.ShortUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserSearchFilter;
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
    @Operation(summary = "Регистрация пользователя в приложении")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Новый пользователь зарегистрирован", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = NewUserResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Введены некорректные данные", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Пользователь с данным именем или электронной почтой уже " +
                    "существует", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public NewUserResponse createUser(@RequestBody @Valid @Parameter(description = "Регистрационные данные") NewUserRequest newUser) {
        log.debug("Регистрация нового пользователя с email '{}'.", newUser.getEmail());
        final User userToAdd = userMapper.toModel(newUser);
        final User addedUser = userService.createUser(userToAdd);
        return userMapper.toNewUserDto(addedUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицировался, получены токены доступа",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TokensResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не найден или введены неверный логин/пароль", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public TokensResponse fakeLogin() {
        throw new IllegalStateException("Данный эндпоинт реализован на уровне Spring Security.");
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "JWT")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Выход из приложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь вышел из учетной записи"),
            @ApiResponse(responseCode = "401", description = "Пользователь с введенным refresh токеном не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void fakeLogout() {
        throw new IllegalStateException("Данный эндпоинт реализован на уровне Spring Security.");
    }

    @PatchMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Редактирование данных пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "400", description = "Введены некорректные данные", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public UserDto updateUser(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId,
                              @RequestBody @Valid @Parameter(description = "Обновленные параметры") UserUpdateRequest userUpdateRequest,
                              @Parameter(hidden = true) Principal principal) {
        log.info("Обновление данных пользователя с id '{}'.", userId);
        final User updatedUser = userService.updateUser(userId, userUpdateRequest);
        return userMapper.toDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление профиля пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void deleteUser(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId) {
        log.info("Удаление пользователя с id '{}'.", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск пользователя по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public UserDto findUserById(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId) {
        log.debug("Получение данных пользователя с id '{}'.", userId);
        final User user = userService.findUserById(userId);
        return userMapper.toDto(user);
    }

    @PostMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Обновление фотографии профиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография профиля успешно загружена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DataFileDto.class))}),
            @ApiResponse(responseCode = "400", description = "Введены некорректные данные или не прикреплен файл", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при сохранении файла", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public DataFileDto uploadProfilePicture(@PathVariable long userId,
                                            @RequestParam("avatar")
                                            @Parameter(description = "Файл фотографии") MultipartFile avatar,
                                            @Parameter(hidden = true) Principal principal) {
        log.info("Загрузка фотографии профиля '{}' пользователя с id '{}'.", avatar.getName(), userId);
        final DataFile savedFile = userService.uploadProfilePicture(userId, avatar);
        return dataFileMapper.toDto(savedFile);
    }

    @GetMapping(value = "/{userId}/avatar")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение фотографии профиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография профиля найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DataFileDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Фотография профиля не найдена", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public DataFileDto getUserProfilePicture(@PathVariable
                                             @Parameter(description = "Идентификатор пользователя") long userId) {
        log.debug("Получение фотографии профиля пользователя с id '{}'.", userId);
        DataFile profilePicture = userService.getProfilePicture(userId);
        return dataFileMapper.toDto(profilePicture);
    }

    @DeleteMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление фотографии профиля")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Фотография профиля успешно удалена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Ошибка при удалении файла из локального хранилища", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void deleteProfilePicture(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId,
                                     @Parameter(hidden = true) Principal principal) {
        log.info("Пользователь с id '{}' удаляет фотографию профиля.", userId);
        userService.deleteProfilePicture(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Добавление пользователя в друзья")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь добавлен в друзья", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = FriendshipDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public FriendshipDto addFriend(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId,
                                   @PathVariable @Parameter(description = "Идентификатор друга") long friendId) {
        log.info("Пользователь с id '{}' добавляет в друзья пользователя c id '{}'.", userId, friendId);
        Friendship friendship = userService.addFriend(userId, friendId);
        return friendshipMapper.toDto(friendship);
    }

    @GetMapping("/{userId}/friends")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение списка друзей пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список друзей пользователя", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ShortUserDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<ShortUserDto> getFriendsList(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId) {
        log.info("Получение списка друзей пользователя с id '{}'.", userId);
        List<User> friends = userService.getFriendsList(userId);
        return userMapper.toShortDtoList(friends);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удаление пользователя из друзей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь удален из списка друзей"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public void deleteFriend(@PathVariable @Parameter(description = "Идентификатор пользователя") long userId,
                             @PathVariable @Parameter(description = "Идентификатор друга") long friendId) {
        log.info("Пользователь с id '{}' удалил из друзей пользователя с id '{}'.", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск пользователей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список пользователей", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public List<UserDto> findAllUsers(@Parameter(description = "Фильтр поиска") UserSearchFilter filter,
                                      @RequestParam(defaultValue = "0")
                                      @Parameter(description = "Номер страницы") Integer page,
                                      @RequestParam(defaultValue = "10")
                                      @Parameter(description = "Количество элементов на странице") Integer size) {
        log.debug("Получение списка всех пользователей. page: '{}', size: '{}'.", page, size);
        List<User> users = userService.findAllUsers(filter, page, size);
        return userMapper.toDtoList(users);
    }

    @GetMapping("/info")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Поиск пользователя по никнейму")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Срок действия токена доступа истек"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Произошла неизвестная ошибка", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    public UserDto findUserByUsername(@RequestParam @Parameter(description = "Идентификатор пользователя") String username) {
        log.debug("Получение данных пользователя с username '{}'.", username);
        final User user = userService.findFullUserByUsername(username);
        return userMapper.toDto(user);
    }
}
