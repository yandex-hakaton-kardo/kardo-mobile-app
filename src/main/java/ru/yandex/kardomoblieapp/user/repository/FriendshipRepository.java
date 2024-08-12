package ru.yandex.kardomoblieapp.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.user.model.Friendship;
import ru.yandex.kardomoblieapp.user.model.FriendshipId;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    @Query("SELECT f.id.friend.id FROM Friendship f WHERE f.id.user.id = ?1")
    List<Long> findUsersFriendsIds(long userId);
}
