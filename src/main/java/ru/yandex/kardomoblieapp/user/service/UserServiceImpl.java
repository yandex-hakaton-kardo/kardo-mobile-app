package ru.yandex.kardomoblieapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.repository.DataFileRepository;
import ru.yandex.kardomoblieapp.shared.exception.DataFileStorageException;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final DataFileRepository dataFileRepository;

    @Value("${server.file-storage.directory}")
    private String baseFileDirectory;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

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

        try {
            deleteCurrentProfilePictureIfExists(user);
            final String userFileStorage = baseFileDirectory + "/" + user.getId() + "/";
            createDirectoryIfNotExists(userFileStorage);

            final String profilePicturePath = userFileStorage + picture.getOriginalFilename();
            final Path file = Paths.get(profilePicturePath);
            final DataFile dataFile = DataFile.builder()
                    .fileName(picture.getName())
                    .fileType(picture.getContentType())
                    .filePath(profilePicturePath)
                    .build();
            picture.transferTo(file);
            final DataFile savedFile = dataFileRepository.save(dataFile);
            user.setProfilePicture(dataFile);
            userRepository.save(user);
            log.info("Пользователь с id '{}' загрузил фотографию профиля '{}'.", userId, picture.getName());
            return savedFile;
        } catch (IOException e) {
            throw new DataFileStorageException(e.getCause().getMessage());
        }
    }

    @Override
    public byte[] downloadProfilePicture(long userId) {
        final User user = findUser(userId);
        final DataFile profilePicture = user.getProfilePicture();
        if (profilePicture == null) {
            throw new NotFoundException("У пользователя c id'" + user.getId() + "' нет фотографии профиля.");
        }

        try {
            byte[] file = Files.readAllBytes(Paths.get(profilePicture.getFilePath()));
            log.info("Получение фотографии профиля '{}' пользователя с id '{}'.", profilePicture.getFileName(), userId);
            return file;
        } catch (IOException e) {
            throw new DataFileStorageException(e.getCause().getMessage());
        }
    }

    @Override
    public void deleteProfilePicture(long requesterId, long userId) {
        final User requester = findUser(requesterId);
        final User user = findUser(userId);
        checkAuthorities(userId, requester);

        try {
            deleteCurrentProfilePictureIfExists(user);
        } catch (IOException e) {
            throw new DataFileStorageException(e.getCause().getMessage());
        }
    }

    private void deleteCurrentProfilePictureIfExists(User user) throws IOException {
        final DataFile profilePicture = user.getProfilePicture();
        if (profilePicture != null) {
            dataFileRepository.deleteById(profilePicture.getId());
            final Path currentPicture = Paths.get(profilePicture.getFilePath());
            Files.deleteIfExists(currentPicture);
            user.setProfilePicture(null);
            userRepository.save(user);
        }
    }

    private void createDirectoryIfNotExists(String userFileStorage) throws IOException {
        final Path directory = Paths.get(userFileStorage);
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
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
