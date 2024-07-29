package ru.yandex.kardomoblieapp.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.post.model.PostLike;
import ru.yandex.kardomoblieapp.post.model.PostLikeId;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    @Query(value = "SELECT COUNT(*) FROM post_likes WHERE post_id = ?1", nativeQuery = true)
    long findPostLikesCount(long postId);
}
