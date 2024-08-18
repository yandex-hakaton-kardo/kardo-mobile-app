package ru.yandex.kardomoblieapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.location.dto.Location;
import ru.yandex.kardomoblieapp.location.service.LocationService;
import ru.yandex.kardomoblieapp.shared.exception.InvalidDateOfBirthException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserSearchFilter;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.FriendshipId;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.model.UserRole;
import ru.yandex.kardomoblieapp.user.repository.FriendshipRepository;
import ru.yandex.kardomoblieapp.user.repository.UserRepository;
import ru.yandex.kardomoblieapp.user.repository.UserSpecification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final int OLDEST_AGE = 100;

    private static final int YOUNGEST_AGE = 6;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final DataFileService dataFileService;

    private final FriendshipRepository friendshipRepository;

    private final PasswordEncoder passwordEncoder;

    private final LocationService locationService;

    /**
     * Регистрация нового пользователя в приложении. Пользователь создается с ролью USER. При сохранении
     * в БД пароль пользователя кодируется.
     *
     * @param userToAdd данные нового пользователя
     * @return добавленный пользователь с назначенным id
     */
    @Override
    public User createUser(User userToAdd) {
        userToAdd.setPassword(passwordEncoder.encode(userToAdd.getPassword()));
        userToAdd.setRole(UserRole.USER);
        final User savedUser = userRepository.save(userToAdd);
        log.debug("Пользователь с id '{}' был сохранен.", savedUser.getId());
        return savedUser;
    }

    /**
     * Обновление данных пользователя.
     *
     * @param userId            идентификатор пользователя, который обновляет данные
     * @param userUpdateRequest обновленные данные
     * @return пользователь с обновленными данными
     */
    @Override
    @Transactional
    public User updateUser(long userId, UserUpdateRequest userUpdateRequest) {
        final User user = getUser(userId);
        validateDateOfBirth(userUpdateRequest.getDateOfBirth());
        userMapper.updateUser(userUpdateRequest, user);
        setLocationToUser(user, userUpdateRequest.getCountryId(), userUpdateRequest.getRegionId(),
                userUpdateRequest.getCity());
        userRepository.save(user);
        log.info("Профиль пользователя с id '{}' был обновлен.", userId);
        return user;
    }

    /**
     * Удаление пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    @Override
    @Transactional
    public void deleteUser(long userId) {
        getUser(userId);
        userRepository.deleteById(userId);
    }

    /**
     * Поиск пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     */
    @Override
    public User findUserById(long userId) {
        final User user = getUser(userId);
        log.debug("Получены данные пользователя с id '{}'.", userId);
        return user;
    }

    /**
     * Загрузка фотографии профиля. Имя файла при сохранении заменяется на UUID. В БД сохраняется имя и путь до файла,
     * а сам файл перемещается в локальное хранилище.
     *
     * @param userId  идентификатор пользователя, который загружает фотографию
     * @param picture файл с изображением
     * @return данные о загруженном файле
     */
    @Override
    @Transactional
    public DataFile uploadProfilePicture(long userId, MultipartFile picture) {
        final User user = getUser(userId);

        final DataFile uploadedFile = dataFileService.uploadFile(picture, userId);
        user.setProfilePicture(uploadedFile);
        userRepository.save(user);
        log.info("Пользователь с id '{}' загрузил фотографию профиля с id '{}'.", userId, uploadedFile.getId());
        return uploadedFile;
    }

    /**
     * Получение данных о местоположении фотографии профиля.
     *
     * @param userId идентификатор пользователя
     * @return данные о местоположении фотографии профиля
     */
    @Override
    public DataFile getProfilePicture(long userId) {
        final User user = getUser(userId);
        final DataFile profilePicture = user.getProfilePicture();
        log.debug("Получение фотографии профиля пользователя с id '{}'.", userId);
        return profilePicture;
    }

    /**
     * Удаление фотографии профиля.
     *
     * @param userId идентификатор пользователя
     */
    @Override
    public void deleteProfilePicture(long userId) {
        final User user = getUser(userId);
        deleteCurrentProfilePictureIfExists(user);
    }

    /**
     * Добавление пользователя в список друзей. По умолчанию пользователь, добавленный в список друзей, получает статус
     * ПОДПИСЧИК. Если оба пользователя добавляют друг друга в список друзей, то статус изменятся на ДРУГ.
     *
     * @param userId   идентификатор пользователя, добавляющий друга
     * @param friendId идентификатор пользователя, которого добавляют в список друзей
     * @return состояние дружбы между пользователями
     */
    @Override
    @Transactional
    public Friendship addFriend(long userId, long friendId) {
        final User user = getUser(userId);
        final User friend = getUser(friendId);
        final List<Long> secondUserFriendsIds = friendshipRepository.findUsersFriendsIds(friendId);
        final Friendship friendship = new Friendship();
        friendship.setId(FriendshipId.of(user, friend));
        if (secondUserFriendsIds.stream().anyMatch(id -> id == userId)) {
            friendship.setStatus(FriendshipStatus.FRIEND);
            Friendship mutualFriendship = Friendship.builder()
                    .id(FriendshipId.of(friend, user))
                    .status(FriendshipStatus.FRIEND)
                    .build();
            friendshipRepository.save(mutualFriendship);
        } else {
            friendship.setStatus(FriendshipStatus.SUBSCRIBER);
        }
        friendshipRepository.save(friendship);
        log.info("Пользователи с id '{}' и '{}' стали друзьями.", userId, friendId);
        return friendship;
    }

    /**
     * Получение списка друзей пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список друзей пользователя
     */
    @Override
    @Transactional
    public List<User> getFriendsList(long userId) {
        getUser(userId);
        final List<Long> friendsIds = friendshipRepository.findUsersFriendsIds(userId);
        final List<User> friends = userRepository.findUsersWithIdsIn(friendsIds);
        log.info("Получен список друзей пользователя с id '{}'. Всего друзей: '{}'.", userId, friends.size());
        return friends;
    }

    /**
     * Удаление пользователя из списка друзей.
     *
     * @param userId   идентификатор пользователя, делающий запрос
     * @param friendId идентификатор пользователя из списка друзей
     */
    @Override
    public void deleteFriend(long userId, long friendId) {
        final User user = getUser(userId);
        final User friend = getUser(friendId);
        friendshipRepository.deleteById(FriendshipId.of(user, friend));
        log.info("Пользователь с id '{}' удалил из друзей пользователя с id '{}'.", userId, friendId);
    }

    /**
     * Поиск пользователя по никнейму.
     *
     * @param username никнейм пользователя
     * @return найденный пользователь
     */
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь с именем '" + username + "' не найден."));
    }

    /**
     * Поиск пользователей по фильтру. Список пользователей возвращается постранично, согласно заданным параметрам.
     *
     * @param filter фильтр поиска
     * @param page   номер страницы
     * @param size   количество элементов на странице
     * @return найденный список пользователей
     */
    @Override
    public List<User> findAllUsers(UserSearchFilter filter, Integer page, Integer size) {
        final Pageable pageable = PageRequest.of(page, size);
        final List<Specification<User>> specifications = userSearchFilterToSpecifications(filter);
        final Specification<User> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        return userRepository.findAll(resultSpec, pageable).getContent();
    }

    /**
     * Получение полных данных пользователя по никнейму.
     *
     * @param username никнейм пользователя
     * @return найденный пользователь
     */
    @Override
    public User findFullUserByUsername(String username) {
        final User user = userRepository.findFullUserByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь с никнеймом '" + username + "' не найден."));
        log.debug("Получен пользователь с username '{}'.", username);
        return user;
    }

    /**
     * Изменение роли пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param newRole новая роль пользователя
     * @return измененный пользователь
     */
    @Override
    public User changeUserRole(long userId, UserRole newRole) {
        User user = getUser(userId);
        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        log.info("У пользователя с id '{}' новая роль '{}'.", userId, newRole.name());
        return savedUser;
    }

    private List<Specification<User>> userSearchFilterToSpecifications(UserSearchFilter searchFilter) {
        final List<Specification<User>> resultSpecification = new ArrayList<>();
        resultSpecification.add(UserSpecification.textInUsernameOrEmail(searchFilter.getName()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void deleteCurrentProfilePictureIfExists(User user) {
        final DataFile profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            dataFileService.deleteFile(profilePicture.getId());
            user.setProfilePicture(null);
            userRepository.save(user);
        }
    }

    private User getUser(long userId) {
        return userRepository.findFullUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null) {
            final LocalDate currentDate = LocalDate.now();
            final LocalDate youngerThanDate = currentDate.minusYears(OLDEST_AGE);
            final LocalDate olderThanDate = currentDate.minusYears(YOUNGEST_AGE);
            if (dateOfBirth.isBefore(youngerThanDate) || dateOfBirth.isAfter(olderThanDate)) {
                throw new InvalidDateOfBirthException("Недопустимое значение даты рождения '" + dateOfBirth + "'.");
            }
        }
    }

    private void setLocationToUser(User user, Long countryId, Long regionId, String cityName) {
        final Location location = locationService.getLocation(countryId, regionId, cityName);
        if (location.getCountry() != null) {
            user.setCountry(location.getCountry());
        }
        if (location.getRegion() != null) {
            user.setRegion(location.getRegion());
        }
        if (location.getCity() != null) {
            user.setCity(location.getCity());
        }
    }
}
