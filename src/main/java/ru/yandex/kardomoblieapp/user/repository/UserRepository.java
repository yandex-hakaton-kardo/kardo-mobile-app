package ru.yandex.kardomoblieapp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
