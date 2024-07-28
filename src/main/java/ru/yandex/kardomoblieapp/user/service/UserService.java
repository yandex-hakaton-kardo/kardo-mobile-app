package ru.yandex.kardomoblieapp.user.service;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

public interface UserService {

    User createUser(User userToAdd);

    User updateUser(long requesterId, long userId, UserUpdateRequest userUpdateRequest);

    void deleteUser(long requesterId, long userId);

    User findUserById(long userId);

    DataFile uploadProfilePicture(long requesterId, long userId, MultipartFile avatar);

    DataFile getProfilePicture(long userId);

    void deleteProfilePicture(long requesterId, long userId);
}
