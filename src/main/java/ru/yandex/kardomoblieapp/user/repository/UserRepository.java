package ru.yandex.kardomoblieapp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profilePicture p WHERE u.id IN ?1")
    List<User> findUsersWithIdsIn(List<Long> friendsIds);

    Optional<User> findByUsername(String username);
}
