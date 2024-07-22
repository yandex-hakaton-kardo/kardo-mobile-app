package ru.yandex.kardomoblieapp.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    public User createUser(User userToAdd) {
        User savedUser = userRepository.save(userToAdd);
        log.info("Пользователь с id '{}' был сохранен.", savedUser.getId());
        return savedUser;
    }


    //TODO получить от фронта id пользователя, делающего запрос
    @Override
    @Transactional
    public User updateUser(long requesterId, long userId, UserUpdateRequest userUpdateRequest) {
        User requester = findUser(requesterId);
        User user = findUser(userId);
        checkAuthorities(userId, requester);
        userMapper.updateUser(userUpdateRequest, user);
        log.info("Профиль пользователя с id '{}' был обновлен.", userId);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(long requesterId, long userId) {
        User requester = findUser(requesterId);
        findUser(userId);
        checkAuthorities(userId, requester);
        userRepository.deleteById(userId);
    }

    @Override
    public User findUserById(long userId) {
        User user = findUser(userId);
        log.info("Получены данные пользователя с id '{}'.", userId);
        return user;
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
