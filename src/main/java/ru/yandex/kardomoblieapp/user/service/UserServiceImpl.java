package ru.yandex.kardomoblieapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.location.model.City;
import ru.yandex.kardomoblieapp.location.model.Country;
import ru.yandex.kardomoblieapp.location.model.Region;
import ru.yandex.kardomoblieapp.location.service.LocationService;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.LocationInfo;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.FriendshipId;
import ru.yandex.kardomoblieapp.user.model.FriendshipStatus;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.model.UserRole;
import ru.yandex.kardomoblieapp.user.repository.FriendshipRepository;
import ru.yandex.kardomoblieapp.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final DataFileService dataFileService;

    private final FriendshipRepository friendshipRepository;

    private final PasswordEncoder passwordEncoder;

    private final LocationService locationService;

    @Override
    public User createUser(User userToAdd) {
        userToAdd.setPassword(passwordEncoder.encode(userToAdd.getPassword()));
        userToAdd.setRole(UserRole.USER);
        final User savedUser = userRepository.save(userToAdd);
        log.info("Пользователь с id '{}' был сохранен.", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(long userId, UserUpdateRequest userUpdateRequest) {
        final User user = getUser(userId);
        userMapper.updateUser(userUpdateRequest, user);
        LocationInfo locationInfo = LocationInfo.builder()
                .countryId(userUpdateRequest.getCountryId())
                .regionId(userUpdateRequest.getRegionId())
                .city(userUpdateRequest.getCity())
                .build();
        setLocationToUser(locationInfo, user);
        userRepository.save(user);
        log.info("Профиль пользователя с id '{}' был обновлен.", userId);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(String username, long userId) {
        getUser(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User findUserById(long userId) {
        final User user = getUser(userId);
        log.info("Получены данные пользователя с id '{}'.", userId);
        return user;
    }

    @Override
    @Transactional
    public DataFile uploadProfilePicture(long userId, MultipartFile picture) {
        final User user = getUser(userId);

        DataFile uploadedFile = dataFileService.uploadFile(picture, userId);
        user.setProfilePicture(uploadedFile);
        userRepository.save(user);
        log.info("Пользователь с id '{}' загрузил фотографию профиля с id '{}'.", userId, uploadedFile.getId());
        return uploadedFile;
    }

    @Override
    public DataFile getProfilePicture(long userId) {
        final User user = getUser(userId);
        final DataFile profilePicture = user.getProfilePicture();
        log.info("Получение фотографии профиля пользователя с id '{}'.", userId);
        return profilePicture;
    }

    @Override
    public void deleteProfilePicture(long userId) {
        final User user = getUser(userId);
        deleteCurrentProfilePictureIfExists(user);
    }

    @Override
    @Transactional
    public Friendship addFriend(long userId, long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        List<Long> secondUserFriendsIds = friendshipRepository.findUsersFriendsIds(friendId);
        Friendship friendship = new Friendship();
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

    @Override
    @Transactional
    public List<User> getFriendsList(long userId) {
        getUser(userId);
        List<Long> friendsIds = friendshipRepository.findUsersFriendsIds(userId);
        List<User> friends = userRepository.findUsersWithIdsIn(friendsIds);
        log.info("Получен список друзей пользователя с id '{}'. Всего друзей: '{}'.", userId, friends.size());
        return friends;
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        friendshipRepository.deleteById(FriendshipId.of(user, friend));
        log.info("Пользователь с id '{}' удалил из друзей пользователя с id '{}'.", userId, friendId);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь с именем '" + username + "' не найден."));
    }

    @Override
    public List<User> findAllUsers(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userRepository.findAll(pageable).getContent();
        log.info("Получен список всех пользователей");
        return users;
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

    private void setLocationToUser(LocationInfo locationInfo, User user) {
        if (locationInfo.getCountryId() != null) {
            Country country = locationService.getCountryById(locationInfo.getCountryId());
            user.setCountry(country);
        }

        if (locationInfo.getRegionId() != null) {
            Region region = locationService.getRegionById(locationInfo.getRegionId());
            user.setRegion(region);
        }

        if (locationInfo.getCity() != null) {
            Optional<City> city = locationService.findCityByNameCountryAndRegion(locationInfo.getCity(),
                    user.getCountry() != null ? user.getCountry().getId() : null,
                    user.getRegion() != null ? user.getRegion().getId() : null);

            if (city.isPresent()) {
                user.setCity(city.get());
            } else {
                City newCity = City.builder()
                        .name(locationInfo.getCity())
                        .country(user.getCountry())
                        .region(user.getRegion())
                        .build();
                City savedCity = locationService.addCity(newCity);
                user.setCity(savedCity);
            }
        }
    }
}
