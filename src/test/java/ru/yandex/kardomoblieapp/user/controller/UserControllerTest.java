package ru.yandex.kardomoblieapp.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.kardomoblieapp.datafiles.dto.DataFileDto;
import ru.yandex.kardomoblieapp.datafiles.mapper.DataFileMapper;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.NewUserDto;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
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

    private NewUserDto newUser1;

    private User user;

    private UserDto userDto;

    private Long userId;

    private Long requesterId;

    private UserUpdateRequest userUpdateRequest;

    private DataFile dataFile;

    private DataFileDto dataFileDto;

    @BeforeEach
    void init() {
        newUser1 = NewUserDto.builder()
                .name("Имя")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        user = new User();
        userDto = UserDto.builder()
                .id(3L)
                .email("test@mail.ru")
                .build();
        userId = 1L;
        requesterId = 2L;
        userUpdateRequest = UserUpdateRequest.builder()
                .name("updated Имя")
                .secondName("updated Отчество")
                .surname("updated Фамилия")
                .country("updated Россия")
                .city("updated Москва")
                .email("updatedtest@mail.ru")
                .password("updated password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        dataFile = new DataFile();
        dataFileDto = DataFileDto.builder()
                .fileName("fileName")
                .id(23L)
                .build();
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с валидными полями")
    void addUser_allFieldsValid_ShouldReturn200Status() {
        when(userMapper.toModel(newUser1))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userMapper, times(1)).toModel(newUser1);
        verify(userService, times(1)).createUser(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с пустым именем")
    void addUser_withEmptyName_ShouldReturn400Status() {
        newUser1.setName("");
        when(userMapper.toModel(newUser1))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser1)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.name",
                        is("Имя не может быть пустым и должно содержать от 2 до 20 символов.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с коротким именем")
    void addUser_withShortName_ShouldReturn400Status() {
        newUser1.setName("a");
        when(userMapper.toModel(newUser1))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser1)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.name",
                        is("Имя не может быть пустым и должно содержать от 2 до 20 символов.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя без email")
    void addUser_withoutEmail_ShouldReturn400Status() {
        newUser1.setEmail(null);
        when(userMapper.toModel(newUser1))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser1)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.email",
                        is("Некорректный формат электронной почты.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление пользователя с неправильным форматом email")
    void addUser_withoutWrongFormatEmail_ShouldReturn400Status() {
        newUser1.setEmail("а@daf");
        when(userMapper.toModel(newUser1))
                .thenReturn(user);
        when(userService.createUser(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser1)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.email",
                        is("Некорректный формат электронной почты.")));

        verify(userMapper, never()).toModel(any());
        verify(userService, never()).createUser(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя со всеми валидными полями")
    void updateUser_allFieldsValid_ShouldReturn200Status() {
        when(userService.updateUser(requesterId, userId, userUpdateRequest))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).updateUser(requesterId, userId, userUpdateRequest);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя с пустым именем")
    void updateUser_withEmptyName_ShouldReturn400Status() {
        userUpdateRequest.setName("");

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.name",
                        is("Имя не может быть пустым и должно содержать от 2 до 20 символов.")));

        verify(userService, never()).updateUser(anyLong(), anyLong(), any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, когда имя null")
    void updateUser_whenNameIsNull_ShouldReturn200Status() {
        userUpdateRequest.setName(null);
        when(userService.updateUser(requesterId, userId, userUpdateRequest))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).updateUser(requesterId, userId, userUpdateRequest);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, пользователь не найден")
    void updateUser_userNotFound_ShouldReturn404Status() {
        when(userService.updateUser(requesterId, userId, userUpdateRequest))
                .thenThrow(new NotFoundException("Пользователь с id '1' не найден."));

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '1' не найден.")));

        verify(userService, times(1)).updateUser(requesterId, userId, userUpdateRequest);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление пользователя, пользователь не имеет прав доступа")
    void updateUser_userNotAuthorized_ShouldReturn403Status() {
        when(userService.updateUser(requesterId, userId, userUpdateRequest))
                .thenThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."));

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).updateUser(requesterId, userId, userUpdateRequest);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление пользователя")
    void deleteUser_shouldReturnStatus204() {
        doNothing()
                .when(userService).deleteUser(requesterId, userId);

        mvc.perform(delete("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(requesterId, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление пользователя, пользователь не найден")
    void deleteUser_userNotFound_ShouldReturn404Status() {
        doThrow(new NotFoundException("Пользователь с id '1' не найден."))
                .when(userService).deleteUser(requesterId, userId);

        mvc.perform(delete("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '1' не найден.")));

        verify(userService, times(1)).deleteUser(requesterId, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление пользователя, пользователь не имеет прав доступа")
    void deleteUser_userNotAuthorized_ShouldReturn403Status() {
        doThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."))
                .when(userService).deleteUser(requesterId, userId);

        mvc.perform(delete("/users/{userId}", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).deleteUser(requesterId, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии профиля")
    void addProfilePicture_ShouldReturn200Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        when(userService.uploadProfilePicture(requesterId, userId, file))
                .thenReturn(dataFile);
        when(dataFileMapper.toDto(dataFile))
                .thenReturn(dataFileDto);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(dataFileDto.getId()), Long.class))
                .andExpect(jsonPath("$.fileName", is(dataFileDto.getFileName())));

        verify(userService, times(1)).uploadProfilePicture(requesterId, userId, file);
        verify(dataFileMapper, times(1)).toDto(dataFile);
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии пользователем без прав")
    void addProfilePicture_userNotAuthorized_shouldReturn403Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        doThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."))
                .when(userService).uploadProfilePicture(requesterId, userId, file);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).uploadProfilePicture(requesterId, userId, file);
        verify(dataFileMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Добавление фотографии пользователь не найден")
    void addProfilePicture_userNotFound_shouldReturn404Status() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден."))
                .when(userService).uploadProfilePicture(requesterId, userId, file);

        mvc.perform(multipart("/users/{userId}/avatar", userId).file(file)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).uploadProfilePicture(requesterId, userId, file);
        verify(dataFileMapper, never()).toDto(any());
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение фотографии профиля пользователя")
    void getUserProfilePicture_shouldReturnJpeg() {
        byte[] result = new byte[0];

        when(userService.downloadProfilePicture(userId)).thenReturn(result);

        mvc.perform(get("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        verify(userService, times(1)).downloadProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение фотографии профиля пользователя, пользователь не найден")
    void getUserProfilePicture_whenUserNotFound_shouldReturn404Status() {
        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден.")).when(userService)
                .downloadProfilePicture(userId);

        mvc.perform(get("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).downloadProfilePicture(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля")
    void deleteProfilePicture_shouldReturn204Status() {
        doNothing().when(userService).deleteProfilePicture(requesterId, userId);

        mvc.perform(delete("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteProfilePicture(requesterId, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля, пользователь не найден")
    void deleteProfilePicture_whenUserNotFound_shouldThrowNotFoundException() {
        doThrow(new NotFoundException("Пользователь с id '" + userId + "' не найден."))
                .when(userService).deleteProfilePicture(requesterId, userId);

        mvc.perform(delete("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(jsonPath("$.errors.error", is("Пользователь с id '" + userId + "' не найден.")));

        verify(userService, times(1)).deleteProfilePicture(requesterId, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление фотографии профиля, пользователь не может редактировать профиль")
    void deleteProfilePicture_whenUserNotAuthorized_shouldThrowNotAuthorizedException() {
        doThrow(new NotAuthorizedException("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля."))
                .when(userService).deleteProfilePicture(requesterId, userId);

        mvc.perform(delete("/users/{userId}/avatar", userId)
                        .header("X-Kardo-User-Id", requesterId))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotAuthorizedException))
                .andExpect(jsonPath("$.errors.error",
                        is("Пользователь с id '" + requesterId + "' не имеет прав на редактирование профиля.")));

        verify(userService, times(1)).deleteProfilePicture(requesterId, userId);
    }
}