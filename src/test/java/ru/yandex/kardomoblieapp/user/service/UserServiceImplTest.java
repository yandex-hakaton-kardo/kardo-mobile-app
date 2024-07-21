package ru.yandex.kardomoblieapp.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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