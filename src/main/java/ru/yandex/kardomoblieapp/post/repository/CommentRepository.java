package ru.yandex.kardomoblieapp.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.post.model.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.author a WHERE c.id = ?1")
    Optional<Comment> findCommentById(long commentId);
}
