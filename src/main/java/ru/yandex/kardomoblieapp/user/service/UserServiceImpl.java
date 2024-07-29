package ru.yandex.kardomoblieapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final DataFileService dataFileService;

    @Override
    public User createUser(User userToAdd) {
        final User savedUser = userRepository.save(userToAdd);
        log.info("Пользователь с id '{}' был сохранен.", savedUser.getId());
        return savedUser;
    }


    //TODO получить от фронта id пользователя, делающего запрос
    @Override
    @Transactional
    public User updateUser(long requesterId, long userId, UserUpdateRequest userUpdateRequest) {
        final User requester = findUser(requesterId);
        final User user = findUser(userId);
        checkAuthorities(userId, requester);
        userMapper.updateUser(userUpdateRequest, user);
        log.info("Профиль пользователя с id '{}' был обновлен.", userId);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(long requesterId, long userId) {
        final User requester = findUser(requesterId);
        findUser(userId);
        checkAuthorities(userId, requester);
        userRepository.deleteById(userId);
    }

    @Override
    public User findUserById(long userId) {
        final User user = findUser(userId);
        log.info("Получены данные пользователя с id '{}'.", userId);
        return user;
    }

    @Override
    @Transactional
    public DataFile uploadProfilePicture(long requesterId, long userId, MultipartFile picture) {
        final User requester = findUser(requesterId);
        final User user = findUser(userId);
        checkAuthorities(userId, requester);

        DataFile uploadedFile = dataFileService.uploadFile(picture, userId);
        user.setProfilePicture(uploadedFile);
        userRepository.save(user);
        log.info("Пользователь с id '{}' загрузил фотографию профиля с id '{}'.", userId, uploadedFile.getId());
        return uploadedFile;
    }

    @Override
    public DataFile getProfilePicture(long userId) {
        final User user = findUser(userId);
        final DataFile profilePicture = user.getProfilePicture();
        log.info("Получение фотографии профиля пользователя с id '{}'.", userId);
        return profilePicture;
    }

    @Override
    public void deleteProfilePicture(long requesterId, long userId) {
        final User requester = findUser(requesterId);
        final User user = findUser(userId);
        checkAuthorities(userId, requester);
        deleteCurrentProfilePictureIfExists(user);
    }

    private void deleteCurrentProfilePictureIfExists(User user) {
        final DataFile profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            dataFileService.deleteFile(profilePicture.getId());
            user.setProfilePicture(null);
            userRepository.save(user);
        }
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }

    private void checkAuthorities(long userId, User requester) {
        if (!(requester.getId() == userId || requester.isAdmin())) {
            throw new NotAuthorizedException("Пользователь с id '" + requester.getId() + "' не имеет прав на редактирование профиля.");
        }
    }
}
