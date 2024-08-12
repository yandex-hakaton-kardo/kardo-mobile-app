package ru.yandex.kardomoblieapp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profilePicture p WHERE u.id IN ?1")
    List<User> findUsersWithIdsIn(List<Long> friendsIds);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profilePicture p LEFT JOIN FETCH u.country c " +
            "LEFT JOIN FETCH u.region r LEFT JOIN FETCH u.city ct WHERE u.username = ?1")
    Optional<User> findFullUserByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profilePicture p LEFT JOIN FETCH u.country c " +
            "LEFT JOIN FETCH u.region r LEFT JOIN FETCH u.city ct WHERE u.id = ?1")
    Optional<User> findFullUserById(long id);
}
