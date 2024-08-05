package ru.yandex.kardomoblieapp.user.service;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.dto.UserSearchFilter;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.User;

import java.util.List;

public interface UserService {

    User createUser(User userToAdd);

    User updateUser(long userId, UserUpdateRequest userUpdateRequest);

    void deleteUser(String username, long userId);

    User findUserById(long userId);

    DataFile uploadProfilePicture(long userId, MultipartFile avatar);

    DataFile getProfilePicture(long userId);

    void deleteProfilePicture(long userId);

    Friendship addFriend(long userId, long friendId);

    List<User> getFriendsList(long userId);

    void deleteFriend(long userId, long friendId);

    User findByUsername(String username);

    List<User> findAllUsers(UserSearchFilter filter, Integer page, Integer size);
}
