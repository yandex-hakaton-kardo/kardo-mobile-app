package ru.yandex.kardomoblieapp.user.service;

import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.User;

public interface UserService {

    User createUser(User userToAdd);

    User updateUser(long requesterId, long userId, UserUpdateRequest userUpdateRequest);

    void deleteUser(long requesterId, long userId);

    User findUserById(long userId);
}
