package ru.yandex.kardomoblieapp.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User user1;

    private User user2;

    private User user3;

    private UserUpdateRequest updateRequest;

    private long unknownId;

    @BeforeEach
    void init() {
        user1 = createUser(1);
        user2 = createUser(2);
        user3 = createUser(3);
        updateRequest = UserUpdateRequest.builder()
                .name("updated Имя")
                .secondName("updated Отчество")
                .surname("updated Фамилия")
                .country("updated Россия")
                .city("updated Москва")
                .email("updatedtest@mail.ru")
                .password("updated password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        unknownId = 99999L;
    }

    @Test
    @DisplayName("Добавление пользователя")
    void createUser() {
        User savedUser = userService.createUser(user1);

        assertThat(savedUser, notNullValue());
        assertThat(savedUser.getId(), greaterThan(0L));
        assertThat(savedUser.getEmail(), is(user1.getEmail()));
    }

    @Test
    @DisplayName("Обновление всех полей пользователя")
    void updateUser() {
        User savedUser = userService.createUser(user1);
        User updatedUser = userService.updateUser(savedUser.getId(), savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление полей, если null, то сохраняется старое значение")
    void updateUser_whenFieldNull_shouldRemainOldValue() {
        updateRequest.setName(null);
        User savedUser = userService.createUser(user1);
        User updatedUser = userService.updateUser(savedUser.getId(), savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление пользователя, пользователь не найден")
    void updateUser_whenUnknownId_ShouldThrowNotFoundException() {
        User savedUser = userService.createUser(user1);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(savedUser.getId(), unknownId, updateRequest));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Обновление пользователя, пользователь не имеет прав на редактирование")
    void updateUser_whenUnauthorized_ShouldThrowNotFoundException() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> userService.updateUser(savedUser2.getId(), savedUser.getId(), updateRequest));

        assertThat(ex.getLocalizedMessage(),
                is("Пользователь с id '" + savedUser2.getId() + "' не имеет прав на редактирование профиля."));
    }


    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        User savedUser = userService.createUser(user1);
        userService.deleteUser(savedUser.getId(), savedUser.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.findUserById(savedUser.getId()));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + savedUser.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Добавление фотографии профиля")
    void uploadProfilePicture_shouldReturnNotEmptyDataFile() throws IOException {
        User savedUser = userService.createUser(user1);

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);
        DataFile savedFile = userService.uploadProfilePicture(savedUser.getId(), savedUser.getId(), file);

        assertThat(savedFile, notNullValue());
        assertThat(savedFile.getFileName(), is(file.getName()));
        assertThat(savedFile.getFileType(), is(file.getContentType()));
        assertThat(savedUser.getId(), greaterThan(0L));
    }

    @Test
    @DisplayName("Добавление фотографии профиля, пользователь не найден")
    void uploadProfilePicture_whenUserNotExists_shouldThrowNotFoundException() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.uploadProfilePicture(unknownId, unknownId, file));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Добавление фотографии профиля, пользователь не имеет прав на редактирование")
    void uploadProfilePicture_whenUnauthorized_ShouldThrowNotFoundException() throws IOException {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);


        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> userService.uploadProfilePicture(savedUser2.getId(), savedUser.getId(), file));

        assertThat(ex.getLocalizedMessage(),
                is("Пользователь с id '" + savedUser2.getId() + "' не имеет прав на редактирование профиля."));
    }

    @Test
    @DisplayName("Получение фотографии профиля пользователя")
    void downloadProfilePicture_shouldReturnByteArray() throws IOException {
        User savedUser = userService.createUser(user1);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);
        userService.uploadProfilePicture(savedUser.getId(), savedUser.getId(), file);

        DataFile profilePicture = userService.getProfilePicture(savedUser.getId());

        assertThat(profilePicture, notNullValue());
    }

    @Test
    @DisplayName("Получение фотографии профиля пользователя, пользователь не найден")
    void downloadProfilePicture_whenUserNotExists_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getProfilePicture(unknownId));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение фотографии профиля пользователя, у пользователя нет фотографии профиля")
    void downloadProfilePicture_whenNoProfilePicture_shouldThrowNotFoundException() {
        User savedUser = userService.createUser(user1);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getProfilePicture(savedUser.getId()));

        assertThat(ex.getLocalizedMessage(),
                is("У пользователя c id'" + savedUser.getId() + "' нет фотографии профиля."));
    }

    @Test
    @DisplayName("Удаление фотографии пользователя")
    void deleteProfilePicture() throws IOException {
        User savedUser = userService.createUser(user1);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);
        userService.uploadProfilePicture(savedUser.getId(), savedUser.getId(), file);

        userService.deleteProfilePicture(savedUser.getId(), savedUser.getId());

        User userWithoutAvatar = userService.findUserById(savedUser.getId());

        assertThat(userWithoutAvatar.getProfilePicture(), nullValue());
    }

    @Test
    @DisplayName("Удаление фотографии пользователя, пользователь не найден")
    void deleteProfilePicture_whenUserNotExists_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.deleteProfilePicture(unknownId, unknownId));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление фотографии пользователя, пользователь не имеет прав на редактирование профиля")
    void deleteProfilePicture_whenUnauthorized_shouldThrowNotFoundException() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> userService.deleteProfilePicture(savedUser.getId(), savedUser2.getId()));

        assertThat(ex.getLocalizedMessage(),
                is("Пользователь с id '" + savedUser.getId() + "' не имеет прав на редактирование профиля."));
    }

    private User createUser(int id) {
        return User.builder()
                .name("Имя" + id)
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email(id + "test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
    }
}