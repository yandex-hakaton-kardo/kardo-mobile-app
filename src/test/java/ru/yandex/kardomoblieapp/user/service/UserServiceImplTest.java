package ru.yandex.kardomoblieapp.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserSearchFilter;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.model.UserRole;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

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

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_VERSION);

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void init() {
        user1 = createUser(1);
        user2 = createUser(2);
        user3 = createUser(3);
        updateRequest = UserUpdateRequest.builder()
                .name("updated Имя")
                .secondName("updated Отчество")
                .surname("updated Фамилия")
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
        User updatedUser = userService.updateUser(savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление полей, если null, то сохраняется старое значение")
    void updateUser_whenFieldNull_shouldRemainOldValue() {
        updateRequest.setName(null);
        User savedUser = userService.createUser(user1);
        User updatedUser = userService.updateUser(savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
    }

    @Test
    @DisplayName("Обновление страны, региона и города")
    void updateUser_whenCountryRegionAndCityNotNull_shouldUpdateAllFields() {
        User savedUser = userService.createUser(user1);
        updateRequest.setCountryId(1L);
        updateRequest.setRegionId(2L);
        updateRequest.setCity("City");
        User updatedUser = userService.updateUser(savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
        assertThat(updatedUser.getCountry().getId(), is(1L));
        assertThat(updatedUser.getRegion().getId(), is(2L));
        assertThat(updatedUser.getCity().getId(), greaterThan(0L));
        assertThat(updatedUser.getCity().getName(), is("City"));
    }

    @Test
    @DisplayName("Обновление страны и города")
    void updateUser_whenCountryAndCityNotNull_shouldUpdateCountryAndRegion() {
        User savedUser = userService.createUser(user1);
        updateRequest.setCountryId(1L);
        updateRequest.setCity("City");
        User updatedUser = userService.updateUser(savedUser.getId(), updateRequest);

        assertThat(updatedUser, notNullValue());
        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateRequest.getEmail()));
        assertThat(updatedUser.getCountry().getId(), is(1L));
        assertThat(updatedUser.getRegion(), nullValue());
        assertThat(updatedUser.getCity().getId(), greaterThan(0L));
        assertThat(updatedUser.getCity().getName(), is("City"));
    }

    @Test
    @DisplayName("Обновление пользователя, пользователь не найден")
    void updateUser_whenUnknownId_ShouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(unknownId, updateRequest));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        User savedUser = userService.createUser(user1);
        userService.deleteUser(savedUser.getId());

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
        DataFile savedFile = userService.uploadProfilePicture(savedUser.getId(), file);

        assertThat(savedFile, notNullValue());
        assertThat(savedFile.getFileType(), is(file.getContentType()));
        assertThat(savedUser.getId(), greaterThan(0L));
    }

    @Test
    @DisplayName("Добавление фотографии профиля, пользователь не найден")
    void uploadProfilePicture_whenUserNotExists_shouldThrowNotFoundException() throws IOException {

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.uploadProfilePicture(unknownId, file));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение фотографии профиля пользователя")
    void downloadProfilePicture_shouldReturnByteArray() throws IOException {
        User savedUser = userService.createUser(user1);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);
        userService.uploadProfilePicture(savedUser.getId(), file);

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
    @DisplayName("Удаление фотографии пользователя")
    void deleteProfilePicture() throws IOException {
        User savedUser = userService.createUser(user1);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, inputStream);
        userService.uploadProfilePicture(savedUser.getId(), file);

        userService.deleteProfilePicture(savedUser.getId());

        User userWithoutAvatar = userService.findUserById(savedUser.getId());

        assertThat(userWithoutAvatar.getProfilePicture(), nullValue());
    }

    @Test
    @DisplayName("Удаление фотографии пользователя, пользователь не найден")
    void deleteProfilePicture_whenUserNotExists_shouldThrowNotFoundException() {

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.deleteProfilePicture(unknownId));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Добавление пользователя в друзья, статус подписчик")
    void addFriend_whenNotMutual_shouldSetStatusSubscriber() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        Friendship friendship = userService.addFriend(savedUser.getId(), savedUser2.getId());

        System.out.println(friendship);

        assertThat(friendship, notNullValue());
        assertThat(friendship.getStatus(), is(FriendshipStatus.SUBSCRIBER));
        assertThat(friendship.getId().getUser().getId(), is(savedUser.getId()));
        assertThat(friendship.getId().getFriend().getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Добавление пользователя в друзья, статус друг")
    void addFriend_whenMutual_shouldSetStatusFriend() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        userService.addFriend(savedUser.getId(), savedUser2.getId());
        Friendship friendship = userService.addFriend(savedUser2.getId(), savedUser.getId());

        System.out.println(friendship);

        assertThat(friendship, notNullValue());
        assertThat(friendship.getStatus(), is(FriendshipStatus.FRIEND));
        assertThat(friendship.getId().getUser().getId(), is(savedUser2.getId()));
        assertThat(friendship.getId().getFriend().getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Получение списка друзей, друзей нет")
    void getFriendsList_whenNoFriends_shouldReturnEmptyList() {
        User savedUser = userService.createUser(user1);

        List<User> friends = userService.getFriendsList(savedUser.getId());

        assertThat(friends, notNullValue());
        assertThat(friends, emptyIterable());
    }

    @Test
    @DisplayName("Получение списка друзей, один друг")
    void getFriendsList_whenOneFriends_shouldReturnListOfOne() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        userService.addFriend(savedUser.getId(), savedUser2.getId());

        List<User> friends = userService.getFriendsList(savedUser.getId());

        assertThat(friends, notNullValue());
        assertThat(friends.size(), is(1));
        assertThat(friends.get(0).getId(), is(savedUser2.getId()));


        List<User> friends2 = userService.getFriendsList(savedUser2.getId());

        assertThat(friends2, notNullValue());
        assertThat(friends2, emptyIterable());
    }

    @Test
    @DisplayName("Получение списка друзей, обоюдная дружба")
    void getFriendsList_whenMutualFriendship_shouldReturnListOfOne() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        userService.addFriend(savedUser.getId(), savedUser2.getId());
        userService.addFriend(savedUser2.getId(), savedUser.getId());

        List<User> friendsOfUser = userService.getFriendsList(savedUser.getId());
        List<User> friendsOfUser2 = userService.getFriendsList(savedUser2.getId());

        assertThat(friendsOfUser, notNullValue());
        assertThat(friendsOfUser.size(), is(1));
        assertThat(friendsOfUser.get(0).getId(), is(savedUser2.getId()));
        assertThat(friendsOfUser2, notNullValue());
        assertThat(friendsOfUser2.size(), is(1));
        assertThat(friendsOfUser2.get(0).getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Удаление пользователя из друзей")
    void deleteFriend() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        userService.addFriend(savedUser.getId(), savedUser2.getId());

        userService.deleteFriend(savedUser.getId(), savedUser2.getId());

        List<User> friends = userService.getFriendsList(savedUser2.getId());

        assertThat(friends, notNullValue());
        assertThat(friends, emptyIterable());
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void findAllUsers_whenFilterForUsername_shouldReturnUserWithDesiredUsername() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        UserSearchFilter filter = new UserSearchFilter(savedUser.getUsername());

        List<User> users = userService.findAllUsers(filter, 0, 10);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(1));
        assertThat(users.get(0).getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void findAllUsers_whenFilterForNotFullUsername_shouldReturnUsersWithDesiredUsername() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        UserSearchFilter filter = new UserSearchFilter("username");

        List<User> users = userService.findAllUsers(filter, 0, 10);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser.getId()));
        assertThat(users.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findAllUsers_whenSearchForEmail_shouldReturnAllUsersWithDesiredEmail() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        UserSearchFilter filter = new UserSearchFilter("test@mail.ru");

        List<User> users = userService.findAllUsers(filter, 0, 10);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.get(0).getId(), is(savedUser.getId()));
        assertThat(users.get(1).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findAllUsers_whenFilterIsNull_shouldReturnAllUsersIncludingAdmin() {
        User savedUser = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);
        UserSearchFilter filter = new UserSearchFilter(null);

        List<User> users = userService.findAllUsers(filter, 0, 10);

        assertThat(users, notNullValue());
        assertThat(users.size(), is(3));
        assertThat(users.get(0).getUsername(), is("admin"));
        assertThat(users.get(1).getId(), is(savedUser.getId()));
        assertThat(users.get(2).getId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Поиск пользователя по username")
    void findFullUserByUsername_whenUserExists_shouldReturnUser() {
        User savedUser = userService.createUser(user1);

        User user = userService.findFullUserByUsername(savedUser.getUsername());

        assertThat(user, notNullValue());
        assertThat(user.getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Поиск пользователя по username, пользователь не найден")
    void findFullUserByUsername_whenUserNotExists_shouldThrowNotFoundException() {
        String unknownUsername = "unknownUsername";

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.findFullUserByUsername(unknownUsername));

        assertThat(ex.getLocalizedMessage(), is("Пользователь с никнеймом '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Изменение роли пользователя")
    void changeUserRole_whenChangedToAdmin_shouldReturnUserWithAdminRole() {
        User savedUser = userService.createUser(user1);

        assertThat(savedUser.getRole(), is(UserRole.USER));

        User changed = userService.changeUserRole(savedUser.getId(), UserRole.ADMIN);

        assertThat(changed, notNullValue());
        assertThat(changed.getId(), is(savedUser.getId()));
        assertThat(changed.getRole(), is(UserRole.ADMIN));
    }

    private User createUser(int id) {
        return User.builder()
                .username("username" + id)
                .name("Имя" + id)
                .secondName("Отчество")
                .surname("Фамилия")
                .email(id + "test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
    }
}