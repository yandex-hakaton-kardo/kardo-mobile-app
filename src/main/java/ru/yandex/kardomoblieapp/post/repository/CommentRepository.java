package ru.yandex.kardomoblieapp.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.post.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
