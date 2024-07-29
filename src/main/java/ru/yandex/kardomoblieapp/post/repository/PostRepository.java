package ru.yandex.kardomoblieapp.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.post.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author u LEFT JOIN FETCH p.file f WHERE u.id = ?1")
    List<Post> findPostsByAuthorId(long userId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author u LEFT JOIN FETCH p.file f WHERE p.id = ?1")
    Optional<Post> findPostById(long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author u LEFT JOIN FETCH p.file f LEFT JOIN p.comments c WHERE p.id = ?1")
    Optional<Post> findPostWithCommentsById(long id);


    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.file f")
    List<Post> getPostsFeed(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author a LEFT JOIN FETCH p.file f WHERE a.id NOT IN ?1")
    List<Post> findRecommendations(List<Long> friendsIds, Pageable pageable);
}
