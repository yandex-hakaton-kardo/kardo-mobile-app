package ru.yandex.kardomoblieapp.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.datafiles.mapper.DataFileMapper;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.NewUserRequest;
import ru.yandex.kardomoblieapp.user.dto.NewUserResponse;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.FriendshipMapper;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.io.InputStream;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private DataFileMapper dataFileMapper;

    @MockBean
    private FriendshipMapper friendshipMapper;

    private NewUserRequest newUserRequest;

    private User user;

    private UserDto userDto;

    private Long userId;

    private Long requesterId;

    private UserUpdateRequest userUpdateRequest;

    private NewUserResponse newUserResponse;

    private DataFile dataFile;

    private DataFileDto dataFileDto;

    @BeforeEach
    void init() {
        newUserRequest = NewUserRequest.builder()
                .username("username")
                .email("email@mail.com")
                .password("P@ssword1")
                .build();
        newUserResponse = NewUserResponse.builder()
                .id(23L)
                .email("response@mail.ru")
                .username("response")
                .build();
        user = User.builder()
                .username("username")
                .email("email@mail.com")
                .password("P@ssword1")
                .build();
        userDto = UserDto.builder()
                .id(3L)
                .email("test@mail.ru")
                .build();
        userId = 1L;
        requesterId = 2L;
        userUpdateRequest = UserUpdateRequest.builder()
                .name("updatedИмя")
                .secondName("updatedОтчество")
                .surname("updatedФамилия")
                .email("updatedtest@mail.ru")
                .password("updateP@ssword1")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        dataFile = DataFile.builder()
                .id(32L)
                .fileName("file")
                .fileType(MediaType.IMAGE_JPEG_VALUE)
                .build();
        dataFileDto = DataFileDto.builder()
                .fileName("fileName")
                .id(23L)
                .build();
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    @DisplayName("Добавление пользователя с валидными полями")
    void addUser_allFieldsValid_ShouldReturn201Status() {
        when(userMapper.toModel(newUserRequest))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toNewUserDto(user))
                .thenReturn(newUserResponse);

        newUserRequest.setPassword("OhZ8?y1");

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(newUserResponse.id()), Long.class))
                .andExpect(jsonPath("$.email", is(newUserResponse.email())));

        verify(userMapper, times(1)).toModel(newUserRequest);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toNewUserDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с пустым именем")
    void addUser_withEmptyName_ShouldReturn400Status() {
        newUserRequest.setUsername("");
        when(userMapper.toModel(newUserRequest))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toNewUserDto(user))
                .thenReturn(newUserResponse);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.username",
                        is("Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")));

        verify(userMapper, never()).toModel(newUserRequest);
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toNewUserDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с коротким именем")
    void addUser_withShortName_ShouldReturn400Status() {
        newUserRequest.setUsername("a");
        when(userMapper.toModel(newUserRequest))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toNewUserDto(user))
                .thenReturn(newUserResponse);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.username",
                        is("Никнейм не может быть пустым и должен содержать от 2 до 30 символов.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toNewUserDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя без email")
    void addUser_withoutEmail_ShouldReturn400Status() {
        newUserRequest.setEmail(null);
        when(userMapper.toModel(newUserRequest))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toNewUserDto(user))
                .thenReturn(newUserResponse);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.email",
                        is("Некорректный формат электронной почты.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toNewUserDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с неправильным форматом email")
    void addUser_withoutWrongFormatEmail_ShouldReturn400Status() {
        newUserRequest.setEmail("а@daf");
        when(userMapper.toModel(newUserRequest))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toNewUserDto(user))
                .thenReturn(newUserResponse);

        mvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.email",
                        is("Некорректный формат электронной почты.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toNewUserDto(any());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    @DisplayName("Обновление пользователя со всеми валидными полями")
    void updateUser_allFieldsValid_ShouldReturn200Status() {

        when(userService.updateUser(userId, userUpdateRequest))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.id()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.email())));

        verify(userService, times(1)).updateUser(userId, userUpdateRequest);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя с пустым именем")
    void updateUser_withEmptyName_ShouldReturn400Status() {
        userUpdateRequest.setName("");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.name",
                        is("Имя не может быть пустым и должно содержать от 2 до 20 символов.")));

        verify(userService, never()).updateUser(anyLong(), any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, когда имя null")
    void updateUser_whenNameIsNull_ShouldReturn200Status() {
        userUpdateRequest.setName(null);
        when(userService.updateUser(userId, userUpdateRequest))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.id()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.email())));

        verify(userService, times(1)).updateUser(userId, userUpdateRequest);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, пользователь не найден")
    void updateUser_userNotFound_ShouldReturn404Status() {
        when(userService.updateUser(userId, userUpdateRequest))
                .thenThrow(new NotFoundException("Пользователь с id '1' не найден."));

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '1' не найден.")));

        verify(userService, times(1)).updateUser(userId, userUpdateRequest);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, пользователь не имеет прав доступа")
    void updateUser_userNotAuthorized_ShouldReturn403Status() {
        when(userService.updateUser(userId, userUpdateRequest))
                .thenThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."));

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).updateUser(userId, userUpdateRequest);
        verify(userMapper, never()).toDto(any());
    }

//    @Test
//    @SneakyThrows
//    @WithMockUser
//    @DisplayName("Удаление пользователя")
//    void deleteUser_shouldReturnStatus204() {
//        doNothing()
//                .when(userService).deleteUser(user.getUsername(), userId);
//
//        mvc.perform(delete("/users/{userId}", userId))
//                .andExpect(status().isNoContent());
//
//        verify(userService, times(1)).deleteUser(user.getUsername(), userId);
//    }

//    @Test
//    @SneakyThrows
//    @WithMockUser(value = "username")
//    @DisplayName("Удаление пользователя, пользователь не найден")
//    void deleteUser_userNotFound_ShouldReturn404Status() {
//        doThrow(new NotFoundException("Пользователь с id '1' не найден."))
//                .when(userService).deleteUser(any(), eq(userId));
//
//        mvc.perform(delete("/users/{userId}", userId))
//                .andExpect(status().isNotFound())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
//                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '1' не найден.")));
//
//        verify(userService, times(1)).deleteUser(any(), eq(userId));
//    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии профиля")
    void addProfilePicture_ShouldReturn200Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        when(userService.uploadProfilePicture(userId, file))
                .thenReturn(dataFile);
        when(dataFileMapper.toDto(dataFile))
                .thenReturn(dataFileDto);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(dataFileDto.id()), Long.class))
                .andExpect(jsonPath("$.fileName", is(dataFileDto.fileName())));

        verify(userService, times(1)).uploadProfilePicture(userId, file);
        verify(dataFileMapper, times(1)).toDto(dataFile);
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии пользователем без прав")
    void addProfilePicture_userNotAuthorized_shouldReturn403Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        doThrow(new NotAuthorizedException("Пользователь с id '" + user.getUsername() + "' не имеет прав на редактирование профиля."))
                .when(userService).uploadProfilePicture(userId, file);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + user.getUsername() + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).uploadProfilePicture(userId, file);
        verify(dataFileMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии пользователь не найден")
    void addProfilePicture_userNotFound_shouldReturn404Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден."))
                .when(userService).uploadProfilePicture(userId, file);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).uploadProfilePicture(userId, file);
        verify(dataFileMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение фотографии профиля пользователя")
    void getUserProfilePicture_shouldReturnJpeg() {

        when(userService.getProfilePicture(userId)).thenReturn(dataFile);
        when(dataFileMapper.toDto(dataFile)).thenReturn(dataFileDto);

        mvc.perform(get("/users/{userId}/avatar", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dataFileDto.id()), Long.class))
                .andExpect(jsonPath("$.fileName", is(dataFileDto.fileName())));

        verify(userService, times(1)).getProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение фотографии профиля пользователя, пользователь не найден")
    void getUserProfilePicture_whenUserNotFound_shouldReturn404Status() {
        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден.")).when(userService)
                .getProfilePicture(userId);

        mvc.perform(get("/users/{userId}/avatar", userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).getProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля")
    void deleteProfilePicture_shouldReturn204Status() {
        doNothing().when(userService).deleteProfilePicture(userId);

        mvc.perform(delete("/users/{userId}/avatar", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля, пользователь не найден")
    void deleteProfilePicture_whenUserNotFound_shouldThrowNotFoundException() {
        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден."))
                .when(userService).deleteProfilePicture(userId);

        mvc.perform(delete("/users/{userId}/avatar", userId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).deleteProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля, пользователь не может редактировать профиль")
    void deleteProfilePicture_whenUserNotAuthorized_shouldThrowNotAuthorizedException() {
        doThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."))
                .when(userService).deleteProfilePicture(userId);

        mvc.perform(delete("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).deleteProfilePicture(userId);
    }
}

